package pontoeletronico.controller;

import br.jus.tjms.pontoeletronico.client.Constantes;
import pontoeletronico.util.JFXUtil;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;

import org.apache.commons.codec.digest.DigestUtils;

import br.jus.tjms.pontoeletronico.to.DigitalTO;
import br.jus.tjms.pontoeletronico.to.FuncionarioTO;
import br.jus.tjms.pontoeletronico.to.UpdateInfoTO;
import java.io.File;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import pontoeletronico.bean.Digital;
import pontoeletronico.bean.Funcionario;
import pontoeletronico.bean.Parametro;
import pontoeletronico.factory.ServiceLocalFactory;
import pontoeletronico.factory.ServiceRemoteFactory;
import pontoeletronico.jobs.UpdateAppJob;
import pontoeletronico.log.LogMachine;
import pontoeletronico.service.AtualizadorHorario;
import pontoeletronico.service.RelogioJobService;
import pontoeletronico.tipo.TipoOperacao;
import pontoeletronico.util.AudioUtils;
import pontoeletronico.util.Fala;

public class PrincipalController implements Initializable {

    @FXML
    private TextField inputMatricula;

    @FXML
    private Text txData;

    @FXML
    private Text txHora;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        final PrincipalController controller = this;
        
        // cria atualizador do label hora e data
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                AtualizadorHorario thread = new AtualizadorHorario(controller, false);
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
            }
        });

    }

    @FXML
    public void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.F11) {
            try {
                // falar a data
                Fala.falarData(RelogioJobService.getInstancia().getDataHora());
            } catch (Exception ex) {
                Logger.getLogger(PrincipalController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (event.getCode() == KeyCode.F12) {
            try {
                // falar a hora
                Fala.falarHora(RelogioJobService.getInstancia().getDataHora());
            } catch (Exception ex) {
                Logger.getLogger(PrincipalController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @FXML
    public void actionRegistraPonto(ActionEvent event) {

        String texto = inputMatricula.getText();
        
        if (texto.equalsIgnoreCase("#CONFIG")||texto.equalsIgnoreCase("#ADMIN")) {
            LoginController controller = Main.showTelaLogin(true);

            if (controller.getSenhaValida()) {
                Main.showTelaConfiguracao();
            }

        } else if (texto.equalsIgnoreCase("#EXIT")) {
            
            Platform.exit();
            System.exit(0);
            
        } else if (texto.equalsIgnoreCase("#RESTART")||texto.equalsIgnoreCase("#REBOOT")||texto.equalsIgnoreCase("#REINICIAR")) {
            
            try {
                
                exibirMensagem("O sistema vai ser reiniciado agora...");
                
                reiniciar(false, "");
                
            } catch (Exception e) {
                e.printStackTrace();
                exibirMensagem("Não foi possível reiniciar. Erro: " + e.getMessage());
                LogMachine.getInstancia().logErro(e.getMessage(), this.getClass().getName(), "#RESTART");
            } 
            
        } else if (texto.equalsIgnoreCase("#UPDATE")) {
            try {
                
                verificarAtualizacao();
                
            } catch (Exception e) {
                e.printStackTrace();
                exibirMensagem("Não foi possível atualizar. Erro: " + e.getMessage());
                LogMachine.getInstancia().logErro(e.getMessage(), this.getClass().getName(), "#UPDATE");
            }            
        } else if (texto.equalsIgnoreCase("#VERSAO")) {
            
            String msg = "Versão App: " + Constantes.CLIENTE_VERSAO+", Versão Bd: "+obterVersaoBD()+" ("+Constantes.CLIENTE_VERSAOBD+")";
            exibirMensagem(msg);
            LogMachine.getInstancia().logInfo(msg, this.getClass().getName(), "#VERSAO#");
            
        } else {
            
            registrarPonto();

        }

        inputMatricula.setText("");

    }
    
    
    private String obterVersaoBD() {
        
        // obter versão de banco de dados
        Parametro p = ServiceLocalFactory.getParametroService().getParametros();
        
        if (p!=null && p.getVersaoBD()!=null) {
            return p.getVersaoBD();
        } else {
            return "N/D";
        }

    }    

    public void atualizaLabelData(String data) {
        txData.setText(data);

    }

    public void atualizaLabelHora(String hora) {
        txHora.setText(hora);

    }

    public void registrarPonto() {

        if (!ServiceLocalFactory.getConfiguracaoService().isConfigurado()) {
            exibirMensagem("Erro: configuração de localidade não definida!");
            return;
        }

        int matricula = 0;

        try {

            try {
                matricula = Integer.valueOf(inputMatricula.getText().trim());
                inputMatricula.setText("");
            } catch (NumberFormatException e) {
                exibirMensagem("Informe uma matrícula válida!");
                inputMatricula.setText("");
                return;
            }

            // busca funcionario localmente
            Funcionario funcionario = ServiceLocalFactory.getFuncionarioServiceLocal().buscarPorId(matricula);

            if (funcionario == null) {

                // nao existe o funcionario localmente, busca remotamente
                FuncionarioTO uoFnc = ServiceLocalFactory.getFuncionarioServiceLocal().buscarRemotoFuncionarioPorId(matricula);

                if (uoFnc != null) {
                    // se retornou remoto, entao criamos o fnc localmente com
                    funcionario = new Funcionario(uoFnc);

                    String senha = ServiceLocalFactory.getFuncionarioServiceLocal().buscarRemotoSenhaIntranet(funcionario.getId(), Constantes.EMPRESA_DEFAULT);
                    if (senha == null) {
                        LogMachine.getInstancia().logErro("Senha da intranet do funcionário "+funcionario.toString()+" é nula...", this.getClass().getName(), "registrarPonto");
                    }
                    funcionario.setSenhaintranet(senha);

                    // persiste o fnc no nosso banco local
                    ServiceLocalFactory.getFuncionarioServiceLocal().salvar(funcionario);
                } else {
                    // funcionario nao existe mesmo...
                    String msg = "Matrícula " + String.valueOf(matricula) + " não encontrada.";
                    exibirMensagem(msg);
                    LogMachine.getInstancia().logErro(msg, this.getClass().getName(), "registrarPonto", null, null, null, TipoOperacao.ERROMATRICULANAOENCONTRADA);
                    return;
                }
            }

            // atualiza a referencia do funcionario, pois o mesmo pode ter sido alterado em um job
            ServiceLocalFactory.getFuncionarioServiceLocal().refresh(funcionario);

            // verifica se o funcionário não é isento de digitais (se for isento, ignora sincronização de digitais)
            if (!funcionario.getIsentadigital()) {
                if ((funcionario.getDigitais() == null) || (funcionario.getDigitais().size() == 0)) {

                    // nao ha digitais localmente, busca via remotamente e grava localmente
                    // *******************************************************************************
                    // ******** OBS.: ESTE PROCESSO NAO PODE DEMORAR MAIS QUE 3 SEGUNDOS ********
                    // *******************************************************************************
                    // cria objeto FuncionarioTO que servira de base para a busca de digitais remotamente
                    FuncionarioTO funcTO = new FuncionarioTO();
                    funcTO.setEmpresa(Constantes.EMPRESA_DEFAULT);
                    funcTO.setMatricula(funcionario.getId());
                    funcTO.setNome(funcionario.getNome());

                    // faz a busca de digitais remotamente
                    List<DigitalTO> digitais = ServiceLocalFactory.getDigitalServiceLocal().buscarRemotoPorFuncionario(funcTO);

                    if (digitais.size() == 0) {
                        // nao ha digitais remotamente, nao eh possivel registrar ponto...
                        exibirMensagem("Não há digitais cadastradas para esta matrícula!");
                        LogMachine.getInstancia().logErro("Não há digitais cadastradas para esta matrícula!", this.getClass().getName(), "registrarPonto", funcionario, null, null, TipoOperacao.ERRODIGITAISNAOCADASTRADAS);
                        return;
                    } else {
                        // ha digitais remotamente, grava local, permite que o ponto seja registrado
                        funcionario.setDigitais(new ArrayList<Digital>());

                        for (DigitalTO digitalTO : digitais) {
                            Digital d = new Digital(digitalTO.getDataCriacao(), digitalTO.getDataModificacao(), digitalTO.getImagem(), digitalTO.getImagemProcessada(), funcionario, true);
                            funcionario.getDigitais().add(d);
                        }

                        ServiceLocalFactory.getFuncionarioServiceLocal().salvar(funcionario);
                    }

                    digitais = new ArrayList<>();
                }
            }
            
            // verifica se o funcionario é isento de digital e solicita senha da intranet
            if (funcionario.getIsentadigital()) {
                String senha = funcionario.getSenhaintranet() != null ? funcionario.getSenhaintranet() : "";

                SenhaController controllerSenha = Main.showTelaSenha();

                if (controllerSenha.getSenhaInformada()) {
                    String senhaInformada = controllerSenha.getSenha();
                    if (!senha.equals(DigestUtils.md5Hex(senhaInformada))) {
                        JFXUtil.showErrorMessage("Senha intranet!", "Senha Inválida. Tente novamente ou contacte o suporte se o erro persistir.");
                        LogMachine.getInstancia().logErro("Senha inválida ao registrar ponto com isenção de digital. Senha informada: " + DigestUtils.md5Hex(senhaInformada) + ", senha da intranet atual: " + senha, this.getClass().getName(), "registrarPonto", funcionario, null, null, TipoOperacao.ERROVALIDACAOSENHAINTRANET);
                        return;
                    } else {
                        LogMachine.getInstancia().logInfo("Senha válida ao iniciar batimento de ponto com isenção de digital. Senha informada: " + DigestUtils.md5Hex(senhaInformada), this.getClass().getName(), "registrarPonto", funcionario, null, null, TipoOperacao.VALIDACAOSENHAINTRANET);
                    }
                } else {
                    JFXUtil.showErrorMessage("Senha intranet!", "Senha não informada. Tente novamente, informando a senha da intranet!");
                    return;
                }
            }

            LogMachine.getInstancia().logInfo("Batimento de ponto iniciado", this.getClass().getName(), "registrarPonto", funcionario, null, null, TipoOperacao.BATIMENTOINICIADO);

            final Funcionario f = funcionario;

            Main.showTelaDigital(f, ModoTelaDigital.VERIFICACAO);

        } catch (Exception ex) {
            ex.printStackTrace();
            exibirMensagem("Não foi possível consultar a matrícula no banco de dados!");
            LogMachine.getInstancia().logErro("Não foi possível consultar a matrícula no banco de dados!" + " - Matrícula informada: " + String.valueOf(matricula), this.getClass().getName(), "registrarPonto", null, null, null, TipoOperacao.ERROBUSCAMATRICULA);
        }
    }

    private void exibirMensagem(String mensagem) {
        JFXUtil.showWarningMessage("", mensagem);
    }

    private void verificarAtualizacao() throws Exception {
        
        exibirMensagem("O sistema vai verificar se existem atualizações...");
        
        String versaoAtual = Constantes.CLIENTE_VERSAO;

        // obter remotamente as novidades e a versão atual
        UpdateInfoTO updateInfo = ServiceRemoteFactory.getUpdateServiceRemoto().getUpdateInfo();
        String novidades = updateInfo.getNovidades();
        String versao = updateInfo.getVersao();

        if (versao!=null && !versao.equals(versaoAtual)) {

            exibirMensagem("Versão "+versao+" disponível.\n\nNovidades: "+novidades+"\n\nPressione OK para continuar e aguarde.");

            UpdateAppJob.getInstancia().execute(null);

        }                        
    }

    public static void reiniciar(Boolean falha, String msg) throws Exception {
        
        try {
            
            if (falha) {
                erroSair("Erro", msg);
            } else {
                sair();
            }

        } catch (Exception ex) {
            throw new Exception(ex);
        }        

    }
    
    private static void erroSair(String sumary, String mesage) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                AudioUtils.tocarAudio(AudioUtils.ARQUIVO_ERRO);

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText(sumary);
                alert.setContentText(mesage);
                alert.showAndWait();

                sair();

            }
        });
    }
    
    private static void sair() {
        try {
            String workingDir = Paths.get(".").toAbsolutePath().normalize().toString()+File.separator;
            System.out.println("workingDir = "+workingDir);

            if (!workingDir.contains("spe")) {
                workingDir = "c:\\sistemas\\spe-javafx\\";
                System.out.println("workingDir = "+workingDir);
            }

            String app = "spe_update.bat";

            ProcessBuilder pb = new ProcessBuilder(workingDir+app, "spe.bat", "RESTART");

            pb.directory(new File(workingDir));
            File log = new File("updateAppJob.log");

            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
            Process p = pb.start();

            assert pb.redirectInput() == ProcessBuilder.Redirect.INHERIT;
            assert pb.redirectOutput().file() == log;
            assert p.getInputStream().read() == -1;

            // logar fechamento da aplicação atual
            //   necessita de um mecanismo para avisar os outros jobs que vai fechar...
            LogMachine.getInstancia().logInfo("Fechando aplicação...", PrincipalController.class.getName(), "reiniciar");

            Platform.exit();
            System.exit(0);
        } catch(Exception e) {
            e.printStackTrace();            
        }
    }

}
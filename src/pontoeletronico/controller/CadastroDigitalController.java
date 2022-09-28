package pontoeletronico.controller;

import pontoeletronico.util.JFXUtil;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import br.jus.tjms.comuns.exceptions.ServiceException;
import br.jus.tjms.pontoeletronico.client.Constantes;
import br.jus.tjms.pontoeletronico.to.DigitalTO;
import br.jus.tjms.pontoeletronico.to.FuncionarioTO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import pontoeletronico.bean.Digital;
import pontoeletronico.bean.Funcionario;
import pontoeletronico.factory.ServiceLocalFactory;
import pontoeletronico.jobs.AtualizarFuncionariosJob;
import pontoeletronico.jobs.EnviarDigitaisJob;
import pontoeletronico.jobs.EnviarLogJob;
import pontoeletronico.jobs.EnviarPontosJob;
import pontoeletronico.jobs.EnviarTodasDigitaisJob;
import pontoeletronico.jobs.ReceberDigitaisJob;
import pontoeletronico.log.LogMachine;
import pontoeletronico.tipo.TipoOperacao;

import pontoeletronico.util.Utils;

public class CadastroDigitalController implements Initializable {

    @FXML
    private Button btPesquisar;
    @FXML
    private Button btSair;
    @FXML
    private Button btNovaDigital;
    @FXML
    private Button btApagarDigital;
    @FXML
    private Button btSincronizarDados;
    @FXML
    private CheckBox checkAtualizarDigitais;

    @FXML
    private TextField edtMatricula;
    @FXML
    private TextField edtNome;
    @FXML
    private TextField edtLotacao;

    @FXML
    private TableView<Digital> gridDigitais;
    @FXML
    private TableColumn<TableView<Digital>, Integer> colunaId;
    @FXML
    private TableColumn<TableView<Digital>, String> colunaDataCriacao;
    @FXML
    private TableColumn<TableView<Digital>, ImageView> colunaDigital;

    @FXML
    private TextArea txtMensagem;

    @FXML
    private ProgressBar barraProgresso;

    private Funcionario funcionario;

    private Integer matriculaAdm;
    private Integer empresaAdm;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        barraProgresso.setVisible(false);

        limparGridDigitais();

        gridDigitais.setDisable(true);

        btNovaDigital.setDisable(true);
        btApagarDigital.setDisable(true);

        colunaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colunaDataCriacao.setCellValueFactory(new PropertyValueFactory<>("dataCriacaoFormatada"));
        colunaDigital.setCellValueFactory(new PropertyValueFactory<>("imagemView"));
        colunaDigital.setPrefWidth(200);

        gridDigitais.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

    }

    @FXML
    public void actionMatriculaKeyPressed(KeyEvent event) {

        if (event.getCode().equals(KeyCode.ENTER)) {
            pesquisarMatricula();
        } else if (event.getCode().equals(KeyCode.ESCAPE)) {
            btSair.fire();
        }

    }

    @FXML
    public void actionPesquisar(ActionEvent event) {
        pesquisarMatricula();
    }

    @FXML
    public void actionNovaDigital(ActionEvent event) {
        inserirNovaDigital();
    }

    @FXML
    public void actionApagarDigital(ActionEvent event) {
        apagarDigital(true);
    }

    @FXML
    public void actionSincronizar(ActionEvent event) {
        sincronizarDados();
    }

    private void setProgresso(Integer etapa, Integer total) {
        barraProgresso.setProgress(100l / Double.valueOf(total) * Double.valueOf(etapa) / 100l);
    }

    private void sincronizarDados() {

        if (!ServiceLocalFactory.getConfiguracaoService().isConfigurado()) {
            exibirMensagem("Erro: configuração de localidade não definida!", false, true);
            return;
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                barraProgresso.setVisible(true);

                btSincronizarDados.setDisable(true);
                checkAtualizarDigitais.setDisable(true);

                Integer erros = 0;

                try {
                    Thread.sleep(250);
                } catch (Exception e) {
                }

                exibirMensagem("Iniciando sincronização de dados...", false, false);

                try {
                    Thread.sleep(250);
                } catch (Exception e) {
                }

                // envia as digitais locais para o servidor                
                if (checkAtualizarDigitais.isSelected()) {
                    exibirMensagem("Iniciando envio de todas as digitais...", false, false);
                    try {
                        EnviarTodasDigitaisJob.getInstancia().execute(null);
                        exibirMensagem("Digitais enviadas...", false, false);
                    } catch (Exception e) {
                        erros++;
                        e.printStackTrace();
                        exibirMensagem("Erro ao enviar digitais: " + e.getMessage(), false, true);
                        LogMachine.getInstancia().logErro(e.getMessage(), this.getClass().getName(), "btnSincronizarDadosActionPerformed");
                    }
                } else {
                    exibirMensagem("Iniciando envio de digitais...", false, false);

                    // envia as digitais do funcionário selecionado...
                    if (getFuncionario() != null) {

                        try {

                            int ndigitais = 0;

                            ServiceLocalFactory.getFuncionarioServiceLocal().refresh(getFuncionario());

                            if ((getFuncionario().getDigitais() != null) && (getFuncionario().getDigitais().size() > 0)) {

                                List<Funcionario> listafnc = new ArrayList<Funcionario>();

                                for (Digital digital : getFuncionario().getDigitais()) {
                                    if (listafnc.indexOf(digital.getFuncionario()) == -1) {
                                        listafnc.add(digital.getFuncionario());
                                    }
                                }

                                List<FuncionarioTO> listafncTO = new ArrayList<FuncionarioTO>();

                                for (Funcionario funcionariolocal : listafnc) {

                                    FuncionarioTO funcionarioTO = new FuncionarioTO();
                                    funcionarioTO.setEmpresa(Constantes.EMPRESA_DEFAULT);
                                    funcionarioTO.setMatricula(funcionariolocal.getId());
                                    funcionarioTO.setNome(funcionariolocal.getNome());
                                    funcionarioTO.setDigitais(new ArrayList<DigitalTO>());

                                    for (Digital digital : funcionariolocal.getDigitais()) {
                                        DigitalTO digitalTO = new DigitalTO();
                                        digitalTO.setDataCriacao(digital.getDataCriacao());
                                        digitalTO.setDataModificacao(digital.getDataModificacao());
                                        digitalTO.setImagem(digital.getImagem());
                                        digitalTO.setImagemProcessada(digital.getImagemProcessada());
                                        digitalTO.setMatricula(funcionarioTO.getMatricula());
                                        digitalTO.setEmpresa(funcionarioTO.getEmpresa());
                                        funcionarioTO.getDigitais().add(digitalTO);
                                        ndigitais++;
                                    }

                                    listafncTO.add(funcionarioTO);
                                }

                                // envia para o serviço remoto
                                ServiceLocalFactory.getDigitalServiceLocal().gravarRemotoPorFuncionario(listafncTO);

                                String msg = ndigitais + " digital(is) do funcionário " + getFuncionario() + " enviada(s).";

                                exibirMensagem(msg, false, false);

                                LogMachine.getInstancia().logInfo(msg, this.getClass().getName(), "btnSincronizarDadosActionPerformed");

                                Utils.limpaLista(listafncTO);
                                Utils.limpaLista(listafnc);
                            }

                        } catch (Exception e) {
                            erros++;
                            e.printStackTrace();
                            exibirMensagem("Erro ao enviar digitais do funcionário " + getFuncionario() + ": " + e.getMessage(), false, true);
                            LogMachine.getInstancia().logErro(e.getMessage(), this.getClass().getName(), "btnSincronizarDadosActionPerformed");
                        }
                    }

                    try {
                        EnviarDigitaisJob.getInstancia().execute(null);
                        exibirMensagem("Digitais enviadas...", false, false);
                    } catch (Exception e) {
                        erros++;
                        e.printStackTrace();
                        exibirMensagem("Erro ao enviar digitais: " + e.getMessage(), false, true);
                        LogMachine.getInstancia().logErro(e.getMessage(), this.getClass().getName(), "btnSincronizarDadosActionPerformed");
                    }
                }

                setProgresso(1, 5);

                try {
                    Thread.sleep(250);
                } catch (Exception e) {
                }

                // recebe as digitais do servidor e grava localmente        
                if (checkAtualizarDigitais.isSelected()) {
                    exibirMensagem("Iniciando recepção de digitais...", false, false);
                    try {
                        ReceberDigitaisJob.getInstancia().execute(null);
                        exibirMensagem("Digitais recebidas...", false, false);
                    } catch (Exception e) {
                        erros++;
                        e.printStackTrace();
                        exibirMensagem("Erro ao receber digitais: " + e.getMessage(), false, true);
                        LogMachine.getInstancia().logErro(e.getMessage(), this.getClass().getName(), "btnSincronizarDadosActionPerformed");
                    }
                }

                setProgresso(2, 5);

                try {
                    Thread.sleep(250);
                } catch (Exception e) {
                }

                // envia registro de ponto
                exibirMensagem("Iniciando envio de registro de ponto...", false, false);
                try {
                    EnviarPontosJob.getInstancia().execute(null);
                    exibirMensagem("Registro de ponto enviado...", false, false);
                } catch (Exception e) {
                    erros++;
                    e.printStackTrace();
                    exibirMensagem("Erro ao enviar registro de ponto: " + e.getMessage(), false, true);
                    LogMachine.getInstancia().logErro(e.getMessage(), this.getClass().getName(), "btnSincronizarDadosActionPerformed");
                }

                setProgresso(3, 5);

                try {
                    Thread.sleep(250);
                } catch (Exception e) {
                }

                //atualiza dados de funcionarios locais
                if (checkAtualizarDigitais.isSelected()) {
                    exibirMensagem("Iniciando atualização de dados de funcionários...", false, false);
                    try {
                        AtualizarFuncionariosJob.getInstancia().execute(null);
                        exibirMensagem("Dados de funcionários atualizados...", false, false);
                    } catch (Exception e) {
                        erros++;
                        e.printStackTrace();
                        exibirMensagem("Erro ao atualizar dados de funcionários: " + e.getMessage(), false, true);
                        LogMachine.getInstancia().logErro(e.getMessage(), this.getClass().getName(), "btnSincronizarDadosActionPerformed");
                    }
                } else {
                    // atualizar somente o funcionário selecionado
                    if (getFuncionario() != null) {
                        try {
                            ServiceLocalFactory.getFuncionarioServiceLocal().refresh(getFuncionario());

                            FuncionarioTO funcTO = ServiceLocalFactory.getFuncionarioServiceLocal().buscarRemotoPorId(getFuncionario().getId(), Constantes.EMPRESA_DEFAULT);

                            if (funcTO != null) {

                                getFuncionario().setNome(funcTO.getNome());
                                getFuncionario().setLotacao(funcTO.getLotacao()!=null?funcTO.getLotacao():"");
                                getFuncionario().setIsentadigital(funcTO.getIsentaDigital());
                                String senha = ServiceLocalFactory.getFuncionarioServiceLocal().buscarRemotoSenhaIntranet(getFuncionario().getId(), Constantes.EMPRESA_DEFAULT);
                                if (senha == null) {
                                    LogMachine.getInstancia().logErro("Senha da intranet do funcionário "+getFuncionario().toString()+" é nula...", this.getClass().getName(), "btnSincronizarDadosActionPerformed");
                                }
                                getFuncionario().setSenhaintranet(senha);
                                ServiceLocalFactory.getFuncionarioServiceLocal().atualizar(getFuncionario());

                            } else {
                                throw new Exception("Funcionário "+getFuncionario().toString()+" não encontrado remotamente!");
                            }
                        } catch (Exception e) {
                            erros++;
                            e.printStackTrace();
                            String msg = "Erro ao atualizar dados do funcionário "+getFuncionario().toString()+": " + e.getMessage();
                            exibirMensagem(msg, false, true);
                            LogMachine.getInstancia().logErro(msg, this.getClass().getName(), "btnSincronizarDadosActionPerformed");
                        }
                    }
                }

                setProgresso(4, 5);

                try {
                    Thread.sleep(250);
                } catch (Exception e) {
                }

                // envia registro de log
                exibirMensagem("Iniciando envio do registro de log...", false, false);

                try {
                    EnviarLogJob.getInstancia().execute(null);
                    exibirMensagem("Registro de log enviado...", false, false);
                } catch (Exception e) {
                    erros++;
                    e.printStackTrace();
                    exibirMensagem("Erro ao enviar registro de log: " + e.getMessage(), false, true);
                    LogMachine.getInstancia().logErro(e.getMessage(), this.getClass().getName(), "btnSincronizarDadosActionPerformed");
                }

                setProgresso(5, 5);

                try {
                    Thread.sleep(250);
                } catch (Exception e) {
                }

                barraProgresso.setVisible(false);

                btSincronizarDados.setDisable(false);
                checkAtualizarDigitais.setDisable(false);
                exibirMensagem("Sincronização de dados finalizada com "+erros+" erros(s).", false, false);
                    }
        });

        

    }

    private void limparGridDigitais() {
        gridDigitais.getItems().clear();
    }

    // preenche a grid com as digitais do funcionário
    private void listarGridDigitais() {

        limparGridDigitais();

        try {

            ServiceLocalFactory.getFuncionarioServiceLocal().refresh(getFuncionario());

            List<Digital> digitaisCadastradas = getFuncionario().getDigitais();

            if ((digitaisCadastradas != null) && (digitaisCadastradas.size() > 0)) {

                for (Digital digital : digitaisCadastradas) {
                    gridDigitais.getItems().add(digital);
                }
            }

            Utils.limpaLista(digitaisCadastradas);

            gridDigitais.layout();
        } catch (Exception ex) {
            ex.printStackTrace();
            exibirMensagem("Não foi possível buscar as digitais cadastradas: "+ex.getMessage(), false, true);
            LogMachine.getInstancia().logErro(ex.getMessage(), this.getClass().getName(), "listarGridDigitais");
        }
    }

    // localiza uma matrícula
    private boolean buscaMatricula() {

        int matricula = 0;

        try {
            try {
                matricula = Integer.parseInt(edtMatricula.getText());
            } catch (NumberFormatException e) {
                exibirMensagem("Informe uma matrícula válida!", false, true);
                return false;
            }

            ServiceLocalFactory.getFuncionarioServiceLocal().getEntityManager().clear();

            // busca local
            Funcionario funcionariolocal = ServiceLocalFactory.getFuncionarioServiceLocal().buscarPorId(matricula);

            // busca remotamente
            FuncionarioTO funcionarioTO = ServiceLocalFactory.getFuncionarioServiceLocal().buscarRemotoFuncionarioPorId(matricula);

            // se não encontrou o funcionário localmente, cria uma cópia do funcionário remoto
            if (funcionariolocal == null) {

                try {
                    if (funcionarioTO != null) {

                        // se retornou funcionario remoto, então cria localmente o funcionario com uma cópia dos dados retornados remotamente
                        funcionariolocal = new Funcionario(funcionarioTO);

                        String senha = ServiceLocalFactory.getFuncionarioServiceLocal().buscarRemotoSenhaIntranet(funcionariolocal.getId(), Constantes.EMPRESA_DEFAULT);
                        
                        if (senha == null) {
                            LogMachine.getInstancia().logErro("Senha da intranet do funcionário "+funcionariolocal.getNome()+" é nula...", this.getClass().getName(), "buscaMatricula");
                        }

                        funcionariolocal.setSenhaintranet(senha);

                        // persiste o funcionário no nosso banco local
                        ServiceLocalFactory.getFuncionarioServiceLocal().salvar(funcionariolocal);

                        List<DigitalTO> digitais = ServiceLocalFactory.getDigitalServiceLocal().buscarRemotoPorFuncionario(funcionarioTO);

                        //persiste as digitais também, já que o FuncionarioTO pode possuir digitais
                        if ((digitais != null) && (digitais.size() > 0)) {

                            funcionariolocal.setDigitais(new ArrayList<Digital>());

                            for (DigitalTO digitalTO : digitais) {
                                Digital d = new Digital(digitalTO.getDataCriacao(), digitalTO.getDataModificacao(), digitalTO.getImagem(), digitalTO.getImagemProcessada(), funcionariolocal, true);
                                funcionariolocal.getDigitais().add(d);
                            }

                            ServiceLocalFactory.getFuncionarioServiceLocal().salvar(funcionariolocal);
                        }

                        exibirMensagem("Matrícula encontrada: " + String.valueOf(matricula), false, false);

                        // atualiza a referencia do funcionario, pois o mesmo pode ter sido alterado em um job
                        ServiceLocalFactory.getFuncionarioServiceLocal().refresh(funcionariolocal);

                        setFuncionario(funcionariolocal);
                        return true;
                    } else {
                        exibirMensagem("Matrícula não encontrada: " + String.valueOf(matricula), true, false);
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LogMachine.getInstancia().logErro(e.getMessage(), this.getClass().getName(), "buscaMatricula");
                    exibirMensagem("Não foi possível consultar a matrícula no banco de dados: "+e.getMessage(), false, true);
                    return false;
                }
            } else {
                exibirMensagem("Matrícula encontrada: " + String.valueOf(matricula), false, false);

                // atualiza a referencia do funcionario, pois o mesmo pode ter sido alterado em um job
                ServiceLocalFactory.getFuncionarioServiceLocal().refresh(funcionariolocal);

                setFuncionario(funcionariolocal);
                
                return true;
            }
        } catch (ServiceException ex) {
            ex.printStackTrace();
            exibirMensagem("Não foi possível consultar a matrícula no banco de dados: "+ex.getMessage(), false, true);
            return false;
        }
    }

    // chama tela para captura e inserção de digitais
    private void inserirNovaDigital() {
        try {
            Main.showTelaDigital(getFuncionario(), ModoTelaDigital.INSERCAO);
        } catch (Exception e) {
            e.printStackTrace();
            exibirMensagem("Erro ao abrir tela de cadastro de digitais do funcionário " + getFuncionario() + ": " + e.getMessage(), false, true);
            LogMachine.getInstancia().logErro(e.getMessage(), this.getClass().getName(), "inserirNovaDigital");
        }
    }

    private boolean apagarDigital(boolean avisar) {
        // apagar a digital selecionada na grid

        Digital d = (Digital) gridDigitais.getSelectionModel().getSelectedItem();

        if (d != null) {

            try {

                ServiceLocalFactory.getDigitalServiceLocal().remover(d);

                if (avisar) {
                    exibirMensagem("Digital excluída!", true, false);
                }

                gridDigitais.getItems().remove(d);

                ServiceLocalFactory.getFuncionarioServiceLocal().refresh(getFuncionario());

                // marca digitais do funcionario para serem resincronizadas...
                ServiceLocalFactory.getFuncionarioServiceLocal().redefinirEnvioDigitais(getFuncionario());

                ServiceLocalFactory.getFuncionarioServiceLocal().refresh(getFuncionario());

                LogMachine.getInstancia().logInfo("Digital excluída", this.getClass().getName(), "apagarDigital", getFuncionario(), getEmpresaAdm(), getMatriculaAdm(), TipoOperacao.EXCLUSAODIGITAL);

                return true;

            } catch (Exception ex) {
                ex.printStackTrace();
                String msg = "Erro ao excluir digital: " + ex.getMessage();
                exibirMensagem(msg, false, true);
                LogMachine.getInstancia().logErro(msg, this.getClass().getName(), "apagarDigital", getFuncionario(), getEmpresaAdm(), getMatriculaAdm(), TipoOperacao.EXCLUSAODIGITAL);
            }
        } else {
            exibirMensagem("Selecione uma digital!", true, false);
        }

        return false;
    }

    private void alterarDigital() {
        if (apagarDigital(false)) {
            inserirNovaDigital();
        }
    }

    private void pesquisarMatricula() {
        // se econtrar a matrícula, listar as digitais na grid
        // senão, desabilita opções da digital...

        if (buscaMatricula()) {

            // listar digitais
            // habilitar controles de digitais (nova, excluir, alterar)
            edtNome.setText(getFuncionario().getNome());

            if (getFuncionario().getFuncionarioTO() != null) {
                edtLotacao.setText(getFuncionario().getFuncionarioTO().getLotacao());
            } else {
                if (getFuncionario().getLotacao() != null) {
                    edtLotacao.setText(getFuncionario().getLotacao());
                } else {
                    edtLotacao.setText("N/D");
                }
            }

            listarGridDigitais();

            gridDigitais.setDisable(false);
            btNovaDigital.setDisable(false);
            btApagarDigital.setDisable(false);

        } else {
            // limpar grid de digitais
            // desabilitar controles de digitais
            edtNome.setText("");
            edtLotacao.setText("");

            limparGridDigitais();

            gridDigitais.setDisable(true);
            btNovaDigital.setDisable(true);
            btApagarDigital.setDisable(true);
        }
    }

    @FXML
    public void actionSair(ActionEvent event) {
        Main.showTelaConfiguracao();
    }

    private void exibirMensagem(String msg, Boolean aviso, Boolean erro) {        
        if (erro) {
            txtMensagem.setText("Erro: "+msg);
            JFXUtil.showErrorMessageNow("Erro", msg);
        }  else if (aviso) {
            txtMensagem.setText("Aviso: "+msg);
            JFXUtil.showWarningMessageNow("Aviso", msg);
        } else {
            txtMensagem.setText(msg);
        }
    }

    public Integer getEmpresaAdm() {
        return empresaAdm;
    }

    public void setEmpresaAdm(Integer empresaAdm) {
        this.empresaAdm = empresaAdm;
    }

    public Integer getMatriculaAdm() {
        return matriculaAdm;
    }

    public void setMatriculaAdm(Integer matriculaAdm) {
        this.matriculaAdm = matriculaAdm;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

}
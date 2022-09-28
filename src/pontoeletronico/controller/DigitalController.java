package pontoeletronico.controller;

import pontoeletronico.util.JFXUtil;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import javax.swing.SwingUtilities;

import com.griaule.grfingerjava.Template;

import br.jus.tjms.comuns.exceptions.ServiceException;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import pontoeletronico.bean.Digital;
import pontoeletronico.bean.Funcionario;
import pontoeletronico.bean.Ponto;
import pontoeletronico.factory.ServiceLocalFactory;
import pontoeletronico.log.LogMachine;
import pontoeletronico.service.RelogioJobService;
import pontoeletronico.tipo.TipoOperacao;
import pontoeletronico.util.Ambiente;
import pontoeletronico.util.AudioUtils;
import pontoeletronico.util.Fala;
import pontoeletronico.util.SwingUtils;
import pontoeletronico.view.fingerprint.CapturaViewFingerprint;
import pontoeletronico.view.fingerprint.RegistradorFingerprint;

public class DigitalController implements Initializable, CapturaViewFingerprint {

    @FXML
    private ImageView lbImagemOrigem;
    
    @FXML
    private ImageView lbImagemProcessada;

    @FXML
    private TextField inputFuncionario;

    @FXML
    private TextField inputMensagem;

    @FXML
    private TextField inputStatus;

    @FXML
    private Text inputScaner;
    
    @FXML
    private Button btnFechar;

    private Integer matricula;

    private static DigitalController instancia;
    private Funcionario funcionario;

    private boolean modoAdministrador;
    private boolean modoContinuo;

    private boolean lastInserirAutomaticamente;
    private boolean lastVerificarAutomaticamente;
    private boolean lastIdentificarAutomaticamente;

    private boolean naoRegistrou;
    
    static Boolean bibliotecasInicializadas = false;

    // componentes do SDK Fingerprint
    // objeto que realiza todas as operacoes relacionadas ao SDK
    private RegistradorFingerprint registrador;

    public DigitalController() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        System.out.println("Inicializando DigitalController");
        
        
        if (!bibliotecasInicializadas) {
            try {

                // inicializacao dos componentes do SDK Fingerprint
                String grFingerNativeDirectory = new File(Ambiente.getInstance().getFingerPrintHome()).getAbsolutePath();
                String grFingerNativeDirectoryDefault = new File("C:/Arquivos de programas/Griaule/Fingerprint SDK Java 2007/bin").getAbsolutePath();

                try {
                    LogMachine.getInstancia().logInfo("Tentando carregar lib Fingerprint em " + grFingerNativeDirectory, DigitalController.class.getName(), "CapturaViewFingerprintImpl");
                    try {
                        RegistradorFingerprint.setFingerprintSDKNativeDirectory(grFingerNativeDirectory);
                    } catch (UnsatisfiedLinkError ulex) {
                        ulex.printStackTrace();
                        throw new Exception("Bibliotecas do SDK Fingerprint não encontradas em " + grFingerNativeDirectory + ": " + ulex.getMessage());
                    }
                } catch (Exception e1) {
                    LogMachine.getInstancia().logErro("Falha ao tentar carregar lib Fingerprint em " + grFingerNativeDirectory + ": " + e1.getMessage(), DigitalController.class.getName(), "CapturaViewFingerprintImpl");
                    LogMachine.getInstancia().logInfo("Tentando carregar lib Fingerprint em " + grFingerNativeDirectoryDefault, DigitalController.class.getName(), "CapturaViewFingerprintImpl");

                    try {
                        RegistradorFingerprint.setFingerprintSDKNativeDirectory(grFingerNativeDirectoryDefault);
                    } catch (UnsatisfiedLinkError ulex) {
                        throw new Exception("Bibliotecas do SDK Fingerprint não encontradas em " + grFingerNativeDirectoryDefault + ": " + ulex.getMessage());
                    }
                }

                bibliotecasInicializadas = true;
                
            } catch (Exception e) {
                e.printStackTrace();

                String msg = "Não foi possível carregar as bibliotecas da leitora. \nContacte o suporte técnico.";

                JFXUtil.showErrorMessage("Erro", msg);

                LogMachine.getInstancia().logErro(msg, DigitalController.class.getName(), "CapturaViewFingerprintImpl");

                instancia = null;
                
                bibliotecasInicializadas = false;

                return;
            }
        }

        lbImagemOrigem.setImage(null);
        lbImagemProcessada.setImage(null);
        instancia = this;

    }

    public void setMatricula(Integer matricula) {
        this.matricula = matricula;
    }

    @FXML
    public void actionSair(ActionEvent event) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                JFXUtil.hideWindow(event);
            }
        });        
        
    }
    
    @FXML
    public void actionKeyPressed(KeyEvent event) {
	if (event.getCode() == KeyCode.ESCAPE || event.getSource().equals(btnFechar)) {
            JFXUtil.hideWindow(event);
	}
    }    

    @Override
    public boolean isNaoRegistrou() {
        return naoRegistrou;
    }

    @Override
    public void setNaoRegistrou(boolean naoRegistrou) {
        this.naoRegistrou = naoRegistrou;

    }

    @Override
    public void setVisible(boolean b) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;

    }

    @Override
    public void exibirMensagem(String msg) {
        inputMensagem.setText(msg);

    }

    @Override
    public void setDisplayLabelScannerAtual(String scanner) {
        inputScaner.setText(scanner);

    }

    @Override
    public void setDisplayIconeImagemOrigem(ImageIcon iconeImagemOrigem) {
        throw new RuntimeException("Método não implementado");
    }

    @Override
    public Label getLabelExibirIconeImagemOrigem() {
        throw new RuntimeException("Método não implementado");
    }

    @Override
    public Label getLabelExibirIconeImagemProcessadaOrigem() {
        throw new RuntimeException("Método não implementado");
    }

    @Override
    public void ativarModoVerificacao() {

        getInputFuncionario().setText("");
        // verifico se o funcionario eh isento de digital...
        if (this.funcionario.getIsentadigital()) {
            final DigitalController _this = this;

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {

                    try {
                        lbImagemOrigem.setImage(null);
                        lbImagemProcessada.setImage(null);

                        inputStatus.setText("");

                        String dataHora = DateFormat.getTimeInstance().format(RelogioJobService.getInstancia().getDataHora());
                        Ponto p = ServiceLocalFactory.getPontoServiceLocal().registraPonto(_this.getFuncionario().getId(), null, null, false);
                        
                        if (p != null) {

                            LogMachine.getInstancia().logInfo("Ponto registrado às " + dataHora, this.getClass().getName(), "ativarModoVerificacao", getFuncionario(), null, null, TipoOperacao.PONTOREGISTRADO);

                            setStatus("Ponto registrado às " + dataHora, false, false);
                            getInputFuncionario().setText(funcionario.getNome());

                            JFXUtil.showConfirmationMessage("Ponto Registrado", "Caro(a) " + _this.getFuncionario().getNome() + ",\nseu ponto foi registrado em " + dataHora + ".\n\nPor favor, pressione <ENTER> para concluir!");
                        }
                        try {
                            Thread.sleep(200);
                        } catch (Exception e2) {
                        }

                        // para a captura, senao fica registrando os eventos
                        // mesmo que a tela esteja fechada
                        _this.pararCaptura();
                        // fecha a tela (mas nao destroy a instancia)
                        _this.dispose();

                    } catch (ServiceException e) {

                        e.printStackTrace();

                        final String message = e.getMessage();

                        LogMachine.getInstancia().logErro("Ponto não registrado: " + message, this.getClass().getName(), "ativarModoVerificacao", getFuncionario(), null, null, TipoOperacao.PONTONAOREGISTRADO);

                        setStatus("Ponto não registrado. Tente Novamente", true, false);
                        exibirMensagem("Ponto não registrado: " + message);

                        if ((_this.getFuncionario() != null) && (_this.getFuncionario().getNome() != null)) {
                            JFXUtil.showErrorMessage("Erro ao Registrar Ponto", "Caro(a) " + _this.getFuncionario().getNome() + ", houve um erro ao registrar seu ponto.\nPor favor, pressione <ENTER> para sair!\n\nTente novamente ou contacte o suporte se o erro persistir.");
                        } else {
                            JFXUtil.showErrorMessage("Erro ao Registrar Ponto", "Houve um erro ao registrar o ponto.\nPor favor, pressione <ENTER> para sair!\n\nTente novamente ou contacte o suporte se o erro persistir.");

                        }

                        try {
                            Thread.sleep(200);
                        } catch (Exception e2) {
                        }

                        _this.pararCaptura();
                        _this.dispose();

                    }
                }
            });

        } else {

            // ativar o modo de verificacao (registro de ponto)
            if (iniciarCaptura(false, true, false)) {

                // limpa as imagens de digitais...
                lbImagemOrigem.setImage(null);
                lbImagemProcessada.setImage(null);

                setStatus("Aguardando digital... Posicione o dedo no sensor.", false, false);
                getInputFuncionario().setText(funcionario.getNome());

            } else {

                setStatus("Inativo", false, false);
                getInputFuncionario().setText("");
            }
        }

    }

    protected void dispose() {
        /*Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Main.changeScreen(Screens.PRINCIPAL);
            }
        });*/
        
        btnFechar.fire();
    }

    @Override
    public void ativarModoInsercao() {
        // ativar o modo de insercao
        inputFuncionario.setText("");
        if (iniciarCaptura(true, false, false)) {

            // limpa as imagens de digitais...
            lbImagemOrigem.setImage(null);
            lbImagemProcessada.setImage(null);
            setStatus("Aguardando digital... Posicione o dedo no sensor.", false, false);
            inputFuncionario.setText(funcionario.getNome());
        } else {
            setStatus("Inativo", false, false);
            inputFuncionario.setText("");

        }

    }

    @Override
    public void ativarModoIdentificacao() {
        // ativar o modo de identificacao
        if (iniciarCaptura(false, false, true)) {
            defineControlesModoIdentificacao();
        } else {
            setStatus("Inativo", false, false);
            inputFuncionario.setText("");
        }
    }

    @Override
    public void inserirDigital(BufferedImage imagemOrigem, Template template) {
        try {

            Digital digital = new Digital();

            try {

                digital.setImagem(SwingUtils.resizeImageIconAsJPG(new ImageIcon(imagemOrigem), 100));

                digital.setImagemProcessada(template.getData());

                digital.setFuncionario(getFuncionario());

                digital.setDataCriacao(RelogioJobService.getInstancia().getDataHora());
                digital.setDataModificacao(RelogioJobService.getInstancia().getDataHora());

                digital.setEnviado(false);

            } catch (Exception exception) {
                LogMachine.getInstancia().logWarning("Falha ao inserir digital do funcionário " + getFuncionario() + ": " + exception.getMessage(), this.getClass().getName(), "inserirDigital");
            }

            ServiceLocalFactory.getDigitalServiceLocal().salvar(digital);
            // eh necessario dar refresh no objeto "pai" das digitais, senao o
            // mecanismo de cache do toplink nao sabe que houve alteracao nos objetos "filhos"...
            ServiceLocalFactory.getFuncionarioServiceLocal().refresh(getFuncionario());

            LogMachine.getInstancia().logInfo("Digital cadastrada com sucesso!", this.getClass().getName(), "inserirDigital", getFuncionario(), null, null, TipoOperacao.CADASTRODIGITAL);

            setStatus("Digital cadastrada com sucesso!", false, false);

            try {
                Thread.sleep(2500);
            } catch (Exception e2) {
            }

            pararCaptura();
            this.dispose();

        } catch (Exception e) {
            e.printStackTrace();

            final String message = "Digital não cadastrada: " + e.getMessage();

            LogMachine.getInstancia().logErro(message, this.getClass().getName(), "inserirDigital", getFuncionario(), null, null, TipoOperacao.CADASTRODIGITAL);

            setStatus(message, true, false);
            exibirMensagem(message);

            try {
                Thread.sleep(2500);
            } catch (Exception e2) {
            }

            pararCaptura();
            this.dispose();
        }

    }

    @Override
    public void verificarDigital(BufferedImage imagemOrigem, Template template) {
        // validar a digital, informar o usuario, registrar o ponto...
        naoRegistrou = false;

        try {
            Digital digital = new Digital();

            try {
                digital.setImagemProcessada(template.getData());
            } catch (Exception exception) {
                LogMachine.getInstancia().logWarning("Falha ao verificar digital do funcionário:" + exception.getMessage(), this.getClass().getName(), "verificarDigital", getFuncionario(), null, null, TipoOperacao.ERROVERIFICACAODIGITAL);
            }

            String dataHora = DateFormat.getTimeInstance().format(RelogioJobService.getInstancia().getDataHora());

            final Date dataHoraRegistro = RelogioJobService.getInstancia().getDataHora();

            Ponto p = ServiceLocalFactory.getPontoServiceLocal().registraPonto(getFuncionario().getId(), digital, registrador.getComparador(), true);
            
            if (p != null) {

                LogMachine.getInstancia().logInfo("Ponto registrado às " + dataHora, this.getClass().getName(), "verificarDigital", getFuncionario(), null, null, TipoOperacao.PONTOREGISTRADO);

                setStatus("Ponto registrado às " + dataHora, false, false);
            }
            
            try {
                Thread.sleep(2000);
            } catch (Exception e2) {
            }

            // para a captura, senao fica registrando os eventos mesmo que a tela esteja fechada
            pararCaptura();
            // fecha a tela (mas nao destroy a instancia)
            this.dispose();

        } catch (ServiceException e) {

            e.printStackTrace();

            final String message = "Ponto não registrado: " + e.getMessage();

            LogMachine.getInstancia().logErro(message, this.getClass().getName(), "verificarDigital", getFuncionario(), null, null, TipoOperacao.PONTONAOREGISTRADO);

            setStatus(message, true, false);
            exibirMensagem(message);

            if ((getFuncionario() != null) && (getFuncionario().getNome() != null)) {
                naoRegistrou = true;
                JFXUtil.showErrorMessage("Erro", "Caro(a) " + getFuncionario().getNome() + ", houve um erro ao registrar seu ponto.\nPor favor, pressione <ENTER> para sair!\n\nTente novamente ou contacte o suporte se o erro persistir.");
            } else {
                naoRegistrou = true;
                JFXUtil.showErrorMessage("Erro", "Houve um erro ao registrar o ponto.\nPor favor, pressione <ENTER> para sair!\n\nTente novamente ou contacte o suporte se o erro persistir.");
            }

            try {
                Thread.sleep(200);
            } catch (Exception e2) {
            }

            pararCaptura();
            this.dispose();

        }

    }

    @Override
    public void identificarDigital(BufferedImage imagemOrigem, Template template) {
        // identificar o funcionario pela digital e registrar o ponto

        try {

            Digital digitalInformada = new Digital();

            try {

                digitalInformada.setImagemProcessada(template.getData());
            } catch (Exception exception) {
                LogMachine.getInstancia().logWarning("Falha ao identificar digital do funcionário: " + exception.getMessage(), this.getClass().getName(), "identificarDigital");
            }

            boolean validou = false;

            Digital digitalIdentificada = null;

            List<Digital> digitaisCadastradas = ServiceLocalFactory.getDigitalServiceLocal().buscarTodos();

            if (digitaisCadastradas.size() == 0) {
                throw new ServiceException("Digitais não cadastradas!");
            }

            for (Digital digitalCadastrada : digitaisCadastradas) {

                // identifica o template da digital cadastrada
                validou = ServiceLocalFactory.getLeitoraService().comparaDigital(digitalInformada, digitalCadastrada, registrador.getComparador());

                if (validou) {
                    digitalIdentificada = digitalCadastrada;
                    break;
                }
            }

            if (!validou) {

                throw new ServiceException("Digital inválida!");

            } else {
                setFuncionario(digitalIdentificada.getFuncionario());
                LogMachine.getInstancia().logInfo("Funcionário identificado: " + digitalIdentificada.getFuncionario().getId() + " - " + digitalIdentificada.getFuncionario().getNome(), this.getClass().getName(), "identificarDigital", getFuncionario(), null, null, TipoOperacao.IDENTIFICACAODIGITAL);
                setStatus(("Funcionário identificado: " + digitalIdentificada.getFuncionario().getId() + " - " + digitalIdentificada.getFuncionario().getNome()), false, false);

                try {
                    Thread.sleep(1000);
                } catch (Exception e2) {
                }

                String dataHora = DateFormat.getTimeInstance().format(RelogioJobService.getInstancia().getDataHora());

                Ponto p = ServiceLocalFactory.getPontoServiceLocal().registraPonto(getFuncionario().getId(), digitalIdentificada, registrador.getComparador(), false);
                
                if (p != null) {
                    LogMachine.getInstancia().logInfo("Ponto registrado às " + dataHora, this.getClass().getName(), "identificarDigital", getFuncionario(), null, null, TipoOperacao.PONTOREGISTRADO);
                    setStatus("Ponto registrado às " + dataHora, false, false);
                }
            }

            digitaisCadastradas = new ArrayList<>();

            try {
                Thread.sleep(1000);
            } catch (Exception e2) {
            }

            if (isModoContinuo()) {
                setFuncionario(null);
                defineControlesModoIdentificacao();
            } else {
                pararCaptura();
                this.dispose();
            }

        } catch (Exception e) {

            e.printStackTrace();

            final String message = "Funcionário não identificado. Tente novamente.";

            setStatus(message, true, false);
            exibirMensagem(message);

            try {
                Thread.sleep(200);
            } catch (Exception e2) {
            }

            if (isModoContinuo()) {
                setFuncionario(null);
                defineControlesModoIdentificacao();
            } else {
                pararCaptura();
                this.dispose();
            }
        }

    }

    private void defineControlesModoIdentificacao() {
        lbImagemOrigem.setImage(null);
        lbImagemProcessada.setImage(null);
        setStatus("Aguardando digital... Posicione o dedo no sensor.", false, false);
        inputFuncionario.setText("");

    }

    private boolean isModoContinuo() {
        return modoContinuo;
    }

    @Override
    public void exibirImagem(BufferedImage image) {

        WritableImage imagefx = SwingFXUtils.toFXImage(image, null);

        lbImagemOrigem.setImage(imagefx);
        
        lbImagemOrigem.setFitWidth(300);
        lbImagemOrigem.setPreserveRatio(true);
        lbImagemOrigem.setSmooth(true);
        lbImagemOrigem.setCache(true);        

    }

    @Override
    public void exibirImagemProcessada(BufferedImage image) {
        WritableImage imagefx = SwingFXUtils.toFXImage(image, null);

        lbImagemProcessada.setImage(imagefx);        
        lbImagemProcessada.setFitWidth(300);
        lbImagemProcessada.setPreserveRatio(true);
        lbImagemProcessada.setSmooth(true);
        lbImagemProcessada.setCache(true);        

    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    // parar captura em todos os scanners...
    public void pararCaptura() {
        try {
            inputStatus.setText("");
            getInputFuncionario().setText("");

            registrador.finalizar();
        } catch (Exception e) {
            exibirMensagem("Falha ao parar captura: " + e.getMessage());
        }
    }

    public boolean iniciarCaptura(boolean inserirAutomaticamente, boolean verificarAutomaticamente, boolean identificarAutomaticamente) {

        setLastInserirAutomaticamente(inserirAutomaticamente);
        setLastVerificarAutomaticamente(verificarAutomaticamente);
        setLastIdentificarAutomaticamente(identificarAutomaticamente);

        try {
            registrador = RegistradorFingerprint.getInstancia(this, inserirAutomaticamente, verificarAutomaticamente, identificarAutomaticamente);
            exibirMensagem(registrador.getFingerprintSDKVersion());
            exibirMensagem("Scanner iniciado.");

            setModoContinuo(identificarAutomaticamente);

        } catch (Exception e) {
            e.printStackTrace();
            exibirMensagem("Falha ao iniciar captura: " + e.getMessage());
        }

        return true;

    }

    private void setModoContinuo(boolean modoContinuo) {
        this.modoContinuo = modoContinuo;
    }

    private void setLastIdentificarAutomaticamente(boolean identificarAutomaticamente) {
        this.lastIdentificarAutomaticamente = identificarAutomaticamente;

    }

    private void setLastVerificarAutomaticamente(boolean verificarAutomaticamente) {
        this.lastVerificarAutomaticamente = verificarAutomaticamente;

    }

    private void setLastInserirAutomaticamente(boolean inserirAutomaticamente) {
        this.lastInserirAutomaticamente = inserirAutomaticamente;

    }

    public void setStatus(String msg, boolean erro, boolean falar) {
        final String mensagem = msg;
        final boolean _erro = erro;
        final boolean _falar = falar;

        if (_erro) {
            inputStatus.setStyle(" -fx-background-color: red;");
            try {
                Thread.sleep(250);
            } catch (Exception e) {
            }
            AudioUtils.tocarAudio(AudioUtils.ARQUIVO_ERRO);
        } else {
            inputStatus.setStyle(" -fx-background-color: green ;");
            AudioUtils.tocarAudio(AudioUtils.ARQUIVO_OK);
        }

        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
        inputStatus.setText(mensagem);
        try {
            Thread.sleep(750);
        } catch (Exception e) {
        }
        inputStatus.setText("");
        try {
            Thread.sleep(250);
        } catch (Exception e) {
        }
        inputStatus.setText(mensagem);

        if (_falar) {
            try {
                Fala.falar(mensagem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public TextField getInputFuncionario() {
        return inputFuncionario;
    }

    public void setInputFuncionario(TextField inputFuncionario) {
        this.inputFuncionario = inputFuncionario;
    }

    public boolean isModoAdministrador() {
        return modoAdministrador;
    }

    public void setModoAdministrador(boolean modoAdministrador) {
        this.modoAdministrador = modoAdministrador;
    }

    public ImageView getLbImagemOrigem() {
        return lbImagemOrigem;
    }

    public void setLbImagemOrigem(ImageView lbImagemOrigem) {
        this.lbImagemOrigem = lbImagemOrigem;
    }

    public ImageView getLbImagemProcessada() {
        return lbImagemProcessada;
    }

    public void setLbImagemProcessada(ImageView lbImagemProcessada) {
        this.lbImagemProcessada = lbImagemProcessada;
    }

    public TextField getInputMensagem() {
        return inputMensagem;
    }

    public void setInputMensagem(TextField inputMensagem) {
        this.inputMensagem = inputMensagem;
    }

    public TextField getInputStatus() {
        return inputStatus;
    }

    public void setInputStatus(TextField inputStatus) {
        this.inputStatus = inputStatus;
    }

    public Text getInputScaner() {
        return inputScaner;
    }

    public void setInputScaner(Text inputScaner) {
        this.inputScaner = inputScaner;
    }

    public Button getBtnFechar() {
        return btnFechar;
    }

    public void setBtnFechar(Button btnFechar) {
        this.btnFechar = btnFechar;
    }

    public static DigitalController getInstancia() {
        return instancia;
    }

    public static void setInstancia(DigitalController instancia) {
        DigitalController.instancia = instancia;
    }

    public RegistradorFingerprint getRegistrador() {
        return registrador;
    }

    public void setRegistrador(RegistradorFingerprint registrador) {
        this.registrador = registrador;
    }

}
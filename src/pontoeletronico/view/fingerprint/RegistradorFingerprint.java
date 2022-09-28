package pontoeletronico.view.fingerprint;

import br.jus.tjms.comuns.exceptions.ServiceException;
import com.griaule.grfingerjava.*;
import pontoeletronico.factory.ServiceLocalFactory;
import pontoeletronico.log.LogMachine;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;


/**
 *
 * @author marcosbispo
 * 
 * RegistradorFingerprint 
 * 
 * É a classe responsável por registrar os eventos que ocorrem no scanner (dedo colocado, dedo retirado, imagem adquirida, etc)
 * Também é a responsável por atualizar a tela de captura (view) com a imagem da digital e, se for o caso, ordenar a gravação da digital no banco de dados, entre outras coisas...
 * Obs.: baseado no SDK Fingerprint
 * 
 */
public class RegistradorFingerprint implements IStatusEventListener, IImageEventListener, IFingerEventListener {

    private static RegistradorFingerprint instancia;
    private CapturaViewFingerprint mainview;

    private boolean inserirAutomaticamente;
    private boolean verificarAutomaticamente;
    private boolean identificarAutomaticamente;
    
    private boolean ativo;

    // componente do SDK usado para captura/extração/comparação de digitais
    private static MatchingContext comparador;

    // Última imagem (digital) adquirida
    private FingerprintImage fingerprint;
    // Template processado a partir da última digital adquirida
    private Template template;

    public RegistradorFingerprint(CapturaViewFingerprint view, boolean inserirAuto, boolean verificarAuto, boolean identificarAuto) {
        
        setMainview(view);
        setInserirAutomaticamente(inserirAuto);
        setVerificarAutomaticamente(verificarAuto);
        setIdentificarAutomaticamente(identificarAuto);

        // inicializa o SDK
        this.inicializar();
    }

    public static RegistradorFingerprint getInstancia(CapturaViewFingerprint view, boolean inserirAuto, boolean verificarAuto, boolean identificarAuto) {
        if (instancia == null) {
            instancia = new RegistradorFingerprint(view, inserirAuto, verificarAuto, identificarAuto);
        } else {
            instancia.setMainview(view);
            instancia.setInserirAutomaticamente(inserirAuto);
            instancia.setVerificarAutomaticamente(verificarAuto);
            instancia.setIdentificarAutomaticamente(identificarAuto);
            
            // inicializa o SDK
            //instancia.finalizar();
            instancia.inicializar();            
        }

        return instancia;
    }

    /**
     * Inicializa o SDK
     */
    private void inicializar() {
        try {

            if (comparador == null) {
                comparador = new MatchingContext();
            } 

            // inicia captura usando este (this) registrador
            GrFingerJava.initializeCapture(this);
            
            mainview.exibirMensagem("Fingerprint inicializado com sucesso...");
            
            setAtivo(true);

        } catch (Exception e) {
            String msg = "Erro ao inicializar Fingerprint: " + e.getMessage();
            LogMachine.getInstancia().logErro(msg, this.getClass().getName(), "inicializar");
            mainview.exibirMensagem(msg);
        }
    }

    /**
     * Finaliza a captura
     */
    public void finalizar() {
        try {
            setAtivo(false);
            GrFingerJava.finalizeCapture();
        } catch (GrFingerJavaException e) {   
            e.printStackTrace();
            String msg = "Erro ao finalizar Fingerprint: " + e.getMessage();
            LogMachine.getInstancia().logErro(msg, this.getClass().getName(), "finalizar");
        }
    }
    
    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
    
    public CapturaViewFingerprint getMainview() {
        return mainview;
    }

    public void setMainview(CapturaViewFingerprint mainview) {
        this.mainview = mainview;
    }

    /**
     * Evento disparado sempre que um sensor é plugado
     *
     * @see griaule.grFinger.StatusCallBack#onPlug(String)
     */
    @Override
    public void onSensorPlug(String idSensor) {
        // loga o evento
        mainview.exibirMensagem("Sensor " + idSensor + " plugado!");
        
        try {
            // inicia a captura no sensor
            GrFingerJava.startCapture(idSensor, this, this);
        } catch (GrFingerJavaException e) {
            String msg = "Erro ao plugar sensor: " + e.getMessage();
            LogMachine.getInstancia().logErro(msg, this.getClass().getName(), "onSensorPlug");
            mainview.exibirMensagem(msg);
        }
    }

    /**
     * Evento disparado sempre que um sensor é DESplugado
     *
     * @param idSensor
     * @see griaule.grFinger.StatusCallBack#onUnplug(String)
     */
    @Override
    public void onSensorUnplug(String idSensor) {
        // loga o evento
        mainview.exibirMensagem("Sensor " + idSensor + " desplugado!");
        
        try {
            // para a captura no dito cujo...
            GrFingerJava.stopCapture(idSensor);
        } catch (GrFingerJavaException e) {
            String msg = "Erro ao desplugar sensor: " + e.getMessage();
            LogMachine.getInstancia().logErro(msg, this.getClass().getName(), "onSensorUnplug");
            mainview.exibirMensagem(msg);
        }
    }

    /**
     * Evento disparado sempre que uma imagem é capturada no sensor
     * Aqui fazemos o tratamento da imagem que foi capturada pelo scanner...
     *
     * @param idSensor
     * @param fingerprint
     * @see griaule.grFinger.ImageCallBack#onImage(String, griaule.grFinger.FingerprintImage)
     */
    @Override
    public void onImageAcquired(String idSensor, FingerprintImage fingerprint) {
        
        if (isAtivo()) {
            try {

                // loga o evento
                mainview.exibirMensagem("Imagem adquirida no sensor "+idSensor);

                mainview.setDisplayLabelScannerAtual(idSensor);

                this.fingerprint = fingerprint;

                // exibe a imagem capturada na view...
                mainview.exibirImagem(fingerprint);

                // extrai o template a partir da digital capturada
                try {
                    extrairTemplate();
                } catch (Exception ex) {
                    throw new ServiceException("Digital inválida!");
                }

                // verifica modos atuais (inserção, identificação e verificação)
                if (identificarAutomaticamente) {
                    mainview.identificarDigital(fingerprint, template);
                }

                if (inserirAutomaticamente) {
                    mainview.inserirDigital(fingerprint, template);
                }

                if (verificarAutomaticamente) {
                    mainview.verificarDigital(fingerprint, template);
                }

            } catch (ServiceException e) {
                LogMachine.getInstancia().logErro("Erro ao adquirir imagem do scanner: "+ e.getMessage() , this.getClass().getName(), "onImageAcquired");
                mainview.exibirMensagem("RegistradorFingerprint.onImageAcquired: " + e.getMessage());
            }
        }

    }

    /**
     * Evento disparado sempre que um dedo é colocado no sensor
     *
     * @param idSensor
     * @see griaule.grFinger.FingerCallBack#onFingerDown(String)
     */
    @Override
    public void onFingerDown(String idSensor) {
        // loga o evento
        if (isAtivo()) {
            mainview.exibirMensagem("Dedo colocado no sensor "+idSensor);
        }
    }

    /**
     * Evento disparado sempre que um dedo é removido do sensor
     *
     * @param idSensor
     * @see griaule.grFinger.FingerCallBack#onFingerUp(String)
     */
    @Override
    public void onFingerUp(String idSensor) {
        // loga o evento
        if (isAtivo()) {
            mainview.exibirMensagem("Dedo removido do sensor "+idSensor);
        }
    }

    /**
     * Define as cores usadas para exibir os detalhes da digital
     */
    public void setBiometricDisplayColors(
            Color minutiaeColor, Color minutiaeMatchColor,
            Color segmentColor, Color segmentMatchColor,
            Color directionColor, Color directionMatchColor) {
        try {
            GrFingerJava.setBiometricImageColors(
                    minutiaeColor, minutiaeMatchColor,
                    segmentColor, segmentMatchColor,
                    directionColor, directionMatchColor);

        } catch (GrFingerJavaException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retorna a versão e o tipo de licença atual
     * @return 
     */
    public String getFingerprintSDKVersion() {
        try {
            return "Versão do Fingerprint SDK: " + GrFingerJava.getMajorVersion() + "." + GrFingerJava.getMinorVersion() + " - " +
                   "Tipo de licença: '" + (GrFingerJava.getLicenseType() == GrFingerJava.GRFINGER_JAVA_FULL ? "Identification" : "Verification") + "'.";

        } catch (GrFingerJavaException e) {
            return null;
        }
    }

    /**
     * Retorna a imagem da digital atual, sem detalhes biométricos
     * @return 
     */
    public BufferedImage getFingerprint() {
        return this.fingerprint;
    }

    /**
     * Define parâmetros para identificação / verificação
     */
    public void setParameters(int identifyThreshold, int identifyRotationTolerance, int verifyThreshold, int verifyRotationTolorance) {
        try {
            comparador.setIdentificationThreshold(identifyThreshold);
            comparador.setIdentificationRotationTolerance(identifyRotationTolerance);
            comparador.setVerificationRotationTolerance(verifyRotationTolorance);
            comparador.setVerificationThreshold(verifyThreshold);

        } catch (GrFingerJavaException e) {
            String msg = "Erro ao definir parâmetros: " + e.getMessage();
            LogMachine.getInstancia().logErro(msg, this.getClass().getName(), "setParameters");
        }
    }

    /**
     * Retorna o limite de qualidade para verificação
     * @return 
     */
    public int getVerifyThreshold() {
        try {
            return comparador.getVerificationThreshold();
        } catch (GrFingerJavaException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Retorna o limite de rotação para verificação
     * @return 
     */
    public int getVerifyRotationTolerance() {
        try {
            return comparador.getVerificationRotationTolerance();
        } catch (GrFingerJavaException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Retorna o limite de qualidade para identificação
     * @return 
     */
    public int getIdentifyThreshold() {
        try {
            return comparador.getIdentificationThreshold();
        } catch (GrFingerJavaException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Retorna o limite de rotação para identificação
     * @return 
     */
    public int getIdentifyRotationTolerance() {
        try {
            return comparador.getIdentificationRotationTolerance();
        } catch (GrFingerJavaException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Extrai template a partir da imagem atual
     * @throws java.lang.Exception
     */
    public void extrairTemplate() throws Exception {

        try {
            
            // extrai o template
            template = comparador.extract(fingerprint);

            String msg = "Template extraído com sucesso. ";

            //informa qual a qualidade da imagem caturada
            switch (template.getQuality()) {
                case Template.HIGH_QUALITY:
                    msg += "Alta qualidade.";
                    break;
                case Template.MEDIUM_QUALITY:
                    msg += "Média qualidade.";
                    break;
                case Template.LOW_QUALITY:
                    msg += "Baixa qualidade.";
                    break;
            }
            
            mainview.exibirMensagem(msg);

            // exibe a imagem processada
            mainview.exibirImagemProcessada(GrFingerJava.getBiometricImage(template, fingerprint));

        } catch (Exception e) {
            throw new Exception(e);
        }

    }

    /**
     * Define o diretório onde estão as bibliotecas nativas e arquivos de licença do SDK Fingerprint
     * @param nativeDirectory
     * @throws java.lang.Exception
     */
    public static void setFingerprintSDKNativeDirectory(String nativeDirectory) throws Exception {
        File directory = new File(nativeDirectory);
        try {
            GrFingerJava.setNativeLibrariesDirectory(directory);
            GrFingerJava.setLicenseDirectory(directory);
        } catch (GrFingerJavaException e) {
            String msg = "Erro ao definir path do SDK Fingerprint: " + e.getMessage();
            LogMachine.getInstancia().logErro(msg, RegistradorFingerprint.class.getName(), "setFingerprintSDKNativeDirectory");
        }
    }

    public boolean isInserirAutomaticamente() {
        return inserirAutomaticamente;
    }

    public void setInserirAutomaticamente(boolean inserirAutomaticamente) {
        this.inserirAutomaticamente = inserirAutomaticamente;
    }

    public boolean isVerificarAutomaticamente() {
        return verificarAutomaticamente;
    }

    public void setVerificarAutomaticamente(boolean verificarAutomaticamente) {
        this.verificarAutomaticamente = verificarAutomaticamente;
    }

    public boolean isIdentificarAutomaticamente() {
        return identificarAutomaticamente;
    }

    public void setIdentificarAutomaticamente(boolean identificarAutomaticamente) {
        this.identificarAutomaticamente = identificarAutomaticamente;
    }

    // compara duas templates
    public boolean comparar(Template templateInformado, Template templateBase) {
        boolean resultado = false;

        try {
            
            int arg0 = ServiceLocalFactory.getParametroService().getParametros().getNivelToleranciaVerificacao();
            comparador.setVerificationThreshold(arg0);

            resultado = comparador.verify(templateInformado, templateBase);

        } catch (Exception e) {
            LogMachine.getInstancia().logErro(e.getMessage(), this.getClass().getName(), "comparar");
        }

        return resultado;
    }
    
    // prepara para identificação 
    // deve ser chamado antes de identificar, passando como parâmetro a template informada
    public void prepararParaIdentificacao(Template templateInformado) throws GrFingerJavaException {
        comparador.prepareForIdentification(templateInformado);
    }
    
    // identifica uma template (compara a templatebase com a template informada ao preparar para identificação)
    public boolean identificar(Template templateBase) throws GrFingerJavaException {
        return comparador.identify(templateBase);
    }

    public MatchingContext getComparador() {
        return comparador;
    }
    
    public void setComparador(MatchingContext newcomparador) {
        comparador = newcomparador;
    }
    
    
}

package pontoeletronico.controller;

import br.jus.tjms.pontoeletronico.client.Constantes;
import pontoeletronico.util.JFXUtil;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import br.jus.tjms.pontoeletronico.to.UpdateInfoTO;
import br.jus.tjms.pontoeletronico.to.UpdateScriptAcaoTO;
import br.jus.tjms.pontoeletronico.to.UpdateScriptTipoAcao;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.apache.commons.codec.digest.DigestUtils;
import pontoeletronico.bean.Configuracao;
import pontoeletronico.bean.Funcionario;
import pontoeletronico.bean.Parametro;
import pontoeletronico.factory.EntityManagerFactory;
import pontoeletronico.factory.ServiceLocalFactory;
import pontoeletronico.factory.ServiceRemoteFactory;
import pontoeletronico.jobs.AtualizarFuncionariosJob;
import pontoeletronico.jobs.ColetaLixoJob;
import pontoeletronico.jobs.EnviarDigitaisJob;
import pontoeletronico.jobs.EnviarLogJob;
import pontoeletronico.jobs.EnviarPontosJob;
import pontoeletronico.jobs.PontoEletronicoJobs;
import pontoeletronico.jobs.ReceberDigitaisJob;
import pontoeletronico.jobs.RelogioJob;
import pontoeletronico.jobs.UpdateAppJob;
import pontoeletronico.log.LogMachine;
import pontoeletronico.service.ParametroService;
import pontoeletronico.service.RelogioJobService;
import pontoeletronico.util.Ambiente;
import pontoeletronico.util.SenhaDialog;

public class Main extends Application {

    private static Stage stage;
    private static Scene scenePrincipal;
    private static Scene sceneCadastroDigital;
    private static Scene sceneConfigurarArea;
    private static Scene sceneConfigurarDataHora;

    private static Integer matricula;

    private static Class<? extends Main> instanceClass;

    private static Boolean criarBanco = false;
    private static Boolean atualizar = false;
    
    private static FXMLLoader fxmlDigital = null;
    private static VBox panelDigital = null;
    
    private static FXMLLoader fxmlSenha = null;
    private static VBox panelSenha = null;
    
    private static FXMLLoader fxmlLogin = null;
    private static VBox panelLogin = null;

    @Override
    public void start(Stage primaryStage) throws Exception {

        instanceClass = getClass();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {

            }
        });

        iniciaBD();
        
        atualizaBD();
        
        Boolean dataIniciada = iniciaInstanciaData();

        try {

            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.setMaximized(true);

            stage = primaryStage;

            primaryStage.setTitle("Ponto Eletrônico");

            URL location = getClass().getResource("/resources/fxml/principal.fxml");

            FXMLLoader fxmlPrincipal = new FXMLLoader(location);

            BorderPane panelPrincipal = fxmlPrincipal.load();
            scenePrincipal = new Scene(panelPrincipal);

            Parent fxmlConfigurarArea = FXMLLoader.load(getClass().getResource("/resources/fxml/configurar-area.fxml"));
            sceneConfigurarArea = new Scene(fxmlConfigurarArea);

            Parent fxmlCadastrarDigital = FXMLLoader.load(getClass().getResource("/resources/fxml/cadastro-digital.fxml"));
            sceneCadastroDigital = new Scene(fxmlCadastrarDigital);

            Parent fxmlConfigurarDataHora = FXMLLoader.load(getClass().getResource("/resources/fxml/configurar-data-hora.fxml"));
            sceneConfigurarDataHora = new Scene(fxmlConfigurarDataHora);

            if (dataIniciada) {
                
                primaryStage.setScene(scenePrincipal);
                primaryStage.show();
                
            } else {

                SenhaDialog dialog = new SenhaDialog();
                dialog.setTitle("Senha master");
                dialog.setHeaderText("Informe a senha master");                
                Optional<String> result = dialog.showAndWait();
                
                if (result.isPresent()){
                    if (DigestUtils.md5Hex(result.get()).equals(Constantes.SENHAMASTER_DEFAULT)) {
                        primaryStage.setScene(sceneConfigurarDataHora);
                        primaryStage.show();                        
                    } else {
                        JFXUtil.showErrorMessageNow("Senha master", "Senha inválida.");
                        Platform.exit();
                        System.exit(0);
                    }
                } else {
                    JFXUtil.showErrorMessageNow("Senha master", "Senha não informada.");
                    Platform.exit();
                    System.exit(0);
                }
                
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            LogMachine.getInstancia().logErro("Erro ao montar telas do sistema", Main.class.getName(), "start");
            JFXUtil.showErrorMessage("Erros de Configuração", "Erro ao montar telas do sistema: " + ex.getMessage());

            Platform.exit();
            System.exit(0);
        }

    }

    public static Boolean iniciaInstanciaData() {
        Date data;
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            
            // obtendo a data e hora do servidor ejb
            LogMachine.getInstancia().logInfo("Obtendo Data/Hora do Servidor", Main.class.getName(), "start");
            data = ServiceRemoteFactory.getRelogioServiceRemoto().getDataHora();
            LogMachine.getInstancia().logInfo("Data/Hora Retornada: " + sdf.format(data), Main.class.getName(), "start");

            LogMachine.getInstancia().logInfo("Atribuindo Data/Hora Local:" + sdf.format(data), Main.class.getName(), "start");
            
            // atribuindo data e hora no relogio interno
            RelogioJobService.getInstancia().setDataHora(data);

            iniciaJobs(false);
            
            return true;

        } catch (Exception ex) {
            LogMachine.getInstancia().logErro("Erro ao atualizar relogio interno da aplicacao", Main.class.getName(), "main");
            ex.printStackTrace();
            JFXUtil.showErrorMessage("Erro ao conectar ao servidor", "Você precisa configurar a Data/Hora do sistema de Ponto Eletrônico manualmente.");
            return false;
        }
    }

    public static void changeScreen(Screens screen) {

        switch (screen) {
            case PRINCIPAL: {
                stage.setScene(scenePrincipal);
                JFXUtil.maximizar(stage);
                break;
            }
            case CADASTRO_DIGITAIS: {
                stage.setScene(sceneCadastroDigital);
                JFXUtil.maximizar(stage);
                break;
            }
            case CONFIGURAR_AREA: {
                stage.setScene(sceneConfigurarArea);
                JFXUtil.maximizar(stage);
                break;
            }
            case CONFIGURAR_DATAHORA: {
                stage.setScene(sceneConfigurarDataHora);
                break;
            }
            default:
                break;
        }

    }

    public static void main(String[] args) {
        
        LogMachine.getInstancia().logInfo("\n\nIniciando spe... \n", Main.class.getName(), "main");
        
        LogMachine.getInstancia().logInfo("\nVer. atual: "+ Constantes.CLIENTE_VERSAO+"\n\n", Main.class.getName(), "main");
        LogMachine.getInstancia().logInfo("\nVer. BD atual: "+ Constantes.CLIENTE_VERSAOBD+"\n\n", Main.class.getName(), "main");
        
        Constantes.imprimeVersao();
        
        if (args !=null && args.length > 0) {
            
            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("CREATEDB")) {
                    criarBanco = true;        
                }
                if (args[i].equalsIgnoreCase("UPDATE")) {
                    atualizar = true;        
                }
                if (args[i].equalsIgnoreCase("LOCAL")) {
                    ServiceRemoteFactory.setLocal();
                }
                if (args[i].equalsIgnoreCase("HOMOLOGACAO")) {
                    ServiceRemoteFactory.setHomologacao();
                }                
            }
            
        }

        launch(args);
    }

    public static Integer getMatricula() {
        return matricula;
    }

    public static void setMatricula(Integer matricula) {
        Main.matricula = matricula;
    }

    public void iniciaBD() throws Exception {

        if (System.getProperty("derby.system.home") == null) {
            System.setProperty("derby.system.home", Ambiente.getInstance().getDerbyHome());
        }

        LogMachine.getInstancia().logInfo("Inicializando...", Main.class.getName(), "iniciaBD");

        // *** CRIAÇÃO DO BANCO DE DADOS VIA PARAMETRO CREATEDB ***
        // se foi passado o parametro CREATEDB entao cria o banco de dados...
        if (criarBanco) {
            
            try {

                LogMachine.getInstancia().logInfo("Criando banco de dados...", Main.class.getName(), "iniciaBD");

                // cria persistence unit PontoEletronicoPU, forçando a recriação das tabelas e do banco de dados atraves do parametro abaixo:
                // <property name="toplink.ddl-generation" value="drop-and-create-tables"/>
                Properties p = new Properties();
                p.setProperty("toplink.ddl-generation", "drop-and-create-tables");

                // Hibernate
                // p.setProperty("hibernate.hbm2ddl.auto", "create-drop");
                javax.persistence.EntityManagerFactory emf = Persistence.createEntityManagerFactory("PontoEletronicoPU", p);
                EntityManager em = emf.createEntityManager();

                LogMachine.getInstancia().logInfo("Banco de dados criado com sucesso.", Main.class.getName(), "iniciaBD");

                // inicializa parâmetros padrões no novo banco de dados
                Parametro parametro = new Parametro();
                parametro.setId(1);
                parametro.setDiasEnvioDigitais(Constantes.NDIAS_ENVIO_DIGITAIS_DEFAULT);
                parametro.setIntervaloEnvioDigitais(String.valueOf(1 * Constantes.HORA));
                parametro.setIntervaloRecebimentoDigitais(String.valueOf(1 * Constantes.HORA));
                parametro.setIntervaloEnvioRegistroOperacoes(String.valueOf(1 * Constantes.HORA));
                parametro.setIntervaloEnvioRegistroPonto(String.valueOf(1 * Constantes.HORA));
                parametro.setIntervaloSincronizacaoRelogio(String.valueOf(10 * Constantes.MINUTO));
                parametro.setNivelToleranciaVerificacao(Constantes.NIVEL_TOLERANCIA_VERIFICAO_DEFAULT);
                parametro.setSenhaMaster(Constantes.SENHAMASTER_DEFAULT);
                parametro.setVersaoBD(Constantes.CLIENTE_VERSAOBD);
                ServiceLocalFactory.getParametroService().setParametros(parametro);

                LogMachine.getInstancia().logInfo("Parâmetros padrões definidos com sucesso.", Main.class.getName(), "iniciaBD");

            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("Impossível criar o banco de dados: " + e.getMessage());
            }
        }

        // *** CHECAGEM DA EXISTENCIA DO BANCO DE DADOS ***
        // testa a conexao com o banco derby embutido
        try {
            Configuracao c = EntityManagerFactory.getEntityManager().find(Configuracao.class, 1);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Impossível conectar ao banco de dados, verifique se a aplicação já está aberta. \nFalha reportada: " + e.getMessage());
        }
        
    }
    
    private void atualizaBD() throws Exception {
        
        // ler updatescript previamente gravado e executar as ações
        
        if (!ServiceLocalFactory.getParametroService().isParametrizado()) {
            throw new Exception("Não parametrizado!");
        }
        
        UpdateInfoTO updateInfo = obterUpdateInfo();
        
        if (updateInfo != null) {
            
            String versaoAtual = Constantes.CLIENTE_VERSAO;
            String versaoBD = obterVersaoBD();
            
            if (versaoAtual.equals(updateInfo.getVersao()) && !versaoBD.equals(updateInfo.getUpdateScript().getVersaoBD())) {
        
                List<UpdateScriptAcaoTO> acoes = updateInfo.getUpdateScript().getAcoes();

                // processar
                for (UpdateScriptAcaoTO acao: acoes) {            
                    if (acao.getTipoAcao().equals(UpdateScriptTipoAcao.EXECUTAR_SQL)) {
                        LogMachine.getInstancia().logInfo("Executando comando SQL "+acao.getSequencia()+": "+acao.getValor(), Main.class.getName(), "atualizaBD");
                        executarSQL(acao.getValor());
                    }

                }
                
                atualizarVersaoBD(updateInfo.getUpdateScript().getVersaoBD());
                
            }
        }
        
    }
    
    private void atualizarVersaoBD(String versaoBD) throws Exception {
        // atualizar versão de banco de dados
        LogMachine.getInstancia().logInfo("Atualizando versão do banco de dados para "+versaoBD, Main.class.getName(), "atualizarVersaoBD");
        Parametro p = ServiceLocalFactory.getParametroService().getParametros();
        if (p!=null) {
            p.setVersaoBD(versaoBD);
            ServiceLocalFactory.getParametroService().setParametros(p);
        } else {
            throw new Exception("Não parametrizado!");
        }
        
    }
    
    private String obterVersaoBD() throws Exception {
        
        // obter versão de banco de dados
        Parametro p = ServiceLocalFactory.getParametroService().getParametros();
        
        if (p!=null) {
            if (p.getVersaoBD()!=null) {
                return p.getVersaoBD();
            } else {
                return Constantes.CLIENTE_VERSAOBD;
            }
        } else {
            throw new Exception("Não parametrizado!");
        }
    }
    
    private UpdateInfoTO obterUpdateInfo() throws Exception {
        UpdateInfoTO updateInfo = null;
        
        String nome = Constantes.CLIENTE_UPDATEINFOFILE;
        File file = new File(nome);
        
        if (!file.exists()) {
            //throw new Exception("Arquivo "+nome+" não existe!");
        } else {
        
            try{
                XStream xstream = new XStream();
                updateInfo = (UpdateInfoTO) xstream.fromXML(file);       
            }catch(Exception e){
                e.printStackTrace();
                throw new Exception("Falha ao ler "+nome+": "+e.getMessage());
            }
            
        }
        
        return updateInfo;        
        
    }
    
    private void executarSQL(String sql) {
        try {
            // executar SQL
            if (!EntityManagerFactory.getEntityManager().getTransaction().isActive()) {
                EntityManagerFactory.getEntityManager().getTransaction().begin();
            }

            Integer numAfetados = 0;

            Integer n = EntityManagerFactory.getEntityManager().createNativeQuery(sql).executeUpdate();
            
            numAfetados = numAfetados + n;

            if (EntityManagerFactory.getEntityManager().getTransaction().isActive()) {
                EntityManagerFactory.getEntityManager().flush();
                EntityManagerFactory.getEntityManager().getTransaction().commit();
            }

            LogMachine.getInstancia().logInfo("Comando SQL executado: "+sql+"! \n\n Registros afetados: "+numAfetados, Main.class.getName(), "executarSQL");

        } catch (Exception e) {
            e.printStackTrace();
            if (EntityManagerFactory.getEntityManager().getTransaction().isActive()) {
                EntityManagerFactory.getEntityManager().getTransaction().rollback();
            }            
            LogMachine.getInstancia().logErro("Falha ao executar comando SQL: "+sql+"! \n\nErro: "+e.getMessage(), Main.class.getName(), "executarSQL");
        }
    }    
    
    public void verificaAtualizacao() throws Exception {
        
        if (atualizar) {
            
            LogMachine.getInstancia().logInfo("O sistema vai verificar se existem atualizações...", Main.class.getName(), "verificaAtualizacao");
        
            String versaoAtual = Constantes.CLIENTE_VERSAO;

            // obter remotamente as novidades e a versão atual
            UpdateInfoTO updateInfo = ServiceRemoteFactory.getUpdateServiceRemoto().getUpdateInfo();
            String novidades = updateInfo.getNovidades();
            String versao = updateInfo.getVersao();

            if (versao!=null && !versao.equals(versaoAtual)) {
                
                LogMachine.getInstancia().logInfo("Versão "+versao+" disponível.\n\nNovidades: "+novidades+"\n\nIniciando o processo de atualização...", Main.class.getName(), "verificaAtualizacao");

                UpdateAppJob.getInstancia().execute(null);

            }               
            
        }        
    }

    public static void iniciaJobs(Boolean reiniciar) {

        try {
            
            if (reiniciar) {
                // se for para reiniciar, remove as jobs
                PontoEletronicoJobs.getInstancia().deleteJob("Relogio");
                PontoEletronicoJobs.getInstancia().deleteJob("SincronizarDataHora");
                PontoEletronicoJobs.getInstancia().deleteJob("EnviarDigitaisJob");
                PontoEletronicoJobs.getInstancia().deleteJob("EnviarLogJob");
                PontoEletronicoJobs.getInstancia().deleteJob("ReceberDigitaisJob");
                PontoEletronicoJobs.getInstancia().deleteJob("EnviarPontosJob");
                PontoEletronicoJobs.getInstancia().deleteJob("AtualizarFuncionariosJob");
                PontoEletronicoJobs.getInstancia().deleteJob("ColetaLixoJob");
                PontoEletronicoJobs.getInstancia().deleteJob("UpdateAppJob");
            }

            // iniciando job que atualiza o relogio interno da aplicacao
            LogMachine.getInstancia().logInfo("iniciando job que atualiza o relogio interno da aplicacao", Main.class.getName(), "main");

            PontoEletronicoJobs.getInstancia().addJob("Relogio", RelogioJobService.class, 1 * Constantes.SEGUNDO, 1 * Constantes.SEGUNDO, Constantes.CLIENTE_QUARTZ_HIGH_PRIORITY);
            PontoEletronicoJobs.getInstancia().getScheduler().start();

            // atualizando os parametros
            LogMachine.getInstancia().logInfo("Sincronizando parametros", Main.class.getSimpleName(), "main");
            ParametroService ps = ServiceLocalFactory.getParametroService();
            ps.sincronizarParametros();

            // adicionando o job sincronizacao de horario
            LogMachine.getInstancia().logInfo("Agendando job SincronizarDataHora", Main.class.getSimpleName(), "main");
            PontoEletronicoJobs.getInstancia().addJob("SincronizarDataHora", RelogioJob.class, Integer.parseInt(ps.getParametros().getIntervaloSincronizacaoRelogio()), 10 * Constantes.SEGUNDO);

            // adiciona os jobs de envio e recepcao de digitais
            LogMachine.getInstancia().logInfo("Agendando job EnviarDigitaisJob", Main.class.getSimpleName(), "main");
            PontoEletronicoJobs.getInstancia().addJob("EnviarDigitaisJob", EnviarDigitaisJob.class, Integer.parseInt(ps.getParametros().getIntervaloEnvioDigitais()), 1 * Constantes.MINUTO);

            // adiciona job de envio de log
            LogMachine.getInstancia().logInfo("Agendando job EnviarLogJob", Main.class.getSimpleName(), "main");
            PontoEletronicoJobs.getInstancia().addJob("EnviarLogJob", EnviarLogJob.class, 2 * Constantes.HORA, 2 * Constantes.MINUTO);

            LogMachine.getInstancia().logInfo("Agendando job ReceberDigitaisJob", Main.class.getSimpleName(), "main");
            PontoEletronicoJobs.getInstancia().addJob("ReceberDigitaisJob", ReceberDigitaisJob.class, Integer.parseInt(ps.getParametros().getIntervaloRecebimentoDigitais()), 3 * Constantes.MINUTO);

            LogMachine.getInstancia().logInfo("Agendando job EnviarPontosJob", Main.class.getSimpleName(), "main");
            PontoEletronicoJobs.getInstancia().addJob("EnviarPontosJob", EnviarPontosJob.class, Integer.parseInt(ps.getParametros().getIntervaloEnvioRegistroPonto()), 5 * Constantes.MINUTO);

            // adiciona job para atualizar dados dos funcionários (senha da intranet, isenção de digital, entre outros)
            LogMachine.getInstancia().logInfo("Agendando job AtualizarFuncionariosJob", Main.class.getSimpleName(), "main");
            PontoEletronicoJobs.getInstancia().addJob("AtualizarFuncionariosJob", AtualizarFuncionariosJob.class, 6 * Constantes.HORA, 30 * Constantes.MINUTO);

            // coletor de lixo acionado a cada 30 segundos...
            LogMachine.getInstancia().logInfo("Agendando job ColetaLixoJob", Main.class.getSimpleName(), "main");
            PontoEletronicoJobs.getInstancia().addJob("ColetaLixoJob", ColetaLixoJob.class, 30 * Constantes.SEGUNDO, 60 * Constantes.SEGUNDO);
            
            // adiciona job UpdateAppJob
            LogMachine.getInstancia().logInfo("Agendando job UpdateAppJob", Main.class.getSimpleName(), "main");
            PontoEletronicoJobs.getInstancia().addJob("UpdateAppJob", UpdateAppJob.class, 12 * Constantes.HORA, 15 * Constantes.MINUTO);

            LogMachine.getInstancia().logInfo("passou jobs", "main", "main");

        } catch (Exception ex) {
            LogMachine.getInstancia().logErro("Erro ao iniciar jobs: " + ex.getMessage(), Main.class.getName(), "iniciaJobs");
        }
    }

    public static LoginController showTelaLogin(boolean admin) {

        try {
            if (fxmlLogin == null) {
                fxmlLogin = new FXMLLoader(instanceClass.getResource("/resources/fxml/login.fxml"));
            }
            fxmlLogin.setRoot(null);
            fxmlLogin.setController(null);            
            panelLogin = fxmlLogin.load();
            LoginController loginController = fxmlLogin.getController();
            loginController.setLoginAdmin(admin);

            Scene sceneLogin = new Scene(panelLogin);
            Stage dialog = new Stage();
            dialog.initOwner(stage);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setScene(sceneLogin);
            dialog.showAndWait();

            return loginController;
        } catch (IOException e) {
            JFXUtil.showErrorMessage("Login", "Tela não pode ser criada:" + e.getMessage());
            LogMachine.getInstancia().logErro("Erro ao mostrar tela de login: " + e.getMessage(), Main.class.getName(), "showTelaLogin");
        }
        return null;

    }
    
    
    public static SenhaController showTelaSenha() {

        try {
            
            if (fxmlSenha == null) {
                fxmlSenha = new FXMLLoader(instanceClass.getResource("/resources/fxml/senha.fxml"));                
            }
            fxmlSenha.setRoot(null);
            fxmlSenha.setController(null);
            panelSenha = fxmlSenha.load();
            
            SenhaController senhaController = fxmlSenha.getController();
            senhaController.getInputSenha().setText("");

            Scene scene = new Scene(panelSenha);
            Stage dialog = new Stage();
            dialog.initOwner(stage);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setScene(scene);
            dialog.showAndWait();

            return senhaController;
        } catch (IOException e) {
            JFXUtil.showErrorMessage("Senha", "Tela não pode ser criada:" + e.getMessage());
            LogMachine.getInstancia().logErro("Erro ao mostrar tela de senha: " + e.getMessage(), Main.class.getName(), "showTelaSenha");
        }
        return null;

    }    

    public static void showTelaDigital(Funcionario f, ModoTelaDigital modoTelaDigital) throws Exception {

        try {

            if (RelogioJobService.getInstancia().getRelogio() == null) {
                JFXUtil.showErrorMessage("Configuração da Hora", "O Relório do Ponto Eletrônico não está configurado. Chame o suporte.");
                return;
            }

            if (fxmlDigital == null) {
                fxmlDigital = new FXMLLoader(instanceClass.getResource("/resources/fxml/digital.fxml"));                
            }
            fxmlDigital.setRoot(null);
            fxmlDigital.setController(null);
            panelDigital = fxmlDigital.load();
            
            DigitalController digitalController = fxmlDigital.getController();

            digitalController.setFuncionario(f);

            if (f != null) {
                digitalController.getInputFuncionario().setText(f.getNome());
            }

            switch (modoTelaDigital) {
                case IDENTIFICACAO: {
                    digitalController.ativarModoIdentificacao();
                    break;
                }
                case VERIFICACAO: {
                    digitalController.ativarModoVerificacao();
                    break;
                }
                case INSERCAO: {
                    digitalController.ativarModoInsercao();
                    break;
                }
                default: {
                    throw new Exception("Modo de tela não especificado!");
                }
            }

            Scene sceneDigital = new Scene(panelDigital);
            Stage dialog = new Stage();
            dialog.initOwner(stage);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setScene(sceneDigital);
            dialog.showAndWait();

        } catch (IOException e) {
            JFXUtil.showErrorMessage("Captura Digital", "Tela não pode ser criada:" + e.getMessage());
            LogMachine.getInstancia().logErro("Erro ao mostrar tela de digitais: " + e.getMessage(), Main.class.getName(), "showTelaDigital");
        }

    }

    public static void showTelaConfiguracao() {
        try {
            Parent fxmlConfiguracao = FXMLLoader.load(instanceClass.getResource("/resources/fxml/configuracao.fxml"));
            Scene sceneConfiguracao = new Scene(fxmlConfiguracao);
            stage.setScene(sceneConfiguracao);
            JFXUtil.maximizar(stage);
            stage.show();
        } catch (IOException e) {
            JFXUtil.showErrorMessage("Tela de Configuração", "Tela não pode ser criada:" + e.getMessage());
            LogMachine.getInstancia().logErro("Erro ao mostrar tela de configuração: " + e.getMessage(), Main.class.getName(), "showTelaConfiguracao");
        }
    }
    
    public static void showTelaPrincipal() {
        try {
            Parent fxml = FXMLLoader.load(instanceClass.getResource("/resources/fxml/principal.fxml"));
            Scene scene = new Scene(fxml);
            stage.setScene(scene);
            JFXUtil.maximizar(stage);
            stage.show();
        } catch (IOException e) {
            JFXUtil.showErrorMessage("Tela principal", "Tela não pode ser criada:" + e.getMessage());
            LogMachine.getInstancia().logErro("Erro ao mostrar tela principal: " + e.getMessage(), Main.class.getName(), "showTelaPrincipal");
        }
    }

}
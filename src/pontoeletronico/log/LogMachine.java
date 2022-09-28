package pontoeletronico.log;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;

import javax.persistence.EntityManager;
import pontoeletronico.bean.Funcionario;
import pontoeletronico.factory.EntityManagerFactory;
import pontoeletronico.factory.ServiceLocalFactory;
import pontoeletronico.service.LogService;
import pontoeletronico.tipo.TipoOperacao;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import pontoeletronico.service.RelogioJobService;

public class LogMachine {

    private static LogMachine instancia;    
    private static LogService logService;

    private static long maxMemory;
    private static long freeMemory;
    private static long allocatedMemory;

    private static Logger logger;
    private SimpleDateFormat sdfHora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private LogMachine() {
        try {
            setConfig();
        } catch (Exception e) {
            System.out.println(LogMachine.class.getName() + ": Impossível configurar sistema de log -> " + e.getMessage());
        }
    }

    public static LogMachine getInstancia() {
        if (instancia == null) {
            instancia = new LogMachine();
        }

        return instancia;
    }

    public synchronized void log(TipoLog tipolog, String msg) {
        log(tipolog,msg,"LogMachine","log");
    }
    
    public synchronized void log(TipoLog tipolog, String msg, Funcionario funcionario, Integer empresaAdministrador, Integer matriculaAdministrador, TipoOperacao tipoOperacao) {
        log(tipolog,msg,"LogMachine","log", funcionario, empresaAdministrador, matriculaAdministrador, tipoOperacao);
    }    
    
    public synchronized void log(TipoLog tipolog, String msg, String classe, String metodo) {
        switch (tipolog) {
            case WARNING: {
                logWarning(msg,classe,metodo);
                break;
            }
            case ERRO: {
                logErro(msg,classe,metodo);
                break;
            }
            case INFO: {
                logInfo(msg,classe,metodo);
                break;
            }
            default: {
                logErro(msg,classe,metodo);
                break;
            }
        }
    }    
    
    public synchronized void log(TipoLog tipolog, String msg, String classe, String metodo, Funcionario funcionario, Integer empresaAdministrador, Integer matriculaAdministrador, TipoOperacao tipoOperacao) {
        switch (tipolog) {
            case WARNING: {
                logWarning(msg,classe,metodo, funcionario, empresaAdministrador, matriculaAdministrador, tipoOperacao);
                break;
            }
            case ERRO: {
                logErro(msg,classe,metodo, funcionario, empresaAdministrador, matriculaAdministrador, tipoOperacao);
                break;
            }
            case INFO: {
                logInfo(msg,classe,metodo, funcionario, empresaAdministrador, matriculaAdministrador, tipoOperacao);
                break;
            }
            default: {
                logErro(msg,classe,metodo, funcionario, empresaAdministrador, matriculaAdministrador, tipoOperacao);
                break;
            }
        }
    }

    public synchronized void logErro(String msg, String classe, String metodo) {
        logger.log(Level.SEVERE, "{0}.{1}: {2}", new Object[]{classe, metodo, formataMsg(msg)});        
    }
    
    public synchronized void logMemoria(String msg, String classe, String metodo) {
        String infoMemoria = getInfoMemoria();
        logger.log(Level.INFO, classe+"."+metodo+": "+formataMsg(msg) + infoMemoria);
    }
    
    public synchronized void logErro(String msg, String classe, String metodo, Funcionario funcionario, Integer empresaAdministrador, Integer matriculaAdministrador, TipoOperacao tipoOperacao) {
        logger.log(Level.SEVERE, classe+"."+metodo+": "+formataMsg(msg)+" (Funcionário: "+funcionario+")");
        getLogService().criarLog(TipoLog.ERRO, formataMsg(msg), classe, metodo, funcionario, empresaAdministrador, matriculaAdministrador, tipoOperacao);
    }

    public synchronized void logInfo(String msg, String classe, String metodo) {
        logger.log(Level.INFO, classe+"."+metodo+": "+formataMsg(msg));
    }
    
    public synchronized void logInfo(String msg, String classe, String metodo, Funcionario funcionario, Integer empresaAdministrador, Integer matriculaAdministrador, TipoOperacao tipoOperacao) {
        logger.log(Level.INFO, classe+"."+metodo+": "+formataMsg(msg)+" (Funcionário: "+funcionario+")");
        getLogService().criarLog(TipoLog.INFO, formataMsg(msg), classe, metodo, funcionario, empresaAdministrador, matriculaAdministrador, tipoOperacao);
    }

    public synchronized void logWarning(String msg, String classe, String metodo) {
        logger.log(Level.WARNING, classe+"."+metodo+": "+formataMsg(msg));
    }
    
    public synchronized void logWarning(String msg, String classe, String metodo, Funcionario funcionario, Integer empresaAdministrador, Integer matriculaAdministrador, TipoOperacao tipoOperacao) {
        logger.log(Level.WARNING, classe+"."+metodo+": "+formataMsg(msg)+" (Funcionário: "+funcionario+")");
        getLogService().criarLog(TipoLog.WARNING, formataMsg(msg), classe, metodo, funcionario, empresaAdministrador, matriculaAdministrador, tipoOperacao);
    }
    
    private synchronized String formataMsg(String msg) {
        String hRemoto = "";
        try {
            hRemoto = sdfHora.format(RelogioJobService.getInstancia().getDataHora());            
        } catch (NullPointerException npe) {
            hRemoto = "n/d";
        }
        String hLocal = sdfHora.format(new Date());
        String msgRetorno = String.format("HoraLocal[%s]HoraRemota[%s]->%s", hLocal, hRemoto, msg);

        return msgRetorno;
    }

    private void setConfig() throws Exception {
        String arquivoLog = "ponto.log";
        
        rotacionarArquivo(arquivoLog);
        
        logger = Logger.getLogger(LogMachine.class.getName());

        try {
            FileHandler fh = new FileHandler(arquivoLog,true);
            fh.setFormatter(new SimpleFormatter());           
            logger.addHandler(fh);            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
    private static LogService getLogService() {
        if (logService == null) {
            // setando entitymanager e service
            EntityManager em = EntityManagerFactory.getNewEntityManager();
            logService = ServiceLocalFactory.getNewLogService(em);
        }
        
        return logService;
    }

    private synchronized String getInfoMemoria(){
        maxMemory =  Runtime.getRuntime().maxMemory();
        freeMemory =  Runtime.getRuntime().freeMemory();
        allocatedMemory =  Runtime.getRuntime().totalMemory();

        return " memoria livre: " + NumberFormat.getInstance().format(freeMemory) + " K"  +
            " memoria alocada: " + NumberFormat.getInstance().format(allocatedMemory) + " K" +
            " memoria maxima: " + NumberFormat.getInstance().format(maxMemory) + " K" +
            " total memoria livre: " + NumberFormat.getInstance().format((freeMemory + (maxMemory - allocatedMemory))) + " K";
    }

    private void rotacionarArquivo(String arquivoLog) {
        File f = new File(arquivoLog);
        if (f.exists()) {
            if (f.length()>=52428800l) {
                String data = new SimpleDateFormat("_yyyyMMdd_HHmmss").format(new Date());
                f.renameTo(new File(arquivoLog.replaceAll(".log", data+".log")));                
            }
        }
    }

}
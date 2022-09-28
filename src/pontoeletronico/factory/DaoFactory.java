package pontoeletronico.factory;

import javax.persistence.EntityManager;

import pontoeletronico.dao.ConfiguracaoDao;
import pontoeletronico.dao.DigitalDao;
import pontoeletronico.dao.FuncionarioDao;
import pontoeletronico.dao.LogDao;
import pontoeletronico.dao.ParametroDao;
import pontoeletronico.dao.PontoDao;


/**
 *
 * @autor: DDSI
 */
public class DaoFactory {

    private static PontoDao pontoDao;    
    private static FuncionarioDao funcionarioDao;
    private static DigitalDao digitalDao;
    private static ConfiguracaoDao configuracaoDao;
    private static ParametroDao parametroDao;
    private static LogDao logDao;
    
    public static PontoDao getPontoDao() {
        if (pontoDao == null){
           pontoDao = new PontoDao();
        }
        return pontoDao;
    }
    
    public static PontoDao  getNewPontoDao(EntityManager newEntityManager) {
        return new PontoDao(newEntityManager);
    }
    
    
    public static FuncionarioDao  getFuncionarioDao() {
        if (funcionarioDao == null){
           funcionarioDao = new FuncionarioDao();
        }
        return funcionarioDao;
    }
    
    public static FuncionarioDao  getNewFuncionarioDao(EntityManager newEntityManager) {
        return new FuncionarioDao(newEntityManager);        
    }
    
    public static DigitalDao  getDigitalDao() {
        if (digitalDao == null){
           digitalDao = new DigitalDao();
        }
        return digitalDao;
    }     
    
    public static DigitalDao  getNewDigitalDao(EntityManager newEntityManager) {
        return new DigitalDao(newEntityManager);
    }
    
    public static ConfiguracaoDao  getConfiguracaoDao() {
        if (configuracaoDao == null){
           configuracaoDao = new ConfiguracaoDao();
        }
        return configuracaoDao;
    }     
    
    public static ConfiguracaoDao  getNewConfiguracaoDao(EntityManager newEntityManager) {
        return new ConfiguracaoDao(newEntityManager);
    }
    
    public static ParametroDao  getParametroDao() {
        if (parametroDao == null){
           parametroDao = new ParametroDao();
        }
        return parametroDao;
    }
    
    public static ParametroDao  getNewParametroDao(EntityManager newEntityManager){
        return new ParametroDao(newEntityManager);
    }
    
    public static LogDao  getLogDao() {
        if (logDao == null) {
            logDao = new LogDao();
        }        
        return logDao;
    }
    
    public static LogDao  getNewLogDao(EntityManager newEntitymanager) {
        return new LogDao(newEntitymanager);
    }
}

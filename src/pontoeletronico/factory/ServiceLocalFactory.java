package pontoeletronico.factory;

import javax.persistence.EntityManager;
import pontoeletronico.service.ConfiguracaoService;
import pontoeletronico.service.DigitalServiceLocal;
import pontoeletronico.service.FuncionarioServiceLocal;
import pontoeletronico.service.LeitoraService;
import pontoeletronico.service.LogService;
import pontoeletronico.service.ParametroService;
import pontoeletronico.service.PontoServiceLocal;


/**
 *
 * @autor: DDSI
 */
public class ServiceLocalFactory {

    //atributos para implementacao dos servicos
    private static PontoServiceLocal pontoService;
    
    private static ParametroService parametroService;    
    private static LogService logService;

    
    private static DigitalServiceLocal digitalServiceLocal;
    private static LeitoraService leitoraService;
    private static ConfiguracaoService configuracaoService;
        
    //--------------------------------------------------------------
    //Retorna uma implementação para os Servicos da Classe Ponto
    //--------------------------------------------------------------
    public static PontoServiceLocal getPontoServiceLocal() {
        return new PontoServiceLocal();
    }
    
    public static PontoServiceLocal getNewPontoService(EntityManager newEntityManager) {
        return new PontoServiceLocal(newEntityManager);        
    }
    
    //-----------------------------------------------------------------
    //Retorna uma implementação para os serviços da Leitora de Digital
    //-----------------------------------------------------------------
    public static LeitoraService getLeitoraService(){

        if (leitoraService == null) {
            leitoraService = new  LeitoraService();
        }
        return leitoraService;
    }
    
    
    //-------------------------------------------------------------------
    //Retorna uma implementação para os Servicos da Classe Funcionário
    //-------------------------------------------------------------------
    public static FuncionarioServiceLocal getFuncionarioServiceLocal(){
        
	return new FuncionarioServiceLocal();
   
    }
    
    public static FuncionarioServiceLocal getNewFuncionarioService(EntityManager newEntityManager){
        return new FuncionarioServiceLocal(newEntityManager);        
    }
    
    //-------------------------------------------------------------------
    //Retorna uma implementação para os Servicos da Classe Digital
    //-------------------------------------------------------------------
    public static DigitalServiceLocal getDigitalServiceLocal(){
       return new DigitalServiceLocal();
    }
    
    public static DigitalServiceLocal getNewDigitalService(EntityManager newEntityManager){
        return new DigitalServiceLocal(newEntityManager);
    }
    
    //-------------------------------------------------------------------
    //Retorna uma implementação para os Servicos da Classe Configuracao
    //-------------------------------------------------------------------
    public static ConfiguracaoService getConfiguracaoService(){
        if (configuracaoService == null){
            configuracaoService = new ConfiguracaoService();
        }
        return configuracaoService;
    }
    
    public static ConfiguracaoService getNewConfiguracaoService(EntityManager newEntityManager){
        return new ConfiguracaoService(newEntityManager);
    }

    //-------------------------------------------------------------------
    //Retorna uma implementação para os Servicos da Classe Configuracao
    //-------------------------------------------------------------------
    public static ParametroService getParametroService(){
        if (parametroService == null){
            parametroService = new ParametroService();
        }
        return parametroService;
    }
    
    public static ParametroService getNewParametroService(EntityManager newEntityManager){
        return new ParametroService(newEntityManager);
    }
    
    /**
     * @author marcosbispo
     * @return Implementação da classe de serviços para persistencia de log, usando entityManager padrão
     */
    public static LogService getLogService() {
        if (logService == null){
            logService = new LogService();
        }
        return logService;
    }
    
    /**
     * @author marcosbispo
     * @param newEntityManager Novo entityManager
     * @return Nova instância da Implementação da classe de serviços para persistencia de log usando o entitymanager externo passado como parâmetro
     */
    public static LogService getNewLogService(EntityManager newEntityManager){
        return new LogService(newEntityManager);
    }
    
}
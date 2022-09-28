package pontoeletronico.factory;

import javax.naming.NamingException;

import br.jus.tjms.comuns.context.Config;
import br.jus.tjms.organizacional.rest.UnidadeOrganizacionalRestClient;
import br.jus.tjms.organizacional.service.UnidadeOrganizacionalService;
import br.jus.tjms.pontoeletronico.client.DigitalServiceRemotoClient;
import br.jus.tjms.pontoeletronico.client.FuncionarioServiceRemotoClient;
import br.jus.tjms.pontoeletronico.client.LogServiceRemotoClient;
import br.jus.tjms.pontoeletronico.client.ParametroServiceRemotoClient;
import br.jus.tjms.pontoeletronico.client.PontoServiceRemotoClient;
import br.jus.tjms.pontoeletronico.client.RelogioServiceRemotoClient;
import br.jus.tjms.pontoeletronico.client.UpdateServiceRemotoClient;
import br.jus.tjms.pontoeletronico.service.DigitalServiceRemoto;
import br.jus.tjms.pontoeletronico.service.FuncionarioServiceRemoto;
import br.jus.tjms.pontoeletronico.service.LogServiceRemoto;
import br.jus.tjms.pontoeletronico.service.ParametroServiceRemoto;
import br.jus.tjms.pontoeletronico.service.PontoServiceRemoto;
import br.jus.tjms.pontoeletronico.service.RelogioServiceRemoto;
import br.jus.tjms.pontoeletronico.service.UpdateServiceRemoto;
import pontoeletronico.config.SpeConfig;


/**
 * Fabrica de serviços EJB
 * @author marcosbispo
 * obs.: para usar ServiceClientUtils o arquivo PontoEletronicoEjb.jar usado no 
 * cliente não pode conter esse arquivo
 */

public class ServiceRemoteFactory {

    private static FuncionarioServiceRemoto  funcionarioServiceRemoto;
    private static UnidadeOrganizacionalService unidadeOrganizacionalService;

    private static DigitalServiceRemoto digitalServiceRemoto;
    private static RelogioServiceRemoto relogioServiceRemoto;
    private static PontoServiceRemoto pontoServiceRemoto;
    private static LogServiceRemoto logServiceRemoto;
    private static UpdateServiceRemoto updateServiceRemoto;
    private static ParametroServiceRemoto parametroServiceRemoto;

    private static Boolean LOCAL = false;
    private static Boolean HOMOLOGACAO = false;
    
    
    public static void setHomologacao() {
        LOCAL = false;
        HOMOLOGACAO = true;
    }
    
    public static void setLocal() {
        LOCAL = true;
        HOMOLOGACAO = false;
    }
    
    private static String getURL(String local, String homologacao, String producao) {
        if (LOCAL) {System.out.print("ServiceRemoteFactory.getURL = "+local); return local;}
        if (HOMOLOGACAO) {System.out.print("ServiceRemoteFactory.getURL = "+homologacao); return homologacao;}
        System.out.print("ServiceRemoteFactory.getURL = "+producao); return producao;
    }

    public static ParametroServiceRemoto getParametroServiceRemoto() throws NamingException {

        if (parametroServiceRemoto == null) {
            parametroServiceRemoto = new ParametroServiceRemotoClient(getConfig(), getURL(ParametroServiceRemotoClient.URL_LOCAL,ParametroServiceRemotoClient.URL_HOMOLOGACAO,ParametroServiceRemotoClient.URL_PRODUCAO));
        }

        return parametroServiceRemoto;
    }

    public static UpdateServiceRemoto getUpdateServiceRemoto() throws NamingException {

        if (updateServiceRemoto == null) {
            updateServiceRemoto = new UpdateServiceRemotoClient(getConfig(), getURL(UpdateServiceRemotoClient.URL_LOCAL,UpdateServiceRemotoClient.URL_HOMOLOGACAO,UpdateServiceRemotoClient.URL_PRODUCAO));
        }

        return updateServiceRemoto;
    }

    public static DigitalServiceRemoto getDigitaServiceRemoto() throws NamingException {

        if (digitalServiceRemoto == null) {
            digitalServiceRemoto = new DigitalServiceRemotoClient(getConfig(), getURL(DigitalServiceRemotoClient.URL_LOCAL, DigitalServiceRemotoClient.URL_HOMOLOGACAO,DigitalServiceRemotoClient.URL_PRODUCAO));
        }

        return digitalServiceRemoto;
    }

    public static FuncionarioServiceRemoto getFuncionarioServiceRemoto() throws NamingException {

        if (funcionarioServiceRemoto == null) {
            funcionarioServiceRemoto = new FuncionarioServiceRemotoClient(getConfig(), FuncionarioServiceRemotoClient.URL_PRODUCAO);
        }
        return funcionarioServiceRemoto;
    }

    public static RelogioServiceRemoto getRelogioServiceRemoto() throws NamingException {

        if (relogioServiceRemoto == null) {
            relogioServiceRemoto = new RelogioServiceRemotoClient(getConfig(), getURL(RelogioServiceRemotoClient.URL_LOCAL,RelogioServiceRemotoClient.URL_HOMOLOGACAO,RelogioServiceRemotoClient.URL_PRODUCAO));
        }
        
        return relogioServiceRemoto;
    }

    public static PontoServiceRemoto getPontoServiceRemoto() throws NamingException {

        if (pontoServiceRemoto == null) {
            pontoServiceRemoto = new PontoServiceRemotoClient(getConfig(), getURL(PontoServiceRemotoClient.URL_LOCAL,PontoServiceRemotoClient.URL_HOMOLOGACAO,PontoServiceRemotoClient.URL_PRODUCAO)); 
        }

        return pontoServiceRemoto;
    }

    public static LogServiceRemoto getLogServiceRemoto() throws NamingException {
  
	if (logServiceRemoto == null){
	    logServiceRemoto = new LogServiceRemotoClient(getConfig(), getURL(LogServiceRemotoClient.URL_LOCAL, LogServiceRemotoClient.URL_HOMOLOGACAO,LogServiceRemotoClient.URL_PRODUCAO));
	}
	return logServiceRemoto;
    }

    public static UnidadeOrganizacionalService getUnidadeOrganizacionalService() throws NamingException {        
        if (unidadeOrganizacionalService == null) {
            unidadeOrganizacionalService = new UnidadeOrganizacionalRestClient(getConfig(), UnidadeOrganizacionalRestClient.URL_PRODUCAO);
        }
        return unidadeOrganizacionalService;
    }
    
    public static Config getConfig(){
	return new SpeConfig();
    }
    
}
package pontoeletronico.service;

import java.util.List;

import javax.persistence.EntityManager;

import br.jus.tjms.comuns.exceptions.DaoException;
import br.jus.tjms.comuns.exceptions.ServiceException;
import br.jus.tjms.organizacional.beans.UnidadeOrganizacional;
import br.jus.tjms.organizacional.service.UnidadeOrganizacionalService;
import pontoeletronico.bean.Configuracao;
import pontoeletronico.dao.ConfiguracaoDao;
import pontoeletronico.factory.DaoFactory;
import pontoeletronico.factory.ServiceRemoteFactory;

/**
 *
 * @author daren
 */
public class ConfiguracaoService  {

    ConfiguracaoDao dao;
    
    public ConfiguracaoService(){
        this.dao = DaoFactory.getConfiguracaoDao();
    }
    
    public ConfiguracaoService(EntityManager newEntitymanager) {
        dao = DaoFactory.getNewConfiguracaoDao(newEntitymanager);
    }

    public EntityManager getEntityManager() {
        return this.dao.getEntityManager();
    }

    public void setEntityManager(EntityManager em) {
        this.dao.setEntityManager(em);
    }    
    
    public UnidadeOrganizacional[] listarComarcaSecretaria(int instancia){
       
        try {
            
            UnidadeOrganizacionalService service = ServiceRemoteFactory.getUnidadeOrganizacionalService();
            
            if (instancia == 1){
                List<UnidadeOrganizacional> list = service.buscarComarcas();
                return list.toArray(new UnidadeOrganizacional[]{});
            }
            {
                List<UnidadeOrganizacional> list = service.buscarSecretarias();
                return list.toArray(new UnidadeOrganizacional[]{});
            }        
                   
       
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public UnidadeOrganizacional[] listarVaraDepartamento(int instancia, UnidadeOrganizacional unidade){
        try {
            
            UnidadeOrganizacionalService service = ServiceRemoteFactory.getUnidadeOrganizacionalService();
            
            if (instancia == 1){
                List<UnidadeOrganizacional> list = service.buscarVaras(unidade);
                return list.toArray(new UnidadeOrganizacional[]{});
                
            }
            {
                List<UnidadeOrganizacional> list = service.buscarDepartamentos(unidade);
                return list.toArray(new UnidadeOrganizacional[]{});
            }        
                   
       
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        
    }

    public void salvar(Configuracao configuracao) throws ServiceException {
        try {

            dao.salvar(configuracao);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
        
    }
    
    public void atualizar(Configuracao configuracao) throws ServiceException {
        try {
            dao.atualizar(configuracao);
        } catch (DaoException ex) {
            throw new ServiceException(ex);
        }
        
    }
    
    public boolean isConfigurado(){
        try {
            List list = dao.buscarTodos();
            if (list.isEmpty()){
                return false;
            } else
            {
                return true;
            }
        } catch (DaoException ex) {
            return false;
        }
    }

    public Configuracao getConfiguracao() {
        try {
            List <Configuracao> c = dao.buscarTodos();
            Configuracao cfg = c.get(0);
            return cfg;
        } catch (DaoException ex) {
            return null;
        }
    }
    
}
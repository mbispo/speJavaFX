package pontoeletronico.dao;

import javax.persistence.EntityManager;

import pontoeletronico.bean.Configuracao;

/**
 *
 * @author daren
 */
public class ConfiguracaoDao extends GenericDao<Configuracao, Integer>  {
    
    public ConfiguracaoDao(EntityManager newEntitymanager) {
        super(newEntitymanager);
    }

    public ConfiguracaoDao() {
        super();
    }
       
}

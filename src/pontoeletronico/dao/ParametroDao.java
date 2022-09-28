package pontoeletronico.dao;

import javax.persistence.EntityManager;

import pontoeletronico.bean.Parametro;

/**
 *
 * @author daren
 */
public class ParametroDao extends GenericDao<Parametro, Integer> {
    
    /**
     * Construtor que recebe um entitymanager externo
     * @author marcosbispo
     * @param newEntitymanager
     */
    public ParametroDao(EntityManager newEntitymanager) {
        super(newEntitymanager);
    }

    public ParametroDao() {
        super();
    }
    

}

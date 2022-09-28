package pontoeletronico.factory;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

/**
 *
 * @autor: DDSI
 */
public class EntityManagerFactory {

    private static EntityManager em;

    public static EntityManager getEntityManager() {        
        if (em == null) {
            em = getNewEntityManager();
        }
        return em;
    }
    
    public static EntityManager getNewEntityManager() {
        javax.persistence.EntityManagerFactory emf = Persistence.createEntityManagerFactory("PontoEletronicoPU");
        EntityManager emNew = emf.createEntityManager();
        emNew.setFlushMode(FlushModeType.COMMIT);
        return emNew;
    }
    
}

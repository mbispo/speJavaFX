package pontoeletronico.dao;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import br.jus.tjms.comuns.exceptions.DaoException;
import pontoeletronico.bean.Digital;
import pontoeletronico.log.LogMachine;
import pontoeletronico.service.RelogioJobService;


/**
 *
 * @autor: DDSI
 */
public class DigitalDao extends GenericDao<Digital, Integer>  {

    /**
     * Construtor que recebe um entitymanager externo
     * @author marcosbispo
     * @param newEntitymanager
     */
    public DigitalDao(EntityManager newEntitymanager) {
        super(newEntitymanager);
    }

    public DigitalDao() {
        super();
    }
    /**
     * @author marcosbispo
     * @param ndias
     * @return
     * @throws br.gov.ms.tj.ddsi.comuns.exceptions.DaoException
     */
    public synchronized List<Digital> buscarNovas(int ndias)  {

        List<Digital> lista;

        try {

            String queryPorData = "select d from Digital d where (d.dataCriacao >= :dataCriacao or d.dataModificacao >= :dataModificacao) and d.enviado = false";
            String querySemData = "select d from Digital d where d.enviado = false";
            
            Query query;
            
            if (ndias > 0) {
                query = em.createQuery(queryPorData);

                Calendar c = new GregorianCalendar();

                c.setTime(RelogioJobService.getInstancia().getDataHora());

                c.add(Calendar.DATE, ndias * -1);

                query.setParameter("dataCriacao", c.getTime(), TemporalType.TIMESTAMP);
                query.setParameter("dataModificacao", c.getTime(), TemporalType.TIMESTAMP);

                lista = query.getResultList();

            } else {
                query = em.createQuery(querySemData);
                lista = query.getResultList();
            }

            return lista;

        } catch (Exception e) {
            e.printStackTrace();
            LogMachine.getInstancia().logInfo(e.getMessage(), DigitalDao.class.getName(), "buscarNovas");
            throw new DaoException("Não foi possível buscar!");
        }

    }

    public void definirTodasStatusEnviado() throws DaoException {
        try {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            
            String update = "update Digital d set d.enviado = true";
            
            Query query = em.createQuery(update);

            int i = query.executeUpdate();

            if (em.getTransaction().isActive()) {
                em.flush();
                em.getTransaction().commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LogMachine.getInstancia().logInfo(e.getMessage(), DigitalDao.class.getName(), "definirTodasStatusEnviado");
            throw new DaoException("Não foi possível atualizar o(s) registro(s)");
        }           
    }
    
    
}

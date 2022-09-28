package pontoeletronico.dao;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import br.jus.tjms.comuns.exceptions.DaoException;
import pontoeletronico.bean.Log;
import pontoeletronico.log.LogMachine;
import pontoeletronico.service.RelogioJobService;

/**
 *
 * @author marcosbispo
 */
public class LogDao extends GenericDao<Log, Integer>  {

    /**
     * Construtor que recebe um entitymanager externo
     * @author marcosbispo
     * @param newEntitymanager
     */
    public LogDao(EntityManager newEntitymanager) {
        super(newEntitymanager);
    }

    public LogDao() {
        super();
    }
    
    /**
     * 
     * @return Lista de registros de Log que não foram enviados para o serviço remoto
     * @throws br.jus.tjms.comuns.exceptions.DaoException
     */
    public List<Log> buscarNaoEnviados() throws DaoException {
        
        List<Log> lista;
        
        try {
            lista = em.createQuery("select l from Log l where l.enviado = false").getResultList();
            return lista;
        } catch (Exception e) {
            e.printStackTrace();
            LogMachine.getInstancia().logInfo(e.getMessage(), LogDao.class.getName(), "buscarNaoEnviados");
            throw new DaoException("Não foi possível buscar!");
        }
    }
    
    /**
     * Apaga os logs já enviados, registrados a mais de 3 dias
     * @throws br.jus.tjms.comuns.exceptions.DaoException
     */
    public void limparLogsEnviados() throws DaoException {
        
        try {
            
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();          
            }
            
            Query q = em.createQuery("delete from Log l where l.enviado = true and l.datahora < :dataIni");
            
            int ndias = 3;
            Calendar c = new GregorianCalendar();
            c.setTime(RelogioJobService.getInstancia().getDataHora());
            c.add(Calendar.DATE, ndias * -1);            

            q.setParameter("dataIni", c.getTime());
            
            int i = q.executeUpdate();
            
            LogMachine.getInstancia().logInfo(i+" logs excluidos.", this.getClass().getName(), "limparLogsEnviados");
            
            if (em.getTransaction().isActive()) {
                em.flush();
                em.getTransaction().commit();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LogMachine.getInstancia().logInfo(e.getMessage(), this.getClass().getName(), "limparLogsEnviados");
            throw new DaoException("Não foi possível excluir o registro!");
        }          
        
    }    

}

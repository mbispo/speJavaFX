package pontoeletronico.dao;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import br.jus.tjms.comuns.exceptions.DaoException;
import pontoeletronico.bean.Ponto;
import pontoeletronico.factory.EntityManagerFactory;
import pontoeletronico.log.LogMachine;
import pontoeletronico.service.RelogioJobService;
import pontoeletronico.tipo.SituacaoPonto;

/**
 *
 * autor: DDSI
 */
public class PontoDao extends GenericDao<Ponto,Integer>  {

    /**
     * Construtor que recebe um entitymanager externo
     * @author marcosbispo
     * @param newEntitymanager
     */
    public PontoDao(EntityManager newEntitymanager) {
        super(newEntitymanager);
    }

    public PontoDao() {
        super();
    }    
    
    public List<Ponto> buscarPontosNaoEnviados() throws DaoException {
        
        Query query = EntityManagerFactory.getEntityManager().createQuery("select p from Ponto p where p.situacao = :situacao");
        query.setParameter("situacao",  SituacaoPonto.NAO_ENVIADO);
        
        List<Ponto> lista = query.getResultList();

        return lista;        
    }
    
    /**
     * Apaga os pontos já enviados, registrados a mais de 10 dias
     * @throws br.jus.tjms.comuns.exceptions.DaoException
     */
    public void limparPontosEnviados() throws DaoException {
        
        try {
            
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();          
            }
            
            Query q = em.createQuery("delete from Ponto p where p.situacao = :situacao and p.dataHora < :dataIni");
            
            q.setParameter("situacao", SituacaoPonto.ENVIADO);

            int ndias = 10;
            Calendar c = new GregorianCalendar();
            c.setTime(RelogioJobService.getInstancia().getDataHora());
            c.add(Calendar.DATE, ndias * -1);            

            q.setParameter("dataIni", c.getTime());
            
            int i = q.executeUpdate();
            
            LogMachine.getInstancia().logInfo(i+" pontos excluidos.", this.getClass().getName(), "limparPontosEnviados");
            
            if (em.getTransaction().isActive()) {
                em.flush();
                em.getTransaction().commit();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LogMachine.getInstancia().logInfo(e.getMessage(), this.getClass().getName(), "limparPontosEnviados");
            throw new DaoException("Não foi possível excluir o registro!");
        }          
        
    }
   
    
   
}

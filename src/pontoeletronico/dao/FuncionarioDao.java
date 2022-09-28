package pontoeletronico.dao;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import br.jus.tjms.comuns.exceptions.DaoException;
import pontoeletronico.bean.Funcionario;
import pontoeletronico.bean.Log;
import pontoeletronico.bean.Ponto;
import pontoeletronico.log.LogMachine;
import pontoeletronico.service.RelogioJobService;
import pontoeletronico.tipo.SituacaoPonto;


/**
 *
 * autor: marcosm
 */
public class FuncionarioDao extends GenericDao<Funcionario, Integer>  {

    /**
     * Construtor que recebe um entitymanager externo
     * @author marcosbispo
     * @param newEntitymanager
     */
    public FuncionarioDao(EntityManager newEntitymanager) {
        super(newEntitymanager);
    }

    public FuncionarioDao() {
        super();
    }

    public synchronized void removerDigitais(Funcionario funcionario) throws DaoException {
        try {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();          
            }
            int i = em.createQuery("delete from Digital d where d.enviado = true and d.funcionario.id = "+funcionario.getId()).executeUpdate();
            if (em.getTransaction().isActive()) {
                em.flush();
                em.getTransaction().commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LogMachine.getInstancia().logInfo(e.getMessage(), FuncionarioDao.class.getName(), "removerDigitais");
            throw new DaoException("Não foi possível excluir o registro!");
        }        
    }
    
    public synchronized void redefinirEnvioDigitais(Funcionario funcionario) throws DaoException {
        try {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            
            String update = "update Digital d set d.enviado = false, d.dataModificacao = :dataModificacao where d.funcionario.id = :id_funcionario";
            
            Query query = em.createQuery(update);

            Calendar c = new GregorianCalendar();
            c.setTime(RelogioJobService.getInstancia().getDataHora());

            query.setParameter("dataModificacao", c.getTime(), TemporalType.TIMESTAMP);
            query.setParameter("id_funcionario", funcionario.getId());
            
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
            LogMachine.getInstancia().logInfo(e.getMessage(), FuncionarioDao.class.getName(), "redefinirEnvioDigitais");
            throw new DaoException("Não foi possível atualizar o(s) registro(s)");
        }        
    }

    public boolean podeExcluir(Funcionario funcionario) throws DaoException {
        List<Log> listaLogNaoEnviado;
        List<Ponto> listaPontoNaoEnviado;

        try {

            String queryPonto = "select p from Ponto p where p.situacao = :situacao and p.funcionario = :funcionario";
            String queryLog = "select l from Log l where l.enviado = false and l.funcionario = :funcionario";
            
            int npontos = -1;
            int nlog = -1;
            
            Query query = em.createQuery(queryPonto);
            query.setParameter("situacao", SituacaoPonto.NAO_ENVIADO);
            query.setParameter("funcionario", funcionario);
            
            listaPontoNaoEnviado = query.getResultList();
            
            Query query2 = em.createQuery(queryLog);
            query2.setParameter("funcionario", funcionario);
            
            listaLogNaoEnviado = query2.getResultList();

            if (listaPontoNaoEnviado != null) {
                npontos = listaPontoNaoEnviado.size();
            }
            
            if (listaLogNaoEnviado != null) {
                nlog = listaLogNaoEnviado.size();
            }
            
            return ((npontos == 0)&&(nlog == 0));

        } catch (Exception e) {
            e.printStackTrace();
            LogMachine.getInstancia().logInfo(e.getMessage(), FuncionarioDao.class.getName(), "podeExcluir");
            throw new DaoException("Não foi possível buscar!");
        }        
    }
   
}

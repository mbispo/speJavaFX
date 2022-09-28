package pontoeletronico.jobs;

import javax.persistence.EntityManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import pontoeletronico.factory.EntityManagerFactory;
import pontoeletronico.factory.ServiceLocalFactory;
import pontoeletronico.log.LogMachine;
import pontoeletronico.service.PontoServiceLocal;
import pontoeletronico.tipo.TipoOperacao;

/**
 * @author marcosm
 */
public class EnviarPontosJob implements Job {

    private static EnviarPontosJob instancia;

    public EnviarPontosJob() {
    }

    //------------------------------------------------------------------------
    // Job para enviar os pontos batidos localmente para o banco do SGP
    //------------------------------------------------------------------------
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String msg;
        if (context != null) {
            msg = "Iniciando job "+context.getJobDetail().getFullName();
        } else {
            msg = "Iniciando job "+this.getClass().getName();
        }
        
        LogMachine.getInstancia().logInfo(msg, EnviarPontosJob.class.getName(), "execute");
        
        // criamos instancias locais do entitymanager e services...
        EntityManager em = EntityManagerFactory.getNewEntityManager();        
        PontoServiceLocal pontoService = ServiceLocalFactory.getNewPontoService(em);

        try {
            
            pontoService.processarPontoRemoto();
            
            // apaga os pontos enviados, registrados a mais de 10 dias
            pontoService.limparPontosEnviados();
            
            if ((em != null)&&(em.isOpen())) {
                System.out.println(this.getClass().getName()+": fechando entityManager ");
                pontoService = null;
                em.clear();
                em.close();
                em = null;
            }            
            
        } catch (Exception e) {
            
            if (context != null) {
                msg = "Erro executando job "+context.getJobDetail().getFullName()+": "+e.getMessage();
            } else {
                msg = "Erro executando job "+this.getClass().getName()+": "+e.getMessage();
            }
            
            LogMachine.getInstancia().logErro(msg, EnviarPontosJob.class.getName(), "execute", null, null, null, TipoOperacao.ERROENVIOPONTO);

            if ((em != null)&&(em.isOpen())) {
                em.clear();
                em.close();
            }            
            
            throw new JobExecutionException(e.getMessage());
        }
        
        if (context != null) {
            msg = "Finalizando job "+context.getJobDetail().getFullName();
        } else {
            msg = "Finalizando job "+this.getClass().getName();
        }
        
        LogMachine.getInstancia().logInfo(msg, EnviarPontosJob.class.getName(), "execute");

    }
    
    //-----------------------------------------------------------------------
    // Retorna uma instancia da Classe EnviarPontosJob
    //------------------------------------------------------------------------
    public static EnviarPontosJob getNovaInstancia() {
        EnviarPontosJob job = new EnviarPontosJob();
        return job;
    }
    
    //-----------------------------------------------------------------------
    // Padrao Singleton para criacao de um objeto da Classe EnviarPontosJob
    //------------------------------------------------------------------------
    public static EnviarPontosJob getInstancia() {
        if (instancia == null) {
            instancia = getNovaInstancia();
        }
        return instancia;
    }
}

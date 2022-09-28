package pontoeletronico.jobs;

import java.util.List;
import javax.persistence.EntityManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import pontoeletronico.bean.Log;
import pontoeletronico.factory.EntityManagerFactory;
import pontoeletronico.factory.ServiceLocalFactory;
import pontoeletronico.log.LogMachine;
import pontoeletronico.service.LogService;
import pontoeletronico.util.Utils;

/**
 * 
 * @author marcosbispo
 */
public class EnviarLogJob implements Job {

    private static EnviarLogJob instancia;

    public EnviarLogJob() {
    }  
    

    public void execute(JobExecutionContext context) throws JobExecutionException {
        String msg;
        if (context != null) {
            msg = "Iniciando job "+context.getJobDetail().getFullName();
        } else {
            msg = "Iniciando job "+this.getClass().getName();
        }
        
        LogMachine.getInstancia().logInfo(msg, EnviarLogJob.class.getName(), "execute");
        
        // criamos instancias locais do entitymanager e services...
        EntityManager em = EntityManagerFactory.getNewEntityManager();        
        LogService logService = ServiceLocalFactory.getNewLogService(em);

        try {
            
            // pega a lista dos logs nao enviados              
            List<Log> lista = logService.buscarNaoEnviados();
            
            // envia para o serviço remoto            
            if ((lista != null)&&(lista.size() > 0)) {
            
                logService.gravarRemoto(lista);

                // atualiza status enviado
                for (Log log : lista) {
                    log.setEnviado(true);
                    logService.atualizar(log);
                }
                
                // apaga os log enviados, registrados a mais de 3 dias
                logService.limparLogsEnviados();
                
                if (context != null) {
                    msg = "Executando job "+context.getJobDetail().getFullName()+": "+lista.size()+" registros de log enviados.";
                } else {
                    msg = "Executando job "+this.getClass().getName()+": "+lista.size()+" registros de log enviados.";
                }
                
                LogMachine.getInstancia().logInfo(msg, EnviarLogJob.class.getName(), "execute");
                
            } else {
                if (context != null) {
                    msg = "Executando job "+context.getJobDetail().getFullName()+": não há registros de log a enviar.";
                } else {
                    msg = "Executando job "+this.getClass().getName()+": não há registros de log a enviar.";
                }
                
                LogMachine.getInstancia().logInfo(msg, EnviarLogJob.class.getName(), "execute");
            }
            
            if ((em != null)&&(em.isOpen())) {
                System.out.println(this.getClass().getName()+": fechando entityManager ");
                logService = null;
                em.clear();
                em.close();
                em = null;
            }     
            
            Utils.limpaLista(lista);
            
        } catch (Exception e) {
            
            e.printStackTrace();
            
            if (context != null) {
                msg = "Erro executando job "+context.getJobDetail().getFullName()+": "+e.getMessage();
            } else {
                msg = "Erro executando job "+this.getClass().getName()+": "+e.getMessage();
            }
            
            LogMachine.getInstancia().logErro(msg, EnviarLogJob.class.getName(), "execute");

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
        
        LogMachine.getInstancia().logInfo(msg, EnviarLogJob.class.getName(), "execute");
    }
    
    public static EnviarLogJob getNovaInstancia() {
        return new EnviarLogJob();
    }

    public static EnviarLogJob getInstancia() {
        if (instancia == null) {
            instancia = getNovaInstancia();
        }
        return instancia;
    }
}

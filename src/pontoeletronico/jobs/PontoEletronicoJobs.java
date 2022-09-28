package pontoeletronico.jobs;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author daren
 */
public class PontoEletronicoJobs {
    
    private static PontoEletronicoJobs instancia;
    private static Scheduler scheduler;
    
    public static PontoEletronicoJobs getInstancia(){
        if (instancia == null) {
            instancia = new PontoEletronicoJobs(); 
            try {
                setScheduler(StdSchedulerFactory.getDefaultScheduler());
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }  
        return instancia;
    }
    
    public void addJob(String jobName, Class jobClass, int intervalo, int tempoParaInicio) throws SchedulerException {
        long ctime = System.currentTimeMillis();

        JobDetail jobDetail = new JobDetail(jobName, Scheduler.DEFAULT_GROUP, jobClass);
        SimpleTrigger trigger = new SimpleTrigger(jobName, Scheduler.DEFAULT_GROUP); 
        trigger.setStartTime(new Date(ctime + tempoParaInicio));
        trigger.setRepeatInterval(intervalo);
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        
        if (getScheduler() != null) {
            getScheduler().scheduleJob(jobDetail, trigger);
        } else {
            throw new SchedulerException("Não foi possível adicionar o job "+jobName+"! Scheduler não definido.");
        }
    }
    
    public void addJob(String jobName, Class jobClass, int intervalo, int tempoParaInicio, int prioridade) throws SchedulerException {
        long ctime = System.currentTimeMillis();

        JobDetail jobDetail = new JobDetail(jobName, Scheduler.DEFAULT_GROUP, jobClass);
        SimpleTrigger trigger = new SimpleTrigger(jobName, Scheduler.DEFAULT_GROUP); 
        trigger.setStartTime(new Date(ctime + tempoParaInicio));
        trigger.setRepeatInterval(intervalo);  
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setPriority(prioridade);
        
        if (getScheduler() != null) {
            getScheduler().scheduleJob(jobDetail, trigger);
        } else {
            throw new SchedulerException("Não foi possível adicionar o job "+jobName+"! Scheduler não definido.");
        }
    }

    public void deleteJob(String JobName){
        try {
            if (getScheduler() != null) {
                getScheduler().deleteJob(JobName, scheduler.DEFAULT_GROUP);
            }
        } catch (SchedulerException ex) {
           ex.printStackTrace();
        }
    }
    
    public static Scheduler getScheduler() {
        return scheduler;
    }

    public static void setScheduler(Scheduler scheduler) {
        PontoEletronicoJobs.scheduler = scheduler;
    }
    
}
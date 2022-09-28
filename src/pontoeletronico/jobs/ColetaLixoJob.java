package pontoeletronico.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author marcosbispo
 */
public class ColetaLixoJob implements Job {
    
    private static ColetaLixoJob instancia;

    public ColetaLixoJob() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.gc();        
    }
    
    public static ColetaLixoJob getNovaInstancia() {        
        return new ColetaLixoJob();        
    }
    
    public static ColetaLixoJob getInstancia() {
        if (instancia == null) {
            instancia = getNovaInstancia();
        }
        return instancia;
    }

}

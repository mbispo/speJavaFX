package pontoeletronico.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import pontoeletronico.factory.ServiceRemoteFactory;
import pontoeletronico.log.LogMachine;
import pontoeletronico.service.RelogioJobService;

/**
 *
 * @author daren
 */
public class RelogioJob implements Job {

    private static RelogioJob instancia;

    private java.util.Date dataHoraServidor;
    private java.util.Date dataHoraCliente;

    public static RelogioJob getInstancia() {
        if (instancia == null) {
            instancia = new RelogioJob();
        }
        return instancia;

    }

    public RelogioJob() {

    }

    public void execute(JobExecutionContext context) throws JobExecutionException {

        long Diferenca;

        try {

            dataHoraServidor = ServiceRemoteFactory.getRelogioServiceRemoto().getDataHora();
            System.out.println("Data/Hora Servidor:" + dataHoraServidor);
            dataHoraCliente = RelogioJobService.getInstancia().getDataHora();
            System.out.println("Data/Hora Local:" + dataHoraCliente);

            // se a diferença for maior que 5 minutos atualiza hora local
            Diferenca = (dataHoraServidor.getTime() - dataHoraCliente.getTime());
            Diferenca = Math.abs(Diferenca);
            if (Diferenca > 60000) {
                RelogioJobService.getInstancia().setDataHora(dataHoraServidor);
                LogMachine.getInstancia().logInfo("Relogio atualizado.", this.getClass().getSimpleName(), "execute");
                System.out.println("Relogio atualizado");
            }

            LogMachine.getInstancia().logInfo("Sincronização de horário realizada.", this.getClass().getSimpleName(), "execute");
            System.out.println("Sincronização de horário realizada.");

        } catch (Exception ex) {
            LogMachine.getInstancia().logErro(ex.getMessage(), this.getClass().getName(), "RelogioJob.execute");
        }

    }

}

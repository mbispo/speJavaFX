package pontoeletronico.service;

import pontoeletronico.controller.PrincipalController;
import pontoeletronico.log.LogMachine;

/**
 * @author daren Classe criada para atualizar componentes com GUI com a data e
 * hora atualizadas
 *
 */
public class AtualizadorHorario extends Thread {

    private volatile PrincipalController controler;

    private volatile Boolean debug;
    
    public AtualizadorHorario(PrincipalController controler, Boolean debug) {
        this.controler = controler;
        this.debug = debug;
    }

    @SuppressWarnings("static-access")
    @Override
    public void run() {
        try {

            while (true) {
                if (debug)
                    LogMachine.getInstancia().logInfo("Rodando...", AtualizadorHorario.class.getName(), "run");
                if (controler != null) {
                    String data = RelogioJobService.getInstancia().getData();
                    String hora = RelogioJobService.getInstancia().getHora();
                    controler.atualizaLabelData(data);
                    controler.atualizaLabelHora(hora);

                    if (debug)
                        LogMachine.getInstancia().logInfo("Data/hora: "+data+" "+hora, AtualizadorHorario.class.getName(), "run");
                } else {
                    LogMachine.getInstancia().logErro("Controlador nulo!", AtualizadorHorario.class.getName(), "run");
                    throw new InterruptedException("Controlador nulo!");
                }

                Thread.sleep(1000);

            }

        } catch (InterruptedException ex) {
            String msg = "Erro na thread de atualização do horário: " + ex.getMessage();
            LogMachine.getInstancia().logErro(msg, AtualizadorHorario.class.getName(), "run");
        }
    }

}
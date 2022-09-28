package pontoeletronico;

import pontoeletronico.jobs.EnviarTodasDigitaisJob;

/**
 *
 * @author marcosbispo
 */
public class TesteEnviaTodasDigitais {

    public static void main(String[] args) {
        try {
            EnviarTodasDigitaisJob.getInstancia().execute(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

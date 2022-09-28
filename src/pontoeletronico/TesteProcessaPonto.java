package pontoeletronico;

import pontoeletronico.factory.ServiceLocalFactory;

/**
 *
 * @author marcosbispo
 */
public class TesteProcessaPonto {

    public static void main(String[] args) {
        try {   
            ServiceLocalFactory.getPontoServiceLocal().processarPontoRemoto();
        } catch (Exception e) {
            e.printStackTrace();            
        }
    }

}

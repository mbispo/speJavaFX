package pontoeletronico.service;

import com.griaule.grfingerjava.MatchingContext;
import com.griaule.grfingerjava.Template;

import pontoeletronico.bean.Digital;
import pontoeletronico.log.LogMachine;


/**
 *
 * 
 * LeitoraServiceImplFingerprint
 * 
 * Implementa os metodos da interface LeitoraService (para o SDK Fingerprint)
 */
public class LeitoraService {

    /**
     * @author marcosbispo
     * @param digitalInformada
     * @param digitalArmazenada
     * @return
     */
    public boolean comparaDigital(Digital digitalInformada, Digital digitalArmazenada, Object comparador) {

        boolean resultado = false;

        try {

            MatchingContext fingerprintSDK = (MatchingContext)comparador;

            Template templateArmazenado = new Template(digitalArmazenada.getImagemProcessada());
            
            Template templateInformado = new Template(digitalInformada.getImagemProcessada());

            resultado = fingerprintSDK.verify(templateInformado, templateArmazenado);

        } catch (Exception e) {
            LogMachine.getInstancia().logErro("Erro ao comparar digitais: "+e.getMessage(), LeitoraService.class.getName(), "comparaDigital");
        }

        return resultado;
    }
}

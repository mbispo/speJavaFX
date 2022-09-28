package pontoeletronico.util;

import java.util.List;

/**
 *
 * @author marcosbispo
 */
public class Utils {

    @SuppressWarnings("rawtypes")
	public static void limpaLista(List l) {
        try {
            if (l != null) {
                try {
                    if (l.size() > 0) {
                        l.clear();
                    }
                } catch (Exception e) {
                }

                l = null;
            }
        } catch (Exception e) {
        }
    }
}

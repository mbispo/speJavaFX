package pontoeletronico.util;

import java.io.Serializable;

/**
 *
 * @author marcosbispo
 */
public class Ambiente implements Serializable {

    private static final long serialVersionUID = 8308855007125019770L;

    private static Ambiente instance;

    public static Ambiente getInstance() throws Exception {
        if (instance == null) {
            instance = init().check();
        }
        return instance;
    }

    private static Ambiente init() {
        return new Ambiente();
    }

    private Ambiente() {
        super();
    }

    public Ambiente check() throws Exception {
        // verificar se as variáveis de ambiente estão configuradas

        if (getJavaHome() == null) {
            throw new Exception("Variável de ambiente JAVA_HOME não definida!");
        } else {
            System.out.println("JAVA_HOME = " + getJavaHome());
        }

        if (getDerbyHome() == null) {
            throw new Exception("Variável de ambiente DERBY_HOME não definida!");
        } else {
            System.out.println("DERBY_HOME = " + getDerbyHome());
        }

        if (getFingerPrintHome() == null) {
            throw new Exception("Variável de ambiente FINGERPRINT_HOME não definida!");
        } else {
            System.out.println("FINGERPRINT_HOME = " + getFingerPrintHome());
        }

        if (getESpeakHome() == null) {
            throw new Exception("Variável de ambiente ESPEAK_HOME não definida!");
        } else {
            System.out.println("ESPEAK_HOME = " + getESpeakHome());
        }

        if (getSpeHome() == null) {
            throw new Exception("Variável de ambiente SPE_HOME não definida!");
        } else {
            System.out.println("SPE_HOME = " + getSpeHome());
        }

        return this;
    }

    public String getJavaHome() {
        return System.getenv("JAVA_HOME");
    }

    public String getDerbyHome() {
        //C:\\Java\\spe\\db-derby-10.4.1.3-bin\\
        return System.getenv("DERBY_HOME");
    }

    public String getFingerPrintHome() {
        return System.getenv("FINGERPRINT_HOME");
    }

    public String getESpeakHome() {
        //C:/Program Files (x86)/eSpeak/
        return System.getenv("ESPEAK_HOME");
    }

    public String getSpeHome() {
        // c:/sistemas/spe/
        return System.getenv("SPE_HOME");
    }

    public String getVariavel(String var) {
        return System.getenv(var);
    }

    public static void main(String args[]) {

        try {

            System.out.println(Ambiente.getInstance().getJavaHome());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

}
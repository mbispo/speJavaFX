package pontoeletronico.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author marcos.bispo
 */
public class Fala {

    public static void falarPontoRegistrado() throws Exception {
        falar("Ponto registrado");
    }
    
    public static void falarPontoRegistradoNaHora(Date dataHora) throws Exception  {
        falar("Ponto registrado às "+montaFraseHora(dataHora));
    }
    
    public static void falarHora(Date dataHora) throws Exception  {
        falar("Agora são "+montaFraseHora(dataHora));
    }
    
    public static void falarData(Date dataHora) throws Exception  {
        falar("Hoje é "+montaFraseData(dataHora));
    }

    public static String montaFraseHora(Date data) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(data);

        int hora = c.get(Calendar.HOUR_OF_DAY);
        int minuto = c.get(Calendar.MINUTE);
        int segundo = c.get(Calendar.SECOND);

        String frase = "";

        String fraseHora = hora == 1 ? " uma hora " : (hora == 2 ? " duas horas " : hora + " horas ");
        String fraseMinuto = minuto == 0 ? " " : (minuto == 1 ? " um minuto " : minuto + " minutos ");
        String fraseSegundo = segundo == 0 ? " " : (segundo == 1 ? " um segundo " : segundo + " segundos ");

        frase = fraseHora + fraseMinuto + fraseSegundo;

        return frase;
    }
    
    static String montaFraseData(Date data) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(data);
        
        int dia = c.get(Calendar.DAY_OF_MONTH);
        int mes = c.get(Calendar.MONTH)+1;
        int ano = c.get(Calendar.YEAR);
        
        String frase = "";
            
        String fraseDia = dia == 1?" primeiro ":String.valueOf(dia);
        String fraseMes = " de";
        
        switch (mes) {
            case 1:  {
                fraseMes = fraseMes + " Janeiro";
                break;
            }
            case 2:  {
                fraseMes = fraseMes + " Fevereiro";
                break;
            }
            case 3:  {
                fraseMes = fraseMes + " Março";
                break;
            }
            case 4:  {
                fraseMes = fraseMes + " Abril";
                break;
            }
            case 5:  {
                fraseMes = fraseMes + " Maio";
                break;
            }
            case 6:  {
                fraseMes = fraseMes + " Junho";
                break;
            }
            case 7:  {
                fraseMes = fraseMes + " Julho";
                break;
            }
            case 8:  {
                fraseMes = fraseMes + " Agosto";
                break;
            }
            case 9:  {
                fraseMes = fraseMes + " Setembro";
                break;
            }
            case 10:  {
                fraseMes = fraseMes + " Outubro";
                break;
            }
            case 11:  {
                fraseMes = fraseMes + " Novembro";
                break;
            }
            case 12:  {
                fraseMes = fraseMes + " Dezembro";
                break;
            }
                
            default: {
                fraseMes = fraseMes + " Mês inválido ";
                break;
            }

        }
        
        String fraseAno = " de "+ano;

        frase = fraseDia+fraseMes+fraseAno;
        
        return frase;
    }

    public static void falar(String fala) throws Exception {
        executeCommand(Ambiente.getInstance().getESpeakHome()+"command_line/espeak.exe -v pt+f2 -p 45 -s 185 \"" + fala + "\"");
    }

    private static String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }
}
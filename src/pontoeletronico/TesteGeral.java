package pontoeletronico;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by diego.daniel on 30/11/2017.
 */
public class TesteGeral {

    public static void main(String[] args) {    
        
        
        System.out.println(DigestUtils.md5Hex("Golemz#@930"));
        
        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");
        
        Boolean horarioVerao;
        try {
            
            Date d = f.parse("28-03-2010");
            
            TimeZone tz = TimeZone.getTimeZone("Europe/London");
            //TimeZone tz = TimeZone.getDefault();
            horarioVerao = tz.inDaylightTime( d );
            System.out.println("horário de verão? "+horarioVerao);

            Calendar c = Calendar.getInstance(tz); // omit timezone for default tz
            c.setTime(d); // your date; omit this line for current date
            int offset = c.get(Calendar.DST_OFFSET);
            int zona = c.get(Calendar.ZONE_OFFSET);
            
            System.out.println("DST_OFFSET = "+offset);
            System.out.println("ZONE_OFFSET = "+offset);
            
            System.out.println(tz.getDSTSavings());
            System.out.println(tz.getDisplayName());
            System.out.println(tz.getID());
            System.out.println(tz.getRawOffset());
        
        } catch (ParseException ex) {
            Logger.getLogger(TesteGeral.class.getName()).log(Level.SEVERE, null, ex);
        }
                
        if (true) return;
        
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

        String horaComparacao = "18:01";

        String hora1 = "06:00";
        String hora2 = "18:00";



        try{
            Date dComparacao = formatter.parse(horaComparacao);
            Date d1 = formatter.parse(hora1);
            Date d2 = formatter.parse(hora2);


            if(  (dComparacao.getTime() >= d1.getTime())  &&  (dComparacao.getTime() <= d2.getTime())  ){
                System.out.println(horaComparacao + " esta entre " + hora1 + " e " + hora2);
            }
            else{
                System.out.println(horaComparacao + " NÃO esta entre " + hora1 + " e " + hora2);
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

}

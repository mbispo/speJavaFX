
package pontoeletronico.factory;

import br.jus.tjms.pontoeletronico.client.Constantes;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author marcosbispo
 */
public class CalendarFactory {

	public static CalendarHolder getCalendarHolder() {

		// novo
                
                Boolean horarioDeVerao = false;
                String tzString = "GMT-4";
		try {
			
			tzString = ServiceRemoteFactory.getRelogioServiceRemoto().getTimezone();
			
			System.out.println("\n");
			System.out.println("timezone retornado pelo servidor: "+tzString);
			
			TimeZone tz = TimeZone.getTimeZone(tzString);
			TimeZone.setDefault(tz);
			
			System.out.println("objeto timezone criado:" + tz);
			
			System.out.println("\n");
			
			Logger.getLogger(CalendarFactory.class.getName()).log(Level.INFO, tz.toString());

			Date data = ServiceRemoteFactory.getRelogioServiceRemoto().getDataHora();
			System.out.println("data:" + data);
			Logger.getLogger(CalendarFactory.class.getName()).log(Level.INFO, data.toString());

			if (tz.inDaylightTime(data)) {
                            horarioDeVerao = true;
                            System.out.println("horario de verao - server");
                            Logger.getLogger(CalendarFactory.class.getName()).log(Level.INFO, "horario de verao - server");
			} else {
                            System.out.println("horario normal - server");
                            Logger.getLogger(CalendarFactory.class.getName()).log(Level.INFO, "horario normal - server");
			}
                        
                        return new CalendarHolder(GregorianCalendar.getInstance(tz), tzString, horarioDeVerao);

		} catch (Exception ex) {
			Logger.getLogger(CalendarFactory.class.getName()).log(Level.SEVERE, null, ex);

			// não obteve do servidor, usar timezone da máquina
			TimeZone tz = TimeZone.getTimeZone(Constantes.TIME_ZONE);
                        
			TimeZone.setDefault(tz);
                        
                        tzString = tz.getID();
			
			if (tz.inDaylightTime(Calendar.getInstance().getTime())) {
                            horarioDeVerao = true;
                            System.out.println("horario de verao - local");
                            Logger.getLogger(CalendarFactory.class.getName()).log(Level.INFO, "horario de verao - local");
			} else {
                            System.out.println("horario normal - local");
                            Logger.getLogger(CalendarFactory.class.getName()).log(Level.INFO, "horario normal - local");
			}

			return new CalendarHolder(GregorianCalendar.getInstance(tz), tzString, horarioDeVerao);

		}

	}
}
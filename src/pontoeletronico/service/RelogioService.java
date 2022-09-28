package pontoeletronico.service;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.Timer;

import pontoeletronico.factory.CalendarFactory;
import pontoeletronico.factory.CalendarHolder;


/**
 *
 * @autor: DDSI
 */
public class RelogioService extends Thread {

	Timer timer;
	Calendar relogio;
        String timezone;
        Boolean horarioDeVerao;

	public RelogioService(Date data, int hora, int minuto, int segundo) {
            super();
            CalendarHolder ch = CalendarFactory.getCalendarHolder();
            relogio = ch.getCalendar();
            relogio.setTime(data);
            relogio.set(Calendar.HOUR, hora);
            relogio.set(Calendar.MINUTE, minuto);
            relogio.set(Calendar.SECOND, segundo);
            timezone = ch.getTimezone();
            horarioDeVerao = ch.getHorarioDeVerao();
	}

	@Override
	public void run() {

		timer = new Timer(1000, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				relogio.add(Calendar.SECOND, 1);
			}
			
		});

		timer.start();

		while (true) {
			
		}

	}

	public Date getDataHora() {
		return relogio.getTime();
	}

	public String getData() {
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		return df.format(relogio.getTime());
	}

	public String getHora() {
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
		return df.format(relogio.getTime());
	}

        public String getTimezone() {
            return timezone;
        }

        public Boolean getHorarioDeVerao() {
            return horarioDeVerao;
        }
        
        
}

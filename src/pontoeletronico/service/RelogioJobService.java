package pontoeletronico.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

import pontoeletronico.factory.CalendarFactory;
import pontoeletronico.factory.CalendarHolder;
import pontoeletronico.log.LogMachine;

/**
 * Classe que responsável por manter um relógio para o sistema, não dependendo da
 * data e hora do SO
 * @autor: DDSI
 */
public class RelogioJobService implements Job {

    private static RelogioJobService instancia;
    private volatile Calendar relogio = null;
    
    private volatile String timezone = null;
    private volatile Boolean horarioDeVerao = null;    
    

    public Calendar getRelogio() {
        return relogio;
    }

    public void setRelogio(Calendar aRelogio) {
        relogio = aRelogio;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Boolean getHorarioDeVerao() {
        return horarioDeVerao;
    }

    public void setHorarioDeVerao(Boolean horarioDeVerao) {
        this.horarioDeVerao = horarioDeVerao;
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        incrementaSegundo();        
    }

    public static synchronized void setDataHora(Date data, int hora, int minuto, int segundo) {        
        iniciaRelogio();
        LogMachine.getInstancia().logInfo("Setando Data:"+data+", hora = "+hora+", minuto = "+minuto+", segundo = "+segundo, RelogioJobService.class.getName(), "setDataHora");
        getInstancia().getRelogio().setTime(data);
        getInstancia().getRelogio().set(Calendar.HOUR, hora);
        getInstancia().getRelogio().set(Calendar.MINUTE, minuto);
        getInstancia().getRelogio().set(Calendar.SECOND, segundo);        
    }

    public static synchronized void setDataHora(Date data) throws SchedulerException {
        iniciaRelogio();
        getInstancia().getRelogio().setTime(data);
        LogMachine.getInstancia().logInfo("Setando Data :"+data, RelogioJobService.class.getName(), "setDataHora");
    }
    
    private static void iniciaRelogio() {
        if (getInstancia().getRelogio() == null) {
            CalendarHolder ch = CalendarFactory.getCalendarHolder();
            Calendar ca = ch.getCalendar();
            getInstancia().setRelogio(ca);
            getInstancia().setTimezone(ch.getTimezone());
            getInstancia().setHorarioDeVerao(ch.getHorarioDeVerao());
            LogMachine.getInstancia().logInfo("Iniciando Relogio :"+ca.getTime()+", timezone = "+ch.getTimezone()+", horarioDeVerao = "+ch.getHorarioDeVerao(), RelogioJobService.class.getName(), "iniciaRelogio");
        }
    }
    
    private void incrementaSegundo() {
        getInstancia().getRelogio().add(Calendar.SECOND, 1);
    }

    public static RelogioJobService getInstancia() {
        if (instancia == null) {
            instancia = new RelogioJobService();
        }
        return instancia;
    }

    public static Date getDataHora() {
        return getInstancia().getRelogio().getTime();
    }

    public static String getData() {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(getInstancia().getRelogio().getTime());
    }

    public static String getHora() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(getInstancia().getRelogio().getTime());
    }

}
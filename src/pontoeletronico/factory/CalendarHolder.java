package pontoeletronico.factory;

import java.util.Calendar;
import java.util.Objects;

/**
 *
 * @author marcos.bispo
 */
public class CalendarHolder {
    
    private Calendar calendar;
    private String timezone;
    private Boolean horarioDeVerao;

    public CalendarHolder(Calendar calendar, String timezone, Boolean horarioDeVerao) {
        this.calendar = calendar;
        this.timezone = timezone;
        this.horarioDeVerao = horarioDeVerao;
    }
    
    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.calendar);
        hash = 37 * hash + Objects.hashCode(this.timezone);
        hash = 37 * hash + Objects.hashCode(this.horarioDeVerao);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CalendarHolder other = (CalendarHolder) obj;
        return true;
    }
    
    
    
}

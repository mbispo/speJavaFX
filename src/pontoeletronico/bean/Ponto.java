package pontoeletronico.bean;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;
import pontoeletronico.tipo.SituacaoPonto;

/**
 *
 * @author DDSI
 */
@Entity
public class Ponto implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;    
    
    @Column(name = "Dt_Pnt", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHora;    
    
    @Column(name = "Dt_PntLcl", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataHoraLocal;
    
    @Column(name="Cd_LclMnc", nullable = false)
    private Integer localidade;
    
    @ManyToOne
    @JoinColumn(name="Cd_Fnc", nullable = false)
    private Funcionario funcionario;

    @Column(name="stc_pnt", nullable = false)
    private SituacaoPonto situacao;
    
    @Column(name = "timezone", nullable = false)
    private String timezone;
    
    @Column(name = "horarioVerao", nullable = false)
    private boolean horarioVerao;

    public Ponto(Date dataHora, Integer localidade, Funcionario funcionario, SituacaoPonto situacao, String timezone, boolean horarioVerao) {
        super();
        
        this.dataHora = dataHora;
        this.localidade = localidade;
        this.funcionario = funcionario;
        this.situacao = situacao;
        this.timezone = timezone;
        this.horarioVerao = horarioVerao;
    }

    public Ponto() {
        super();
    }
    
    public Date getDataHora() {
        return dataHora;
    }

    public void setDataHora(Date data) {
        this.dataHora = data;
    }

    public Date getDataHoraLocal() {
        return dataHoraLocal;
    }

    public void setDataHoraLocal(Date dataHoraLocal) {
        this.dataHoraLocal = dataHoraLocal;
    }

    public Integer getLocalidade() {
        return localidade;
    }

    public void setLocalidade(Integer localidade) {
        this.localidade = localidade;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SituacaoPonto getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoPonto situacao) {
        this.situacao = situacao;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public boolean isHorarioVerao() {
        return horarioVerao;
    }

    public void setHorarioVerao(boolean horarioVerao) {
        this.horarioVerao = horarioVerao;
    }

    @Override
    public String toString() {
        return "Ponto{" + "id=" + id + ", dataHora=" + dataHora + ", dataHoraLocal=" + dataHoraLocal + ", localidade=" + localidade + ", funcionario=" + funcionario + ", situacao=" + situacao + ", timezone=" + timezone + ", horarioVerao=" + horarioVerao + '}';
    }

}
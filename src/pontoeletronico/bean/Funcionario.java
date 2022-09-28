package pontoeletronico.bean;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import br.jus.tjms.pontoeletronico.to.FuncionarioTO;

/**
 *
 * autor: DDSI
 */
@Entity
public class Funcionario implements Serializable {

    private static final long serialVersionUID = -29965098345478364L;

    @Id
    @Column(name = "Cd_Fnc", nullable = false)
    private Integer id;

    @Column(name = "Nm_Fnc", nullable = false)
    private String nome;

    @Column(name = "Lotacao", nullable = false)
    private String lotacao;

    @Column(name = "IsentaDigital", nullable = false)
    private boolean isentadigital;

    @Column(name = "SenhaIntranet", nullable = false)
    private String senhaintranet;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "funcionario", fetch = FetchType.LAZY)
    @JoinColumn(name = "Cd_Fnc", nullable = false)
    private List<Digital> digitais;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "funcionario", fetch = FetchType.LAZY)
    @JoinColumn(name = "Cd_Fnc", nullable = false)
    private List<Ponto> pontos;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "funcionario", fetch = FetchType.LAZY)
    private List<Log> logs;

    // atributo que não será persistido no bando de dados...
    @Transient
    private FuncionarioTO funcionarioTO;

    public Funcionario() {
    }

    public Funcionario(FuncionarioTO f) {
        setId(f.getMatricula());
        setNome(f.getNome());
        setLotacao(f.getLotacao() != null ? f.getLotacao() : "");
        setIsentadigital(f.getIsentaDigital());
        setFuncionarioTO(f);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean getIsentadigital() {
        return isentadigital;
    }

    public void setIsentadigital(boolean isentadigital) {
        this.isentadigital = isentadigital;
    }

    public String getLotacao() {
        return lotacao;
    }

    public void setLotacao(String lotacao) {
        this.lotacao = lotacao;
    }

    public String getSenhaintranet() {
        return senhaintranet;
    }

    public void setSenhaintranet(String senhaintranet) {
        this.senhaintranet = senhaintranet != null ? senhaintranet : "";
    }

    public List<Digital> getDigitais() {
        return digitais;
    }

    public void setDigitais(List<Digital> digitais) {
        this.digitais = digitais;
    }

    public List<Ponto> getPontos() {
        return pontos;
    }

    public void setPontos(List<Ponto> pontos) {
        this.pontos = pontos;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs;
    }

    public FuncionarioTO getFuncionarioTO() {
        return funcionarioTO;
    }

    public void setFuncionarioTO(FuncionarioTO funcionarioTO) {
        this.funcionarioTO = funcionarioTO;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Funcionario other = (Funcionario) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.id;
        return hash;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + id + "-" + (nome != null ? nome : "") + "]";
    }

}

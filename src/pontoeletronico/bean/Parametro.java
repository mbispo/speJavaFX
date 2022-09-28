package pontoeletronico.bean;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author daren
 */
@Entity
public class Parametro implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "DS_IntEnvDgt", nullable = false)
    private String intervaloEnvioDigitais;
    
    @Column(name = "DS_IntRcbDgt", nullable = false)
    private String intervaloRecebimentoDigitais;

    @Column(name = "DS_IntEnvRgsPnt", nullable = false)
    private String intervaloEnvioRegistroPonto;

    @Column(name = "DS_IntEnvRgsOpr", nullable = false)
    private String intervaloEnvioRegistroOperacoes;
    
    @Column(name = "NR_NvlTlrVrf", nullable = false)
    private int nivelToleranciaVerificacao;
    
    @Column(name = "DS_IntScrRlg", nullable = false)
    private String intervaloSincronizacaoRelogio;

    @Column(name = "NR_DiaEnvDgt", nullable = false)
    private int diasEnvioDigitais;    
    
    @Column(name = "DS_SnhMst", nullable = false)
    private String senhaMaster;
    
    @Column(name = "versaoBD", nullable = false)
    private String versaoBD;
    
    public Parametro() {
        super();
    }

    public Parametro(String intervaloEnvioDigitais, String intervaloRecebimentoDigitais, String intervaloEnvioRegistroPonto, String intervaloEnvioRegistroOperacoes, int nivelToleranciaVerificacao, String intervaloSincronizacaoRelogio, int diasEnvioDigitais, String senhaMaster, String versaoBD) {
        this.intervaloEnvioDigitais = intervaloEnvioDigitais;
        this.intervaloRecebimentoDigitais = intervaloRecebimentoDigitais;
        this.intervaloEnvioRegistroPonto = intervaloEnvioRegistroPonto;
        this.intervaloEnvioRegistroOperacoes = intervaloEnvioRegistroOperacoes;
        this.nivelToleranciaVerificacao = nivelToleranciaVerificacao;
        this.intervaloSincronizacaoRelogio = intervaloSincronizacaoRelogio;
        this.diasEnvioDigitais = diasEnvioDigitais;
        this.senhaMaster = senhaMaster;
        this.versaoBD = versaoBD;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIntervaloEnvioDigitais() {
        return intervaloEnvioDigitais;
    }

    public void setIntervaloEnvioDigitais(String intervaloEnvioDigitais) {
        this.intervaloEnvioDigitais = intervaloEnvioDigitais;
    }

    public String getIntervaloEnvioRegistroOperacoes() {
        return intervaloEnvioRegistroOperacoes;
    }

    public void setIntervaloEnvioRegistroOperacoes(String intervaloEnvioRegistroOperacoes) {
        this.intervaloEnvioRegistroOperacoes = intervaloEnvioRegistroOperacoes;
    }

    public String getIntervaloEnvioRegistroPonto() {
        return intervaloEnvioRegistroPonto;
    }

    public void setIntervaloEnvioRegistroPonto(String intervaloEnvioRegistroPonto) {
        this.intervaloEnvioRegistroPonto = intervaloEnvioRegistroPonto;
    }

    public String getIntervaloRecebimentoDigitais() {
        return intervaloRecebimentoDigitais;
    }

    public void setIntervaloRecebimentoDigitais(String intervaloRecebimentoDigitais) {
        this.intervaloRecebimentoDigitais = intervaloRecebimentoDigitais;
    }

    public int getNivelToleranciaVerificacao() {
        return nivelToleranciaVerificacao;
    }

    public void setNivelToleranciaVerificacao(int nivelToleranciaVerificacao) {
        this.nivelToleranciaVerificacao = nivelToleranciaVerificacao;
    }

    public int getDiasEnvioDigitais() {
        return diasEnvioDigitais;
    }

    public void setDiasEnvioDigitais(int diasEnvioDigitais) {
        this.diasEnvioDigitais = diasEnvioDigitais;
    }

    public String getIntervaloSincronizacaoRelogio() {
        return intervaloSincronizacaoRelogio;
    }

    public void setIntervaloSincronizacaoRelogio(String intervaloSincronizacaoRelogio) {
        this.intervaloSincronizacaoRelogio = intervaloSincronizacaoRelogio;
    }

    public String getSenhaMaster() {
        return senhaMaster;
    }

    public void setSenhaMaster(String senhaMaster) {
        this.senhaMaster = senhaMaster;
    }

    public String getVersaoBD() {
        return versaoBD;
    }

    public void setVersaoBD(String versaoBD) {
        this.versaoBD = versaoBD;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Parametro)) {
            return false;
        }
        Parametro other = (Parametro) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Parametro{" + "id=" + id + ", intervaloEnvioDigitais=" + intervaloEnvioDigitais + ", intervaloRecebimentoDigitais=" + intervaloRecebimentoDigitais + ", intervaloEnvioRegistroPonto=" + intervaloEnvioRegistroPonto + ", intervaloEnvioRegistroOperacoes=" + intervaloEnvioRegistroOperacoes + ", nivelToleranciaVerificacao=" + nivelToleranciaVerificacao + ", intervaloSincronizacaoRelogio=" + intervaloSincronizacaoRelogio + ", diasEnvioDigitais=" + diasEnvioDigitais + ", senhaMaster=" + senhaMaster + ", versaoBD=" + versaoBD + '}';
    }

}

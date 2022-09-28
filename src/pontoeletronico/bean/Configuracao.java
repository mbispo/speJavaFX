package pontoeletronico.bean;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Column;

/**
 *
 * @author daren
 */
@Entity
public class Configuracao implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "CD_CmrScr", nullable = true)
    private Integer codigoComarcaSecretaria;

    @Column(name = "CD_VraDpt", nullable = true)
    private Integer codigoVaraDepartamento;

    @Column(name = "DS_CmrScr", nullable = true)
    private String descricaoComarcaSecretaria;

    @Column(name = "DS_VraDpt", nullable = true)
    private String descricaoVaraDepartamento;

    @Column(name = "CD_RdzOrn", nullable = true)
    private Integer codigoReduzidoOrganograma;

    @Column(name = "CD_Hrr", nullable = true)
    private Integer codigoInstancia;

    @Column(name = "CD_HstAto", nullable = true)
    private Integer codigoAto;

    @Column(name = "CD_LclMnc", nullable = true)
    private Integer codigoLocalidade;

    public Integer getCodigoReduzidoOrganograma() {
	return codigoReduzidoOrganograma;
    }

    public void setCodigoReduzidoOrganograma(Integer codigoReduzidoOrganograma) {
	this.codigoReduzidoOrganograma = codigoReduzidoOrganograma;
    }

    public String getDescricaoComarcaSecretaria() {
	return descricaoComarcaSecretaria;
    }

    public void setDescricaoComarcaSecretaria(String descricaoComarcaSecretaria) {
	this.descricaoComarcaSecretaria = descricaoComarcaSecretaria;
    }

    public String getDescricaoVaraDepartamento() {
	return descricaoVaraDepartamento;
    }

    public void setDescricaoVaraDepartamento(String descricaoVaraDepartamento) {
	this.descricaoVaraDepartamento = descricaoVaraDepartamento;
    }

    public Integer getCodigoVaraDepartamento() {
	return codigoVaraDepartamento;
    }

    public void setCodigoVaraDepartamento(Integer codigoVaraDepartamento) {
	this.codigoVaraDepartamento = codigoVaraDepartamento;
    }

    public Integer getCodigoComarcaSecretaria() {
	return codigoComarcaSecretaria;
    }

    public void setCodigoComarcaSecretaria(Integer codigoComarcaSecretaria) {
	this.codigoComarcaSecretaria = codigoComarcaSecretaria;
    }

    public void setId(Integer id) {
	this.id = id;
    }

    public Integer getId() {
	return id;
    }

    public Integer getCodigoInstancia() {
	return codigoInstancia;
    }

    public void setCodigoInstancia(Integer codigoInstancia) {
	this.codigoInstancia = codigoInstancia;
    }

    public Integer getCodigoAto() {
	return codigoAto;
    }

    public void setCodigoAto(Integer codigoAto) {
	this.codigoAto = codigoAto;
    }

    public Integer getCodigoLocalidade() {
	return codigoLocalidade;
    }

    public void setCodigoLocalidade(Integer codigoLocalidade) {
	this.codigoLocalidade = codigoLocalidade;
    }

    @Override
    public int hashCode() {
	int hash = 0;
	hash += (id != null ? id.hashCode() : 0);
	return hash;
    }

    @Override
    public boolean equals(Object object) {
	if (!(object instanceof Configuracao)) {
	    return false;
	}
	Configuracao other = (Configuracao) object;
	if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	return "pontoeletronico.bean.Configuracao[id=" + id + "]";
    }

}

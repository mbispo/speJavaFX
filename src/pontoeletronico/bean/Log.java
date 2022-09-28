package pontoeletronico.bean;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import pontoeletronico.log.TipoLog;
import pontoeletronico.tipo.TipoOperacao;

/**
 *
 * @author marcosbispo
 */
@Entity
public class Log implements Serializable {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date datahora;

    @Temporal(TemporalType.TIMESTAMP)
    private Date datahoraLocal;
    
    @Column(name="mensagem", length=6000)
    private String mensagem;
    
    private TipoLog tipo;
    private String classe;
    private String metodo;
    
    @ManyToOne
    private Funcionario funcionario;

    private boolean enviado;
    
    @Column(nullable = true)
    private Integer empresaAdministrador;
    
    @Column(nullable = true)
    private Integer matriculaAdministrador;

    @Column(nullable = true)
    private TipoOperacao tipoOperacao;
        
    @Column(nullable = true)
    private Integer codigoReduzidoOrganograma;

    @Column(nullable = true)
    private Integer codigoAto;
    
    public Log(){}

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public Integer getCodigoAto() {
        return codigoAto;
    }

    public void setCodigoAto(Integer codigoAto) {
        this.codigoAto = codigoAto;
    }

    public Integer getCodigoReduzidoOrganograma() {
        return codigoReduzidoOrganograma;
    }

    public void setCodigoReduzidoOrganograma(Integer codigoReduzidoOrganograma) {
        this.codigoReduzidoOrganograma = codigoReduzidoOrganograma;
    }

    public Date getDatahora() {
        return datahora;
    }

    public void setDatahora(Date datahora) {
        this.datahora = datahora;
    }

    public Date getDatahoraLocal() {
        return datahoraLocal;
    }

    public void setDatahoraLocal(Date datahoraLocal) {
        this.datahoraLocal = datahoraLocal;
    }
    
    

    public Integer getEmpresaAdministrador() {
        return empresaAdministrador;
    }

    public void setEmpresaAdministrador(Integer empresaAdministrador) {
        this.empresaAdministrador = empresaAdministrador;
    }

    public boolean isEnviado() {
        return enviado;
    }

    public void setEnviado(boolean enviado) {
        this.enviado = enviado;
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

    public Integer getMatriculaAdministrador() {
        return matriculaAdministrador;
    }

    public void setMatriculaAdministrador(Integer matriculaAdministrador) {
        this.matriculaAdministrador = matriculaAdministrador;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public TipoLog getTipo() {
        return tipo;
    }

    public void setTipo(TipoLog tipo) {
        this.tipo = tipo;
    }

    public TipoOperacao getTipoOperacao() {
        return tipoOperacao;
    }

    public void setTipoOperacao(TipoOperacao tipoOperacao) {
        this.tipoOperacao = tipoOperacao;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Log other = (Log) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "[id="+id+", mensagem="+mensagem+"]";
    }
    
    
    

}

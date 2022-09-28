package pontoeletronico.bean;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

//Classes do Framework de persistencia
import javax.persistence.Column;
import javax.persistence.*;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author DDSI
 */
@Entity
public class Digital implements Serializable {

    private static final long serialVersionUID = -999989629220344332L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name = "dt_Criacao", nullable = false)
    private Date dataCriacao;

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    @Column(name = "Dt_Modificacao", nullable = false)
    private Date dataModificacao;

    // armazena as digitais do funcionario
    @Lob
    @Column(name = "img", nullable = false)
    private byte[] imagem;

    @Lob
    @Column(name = "img_processada", nullable = false)
    private byte[] imagemProcessada;

    //funcionario
    @ManyToOne
    @JoinColumn(name = "Cd_Fnc", nullable = false)
    private Funcionario funcionario;

    @Column(name = "enviado", nullable = false)
    private boolean enviado;

    @Transient
    private ImageView imagemView;

    @Transient
    private ImageView imagemProcessadaView;

    @Transient
    private String dataCriacaoFormatada;

    public Digital() {
    }

    public Digital(Date dataCriacao, Date dataModificacao, byte[] imagem, byte[] imagemProcessada, Funcionario funcionario, boolean enviado) {
        this.dataCriacao = dataCriacao;
        this.dataModificacao = dataModificacao;
        this.imagem = imagem;
        this.imagemProcessada = imagemProcessada;
        this.funcionario = funcionario;
        this.enviado = enviado;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Date getDataModificacao() {
        return dataModificacao;
    }

    public void setDataModificacao(Date dataModificacao) {
        this.dataModificacao = dataModificacao;
    }

    public byte[] getImagem() {
        return imagem;
    }

    public void setImagem(byte[] imagem) {
        this.imagem = imagem;
    }

    public byte[] getImagemProcessada() {
        return imagemProcessada;
    }

    public void setImagemProcessada(byte[] imagemProcessada) {
        this.imagemProcessada = imagemProcessada;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public boolean isEnviado() {
        return enviado;
    }

    public void setEnviado(boolean enviado) {
        this.enviado = enviado;
    }

    public ImageView getImagemView() {
        imagemView = null;

        if (imagem != null) {
            imagemView = new ImageView(new Image(new ByteArrayInputStream(imagem)));
        }

        return imagemView;
    }

    public void setImagemView(ImageView imagemView) {
        this.imagemView = imagemView;
    }

    public String getDataCriacaoFormatada() {
        dataCriacaoFormatada = "";
        
        if (dataCriacao!=null) {
            dataCriacaoFormatada = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(dataCriacao);
        }
        
        return dataCriacaoFormatada;
    }

    public void setDataCriacaoFormatada(String dataCriacaoFormatada) {
        this.dataCriacaoFormatada = dataCriacaoFormatada;
    }

    public ImageView getImagemProcessadaView() {
        imagemProcessadaView = null;

        if (imagemProcessada != null) {
            imagemProcessadaView = new ImageView(new Image(new ByteArrayInputStream(imagemProcessada)));
        }

        return imagemProcessadaView;
    }

    public void setImagemProcessadaView(ImageView imagemProcessadaView) {
        this.imagemProcessadaView = imagemProcessadaView;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Digital other = (Digital) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

}
package pontoeletronico.view.fingerprint;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

import pontoeletronico.bean.Funcionario;

import com.griaule.grfingerjava.Template;

import javafx.scene.control.Label;


/**
 *
 * @author marcosbispo
 * 
 * CapturaViewFingerprint
 * 
 * Define os metodos que a view de captura de digitais (baseada no SDK Fingerprint) deverá implementar
 * 
 */
public interface CapturaViewFingerprint  {
    
    // criar nova digital do funcionário
    public void inserirDigital(BufferedImage imagemOrigem, Template template);
    
    // verificar uma digital de um funcionário
    public void verificarDigital(BufferedImage imagemOrigem, Template template);
    
    // identificar uma pessoa pela digital informada
    public void identificarDigital(BufferedImage imagemOrigem, Template template);
    
    // exibir imagens
    public void exibirImagem(BufferedImage image);    
    public void exibirImagemProcessada(BufferedImage image);

    
    public boolean isNaoRegistrou();
    public void setNaoRegistrou(boolean naoRegistrou);

    public void setVisible(boolean b);
    public void setFuncionario(Funcionario funcionario);
    
    // exibir mensagem em uma área de texto
    public void exibirMensagem(String msg);

    // exibir em um label o scanner ativo no momento...
    public void setDisplayLabelScannerAtual(String scanner);
    
    // exibir a imagem capturada
    public void setDisplayIconeImagemOrigem(ImageIcon iconeImagemOrigem);
    
    // retornar os labels da view que servirão para exibir as imagens (de origem e processada)
    public Label getLabelExibirIconeImagemOrigem();
    public Label getLabelExibirIconeImagemProcessadaOrigem();
    
    // ativar os modos de verificação/inserção/identificação de digitais
    public void ativarModoVerificacao();
    public void ativarModoInsercao();
    public void ativarModoIdentificacao();    

}

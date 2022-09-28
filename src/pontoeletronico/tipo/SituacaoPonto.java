/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pontoeletronico.tipo;

/**
 *
 * @author marcosm
 */
public enum SituacaoPonto {

    NAO_ENVIADO("Nao_Enviado"),
    ENVIADO("Enviado");    
    
     String situacao;
    
    private SituacaoPonto(String situacao){
        this.situacao = situacao;
    }
    
    public String toString(){
        return situacao;
    }
}

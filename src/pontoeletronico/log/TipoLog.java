package pontoeletronico.log;

/**
 *
 * @author marcosbispo
 */
public enum TipoLog {    
    ERRO("Erro"),
    WARNING("Aviso"),
    INFO("Informação");
    
    String tipo;
    
    private TipoLog(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return String.valueOf(tipo);
    }
    
}

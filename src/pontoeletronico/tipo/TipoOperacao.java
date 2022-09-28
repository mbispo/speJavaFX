package pontoeletronico.tipo;

/**
 * @version 1.0
 * @created 07-ago-2008 14:51:40
 */
public enum TipoOperacao {
    DATAHORA("Data informada"),
    CONFIGURACAO("Configuração realizada"),
    ERRORECEBIMENTODIGITAIS("Erro no recebimento de digitais"),
    ERROENVIODIGITAIS("Erro no envio de digitais"),
    PONTONAOREGISTRADO("Ponto não registrado"),
    PONTOREGISTRADO("Ponto registrado"),
    CADASTRODIGITAL("Digital cadastrada"),
    EXCLUSAODIGITAL("Digital excluída"),
    VERIFICACAODIGITAL("Digital verificada"),
    ERROVERIFICACAODIGITAL("Erro ao verificar digital"),
    IDENTIFICACAODIGITAL("Digital identificada"),
    ERROENVIOPONTO("Erro no envio dos registros de ponto"),
    ERROMATRICULANAOENCONTRADA("Matrícula não encontrada"),
    ERROBUSCAMATRICULA("Não foi possível buscar a matrícula"),
    ERRODIGITAISNAOCADASTRADAS("Digitais não cadastradas"),
    VALIDACAOSENHAINTRANET("Senha da intranet verificada com sucesso"),
    ERROVALIDACAOSENHAINTRANET("Senha da intranet inválida"),
    BATIMENTOINICIADO("Batimento de ponto iniciado");
            
    String tipo;   
    
    private TipoOperacao(String tipo){
        this.tipo = tipo;
    }
                
    @Override
    public String toString(){
        return tipo;
    }            
}


package service;

public class AcessoNegadoException extends IllegalArgumentException {
    public AcessoNegadoException(String mensagem) { super(mensagem); }
}

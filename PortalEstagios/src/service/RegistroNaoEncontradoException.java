package service;

public class RegistroNaoEncontradoException extends IllegalArgumentException {
    public RegistroNaoEncontradoException(String mensagem) { super(mensagem); }
}

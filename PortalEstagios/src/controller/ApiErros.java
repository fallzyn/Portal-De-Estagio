package controller;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiErros {
    @ExceptionHandler(service.AcessoNegadoException.class)
    public ResponseEntity<Erro> acesso(service.AcessoNegadoException e) {
        return ResponseEntity.status(403).body(new Erro(e.getMessage(), List.of()));
    }
    @ExceptionHandler(service.RegistroNaoEncontradoException.class)
    public ResponseEntity<Erro> inexistente(service.RegistroNaoEncontradoException e) {
        return ResponseEntity.status(404).body(new Erro(e.getMessage(), List.of()));
    }
    @ExceptionHandler(service.ConflitoException.class)
    public ResponseEntity<Erro> conflito(service.ConflitoException e) {
        return ResponseEntity.status(409).body(new Erro(e.getMessage(), List.of()));
    }
    public record Erro(String mensagem, List<String> detalhes) { }

    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Erro> parametroInvalido() {
        return ResponseEntity.badRequest().body(new Erro(
            "Parâmetro inválido. Use area com ID numérico e modalidade PRESENCIAL, REMOTA ou HIBRIDA.",
            List.of()));
    }

    @ExceptionHandler(service.RegistroDuplicadoException.class)
    public ResponseEntity<Erro> duplicado(service.RegistroDuplicadoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new Erro(e.getMessage(), List.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erro> validacao(MethodArgumentNotValidException e) {
        var detalhes = e.getBindingResult().getFieldErrors().stream()
            .map(erro -> erro.getField() + ": " + erro.getDefaultMessage()).toList();
        return ResponseEntity.badRequest().body(new Erro("Confira os campos informados.", detalhes));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Erro> jsonInvalido() {
        return ResponseEntity.badRequest().body(new Erro("Dados JSON inválidos ou campos não permitidos.", List.of()));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Erro> regra(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new Erro(e.getMessage(), List.of()));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Erro> integridade() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new Erro("Registro duplicado ou vinculado a outros dados.", List.of()));
    }
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Erro> banco() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new Erro("Não foi possível concluir a operação no banco.", List.of()));
    }
}

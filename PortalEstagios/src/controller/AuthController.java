package controller;

import dto.CadastroEntrada;
import dto.UsuarioResposta;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import security.UsuarioAutenticado;
import service.CadastroService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final CadastroService cadastros;

    public AuthController(CadastroService cadastros) { this.cadastros = cadastros; }

    public record CsrfResposta(String token, String headerName, String parameterName) { }

    @GetMapping("/csrf")
    public CsrfResposta csrf(CsrfToken token) {
        return new CsrfResposta(token.getToken(), token.getHeaderName(), token.getParameterName());
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResposta cadastrar(@Valid @RequestBody CadastroEntrada entrada) {
        return cadastros.cadastrar(entrada);
    }

    @GetMapping("/eu")
    public UsuarioResposta eu(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return new UsuarioResposta(usuario.id(), usuario.nome(), usuario.getUsername(), usuario.perfil());
    }
}

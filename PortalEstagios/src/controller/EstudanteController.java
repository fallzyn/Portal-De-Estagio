package controller;

import dto.*;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import security.UsuarioAutenticado;
import service.PortalWebService;

@RestController
@RequestMapping("/api/estudante/candidaturas")
public class EstudanteController {
    private final PortalWebService portal;
    public EstudanteController(PortalWebService portal) { this.portal = portal; }
    @GetMapping
    public List<CandidaturaResposta> minhas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return portal.minhas(usuario.id());
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CandidaturaResposta candidatar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody CandidaturaEntrada entrada) {
        return portal.candidatar(usuario.id(), entrada.vagaId());
    }
}

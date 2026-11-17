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
@RequestMapping("/api/empresa/vagas")
public class EmpresaController {
    private final PortalWebService portal;
    public EmpresaController(PortalWebService portal) { this.portal = portal; }

    @GetMapping
    public List<VagaResposta> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return portal.vagas(usuario.id());
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VagaResposta publicar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody VagaEntrada entrada) {
        return portal.publicar(usuario.id(), entrada);
    }
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable long id, @Valid @RequestBody VagaEntrada entrada) {
        portal.editar(usuario.id(), id, entrada);
    }
    @PatchMapping("/{id}/encerramento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void encerrar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable long id) {
        portal.encerrar(usuario.id(), id);
    }
    @GetMapping("/{id}/candidatos")
    public List<CandidatoResposta> candidatos(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable long id) {
        return portal.candidatos(usuario.id(), id);
    }
}

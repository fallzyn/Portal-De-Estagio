package controller;

import model.Contagens;
import dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import security.UsuarioAutenticado;
import service.PortalService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final PortalService portal;
    public AdminController(PortalService portal) { this.portal = portal; }

    @GetMapping("/contagens")
    public Contagens contagens(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return portal.contagens(usuario.id());
    }

    @PostMapping("/areas")
    @ResponseStatus(HttpStatus.CREATED)
    public AreaResposta criar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody AreaEntrada entrada) {
        var area = portal.criarArea(usuario.id(), entrada.nome());
        return new AreaResposta(area.id(), area.nome());
    }
    @PutMapping("/areas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editar(@AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable long id, @Valid @RequestBody AreaEntrada entrada) {
        portal.editarArea(usuario.id(), id, entrada.nome());
    }
    @DeleteMapping("/areas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable long id) {
        portal.excluirArea(usuario.id(), id);
    }
}

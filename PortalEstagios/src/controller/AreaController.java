package controller;

import dto.AreaResposta;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.PortalService;

@RestController
@RequestMapping("/api/areas")
public class AreaController {
    private final PortalService portal;

    public AreaController(PortalService portal) {
        this.portal = portal;
    }

    @GetMapping
    public List<AreaResposta> listar() {
        return portal.areas().stream()
            .map(area -> new AreaResposta(area.id(), area.nome())).toList();
    }
}
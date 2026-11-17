package controller;

import dto.VagaResposta;
import java.util.List;
import model.Modalidade;
import org.springframework.web.bind.annotation.*;
import service.PortalService;

@RestController
@RequestMapping("/api/vagas")
public class VagaController {
    private final PortalService portal;

    public VagaController(PortalService portal) {
        this.portal = portal;
    }

    @GetMapping
    public List<VagaResposta> listar(
            @RequestParam(required = false) Long area,
            @RequestParam(required = false) Modalidade modalidade) {
        if (area != null && area <= 0) {
            throw new IllegalArgumentException("O ID da área deve ser maior que zero.");
        }
        return portal.abertas(area, modalidade).stream().map(VagaResposta::de).toList();
    }
}

package controller;

import dto.AreaResposta;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.AreaConsultaService;

@RestController
@RequestMapping("/api/areas")
public class AreaController {
    private final AreaConsultaService areas;

    public AreaController(AreaConsultaService areas) {
        this.areas = areas;
    }

    @GetMapping
    public List<AreaResposta> listar() {
        return areas.listar().stream()
            .map(area -> new AreaResposta(area.id(), area.nome())).toList();
    }
}

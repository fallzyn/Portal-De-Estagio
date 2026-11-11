package service;

import java.util.List;
import model.Area;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.AreaRepository;

@Service
public class AreaConsultaService {
    private final AreaRepository areas;

    public AreaConsultaService(AreaRepository areas) {
        this.areas = areas;
    }

    @Transactional(readOnly = true)
    public List<Area> listar() {
        return areas.findAllByOrderByNomeAsc();
    }
}
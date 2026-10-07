package cz.uhk.fim.ppro.ordinace.service;

import cz.uhk.fim.ppro.ordinace.dto.VykonDto;
import cz.uhk.fim.ppro.ordinace.exception.BusinessRuleException;
import cz.uhk.fim.ppro.ordinace.exception.ResourceNotFoundException;
import cz.uhk.fim.ppro.ordinace.model.Vykon;
import cz.uhk.fim.ppro.ordinace.repository.VykonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class VykonService {

    private final VykonRepository vykonRepository;

    public VykonService(VykonRepository vykonRepository) {
        this.vykonRepository = vykonRepository;
    }

    public List<VykonDto.Response> ziskejVsechnyVykony() {
        return vykonRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public VykonDto.Response ziskejPodleId(Long id) {
        Vykon v = vykonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Výkon s ID " + id + " nebyl nalezen."));
        return mapToResponse(v);
    }

    @Transactional
    public VykonDto.Response vytvorVykon(VykonDto.Request req) {
        if (vykonRepository.existsByKodVykonu(req.kodVykonu())) {
            throw new BusinessRuleException("Výkon s kódem " + req.kodVykonu() + " již existuje.");
        }

        Vykon v = new Vykon(req.kodVykonu().trim(), req.nazev().trim(), req.body(), req.platnostOd());
        Vykon ulozeny = vykonRepository.save(v);
        return mapToResponse(ulozeny);
    }

    private VykonDto.Response mapToResponse(Vykon v) {
        return new VykonDto.Response(
                v.getId(),
                v.getKodVykonu(),
                v.getNazev(),
                v.getBody(),
                v.getPlatnostOd()
        );
    }
}

package cz.uhk.fim.ppro.ordinace.service;

import cz.uhk.fim.ppro.ordinace.dto.LekarDto;
import cz.uhk.fim.ppro.ordinace.exception.ResourceNotFoundException;
import cz.uhk.fim.ppro.ordinace.model.Lekar;
import cz.uhk.fim.ppro.ordinace.model.OrdinacniHodina;
import cz.uhk.fim.ppro.ordinace.repository.LekarRepository;
import cz.uhk.fim.ppro.ordinace.repository.OrdinacniHodinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LekarService {

    private final LekarRepository lekarRepository;
    private final OrdinacniHodinaRepository ordinacniHodinaRepository;

    public LekarService(LekarRepository lekarRepository, OrdinacniHodinaRepository ordinacniHodinaRepository) {
        this.lekarRepository = lekarRepository;
        this.ordinacniHodinaRepository = ordinacniHodinaRepository;
    }

    public List<LekarDto.Response> ziskejAktivniLekare() {
        return lekarRepository.findByAktivniTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public LekarDto.Response ziskejPodleId(Long id) {
        Lekar lekar = lekarRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lékař s ID " + id + " nebyl nalezen."));
        return mapToResponse(lekar);
    }

    private LekarDto.Response mapToResponse(Lekar l) {
        List<OrdinacniHodina> hodiny = ordinacniHodinaRepository.findByLekarIdOrderByDenVTydnuAscCasOdAsc(l.getId());
        List<LekarDto.OrdinacniHodinaResponse> hodinyDto = hodiny.stream()
                .map(h -> new LekarDto.OrdinacniHodinaResponse(
                        h.getId(),
                        h.getDenVTydnu(),
                        h.getCasOd(),
                        h.getCasDo(),
                        h.getPracovisteAdresa(),
                        h.getLzeObjednat()
                ))
                .toList();

        return new LekarDto.Response(
                l.getId(),
                l.getTitul(),
                l.getJmeno(),
                l.getPrijmeni(),
                l.getCeleJmeno(),
                l.getAktivni(),
                hodinyDto
        );
    }
}

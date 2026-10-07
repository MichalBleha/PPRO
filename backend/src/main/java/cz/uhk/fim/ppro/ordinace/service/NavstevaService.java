package cz.uhk.fim.ppro.ordinace.service;

import cz.uhk.fim.ppro.ordinace.dto.NavstevaDto;
import cz.uhk.fim.ppro.ordinace.exception.BusinessRuleException;
import cz.uhk.fim.ppro.ordinace.exception.ResourceNotFoundException;
import cz.uhk.fim.ppro.ordinace.model.*;
import cz.uhk.fim.ppro.ordinace.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class NavstevaService {

    private final NavstevaRepository navstevaRepository;
    private final RezervaceRepository rezervaceRepository;
    private final PacientRepository pacientRepository;
    private final LekarRepository lekarRepository;
    private final VykonRepository vykonRepository;

    public NavstevaService(
            NavstevaRepository navstevaRepository,
            RezervaceRepository rezervaceRepository,
            PacientRepository pacientRepository,
            LekarRepository lekarRepository,
            VykonRepository vykonRepository
    ) {
        this.navstevaRepository = navstevaRepository;
        this.rezervaceRepository = rezervaceRepository;
        this.pacientRepository = pacientRepository;
        this.lekarRepository = lekarRepository;
        this.vykonRepository = vykonRepository;
    }

    public List<NavstevaDto.Response> ziskejNavstevyPacienta(Long pacientId) {
        return navstevaRepository.findByPacientIdOrderByZacatekNavstevyDesc(pacientId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public NavstevaDto.Response ziskejPodleId(Long id) {
        Navsteva n = navstevaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Návštěva s ID " + id + " nebyla nalezena."));
        return mapToResponse(n);
    }

    @Transactional
    public NavstevaDto.Response zaevidujNavstevu(NavstevaDto.Request req) {
        if (req.zacatekNavstevy().isAfter(OffsetDateTime.now().plusMinutes(5))) {
            throw new BusinessRuleException("Návštěvu ani výkony nelze evidovat do budoucna. Návštěva musí reálně probíhat nebo být dokončena.");
        }

        Pacient pacient = pacientRepository.findById(req.pacientId())
                .orElseThrow(() -> new ResourceNotFoundException("Pacient s ID " + req.pacientId() + " nebyl nalezen."));

        Lekar lekar = lekarRepository.findById(req.lekarId())
                .orElseThrow(() -> new ResourceNotFoundException("Lékař s ID " + req.lekarId() + " nebyl nalezen."));

        Rezervace rezervace = null;
        if (req.rezervaceId() != null) {
            rezervace = rezervaceRepository.findById(req.rezervaceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rezervace s ID " + req.rezervaceId() + " nebyla nalezena."));

            if (rezervace.getStav() == StavRezervace.ZRUSENA) {
                throw new BusinessRuleException("K zrušené rezervaci nelze evidovat návštěvu.");
            }

            rezervace.setStav(StavRezervace.DOKONCENA);
        }

        Navsteva navsteva = new Navsteva(
                rezervace,
                pacient,
                lekar,
                req.zacatekNavstevy(),
                req.konecNavstevy() != null ? req.konecNavstevy() : OffsetDateTime.now(),
                req.zpravaLekare()
        );

        if (req.vykony() != null && !req.vykony().isEmpty()) {
            for (NavstevaDto.PolozkaVykonuRequest polozka : req.vykony()) {
                Vykon vykon = vykonRepository.findById(polozka.vykonId())
                        .orElseThrow(() -> new ResourceNotFoundException("Výkon s ID " + polozka.vykonId() + " nebyl nalezen."));

                int pocet = polozka.pocet() != null && polozka.pocet() > 0 ? polozka.pocet() : 1;
                int bodyCelkem = vykon.getBody() * pocet;

                NavstevaVykon nv = new NavstevaVykon(navsteva, vykon, pocet, bodyCelkem);
                navsteva.pridejVykon(nv);
            }
        }

        Navsteva ulozena = navstevaRepository.save(navsteva);
        return mapToResponse(ulozena);
    }

    private NavstevaDto.Response mapToResponse(Navsteva n) {
        int celkemBodu = 0;
        List<NavstevaDto.PolozkaVykonuResponse> vykonyDto = new java.util.ArrayList<>();

        if (n.getVykony() != null) {
            for (NavstevaVykon nv : n.getVykony()) {
                celkemBodu += nv.getPriznaneBody();
                vykonyDto.add(new NavstevaDto.PolozkaVykonuResponse(
                        nv.getId(),
                        nv.getVykon().getId(),
                        nv.getVykon().getKodVykonu(),
                        nv.getVykon().getNazev(),
                        nv.getPocet(),
                        nv.getVykon().getBody(),
                        nv.getPriznaneBody()
                ));
            }
        }

        return new NavstevaDto.Response(
                n.getId(),
                n.getRezervace() != null ? n.getRezervace().getId() : null,
                n.getPacient().getId(),
                n.getPacient().getPrijmeni() + " " + n.getPacient().getJmeno(),
                n.getPacient().getCisloPojistence(),
                n.getPacient().getKodPojistovny(),
                n.getLekar().getId(),
                n.getLekar().getCeleJmeno(),
                n.getZacatekNavstevy(),
                n.getKonecNavstevy(),
                n.getZpravaLekare(),
                celkemBodu,
                vykonyDto
        );
    }
}

package cz.uhk.fim.ppro.ordinace.service;

import cz.uhk.fim.ppro.ordinace.dto.RezervaceDto;
import cz.uhk.fim.ppro.ordinace.exception.BusinessRuleException;
import cz.uhk.fim.ppro.ordinace.exception.KolizeTerminuException;
import cz.uhk.fim.ppro.ordinace.exception.ResourceNotFoundException;
import cz.uhk.fim.ppro.ordinace.model.Lekar;
import cz.uhk.fim.ppro.ordinace.model.Pacient;
import cz.uhk.fim.ppro.ordinace.model.Rezervace;
import cz.uhk.fim.ppro.ordinace.model.StavRezervace;
import cz.uhk.fim.ppro.ordinace.repository.LekarRepository;
import cz.uhk.fim.ppro.ordinace.repository.PacientRepository;
import cz.uhk.fim.ppro.ordinace.repository.RezervaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RezervaceService {

    private final RezervaceRepository rezervaceRepository;
    private final PacientRepository pacientRepository;
    private final LekarRepository lekarRepository;
    private final OrdinacniDobaService ordinacniDobaService;

    public RezervaceService(
            RezervaceRepository rezervaceRepository,
            PacientRepository pacientRepository,
            LekarRepository lekarRepository,
            OrdinacniDobaService ordinacniDobaService
    ) {
        this.rezervaceRepository = rezervaceRepository;
        this.pacientRepository = pacientRepository;
        this.lekarRepository = lekarRepository;
        this.ordinacniDobaService = ordinacniDobaService;
    }

    public List<RezervaceDto.Response> ziskejRezervaceLekareVObdobi(Long lekarId, OffsetDateTime od, OffsetDateTime doCas) {
        return rezervaceRepository.najdiAktivniRezervaceLekareVObdobi(lekarId, od, doCas).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<RezervaceDto.Response> ziskejHistoriiPacienta(Long pacientId) {
        return rezervaceRepository.findByPacientIdOrderByCasOdDesc(pacientId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<RezervaceDto.Response> ziskejFrontuKPreobjednani() {
        return rezervaceRepository.findByStavOrderByCasOdAsc(StavRezervace.VYZADUJE_PREOBJEDNANI).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RezervaceDto.Response ziskejPodleId(Long id) {
        Rezervace r = rezervaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervace s ID " + id + " nebyla nalezena."));
        return mapToResponse(r);
    }

    @Transactional
    public RezervaceDto.Response vytvorRezervaci(RezervaceDto.Request req) {
        overMrizku15Minut(req.casOd(), req.casDo());

        Pacient pacient = pacientRepository.findById(req.pacientId())
                .orElseThrow(() -> new ResourceNotFoundException("Pacient s ID " + req.pacientId() + " nebyl nalezen."));

        Lekar lekar = lekarRepository.findById(req.lekarId())
                .orElseThrow(() -> new ResourceNotFoundException("Lékař s ID " + req.lekarId() + " nebyl nalezen."));

        if (!lekar.getAktivni()) {
            throw new BusinessRuleException("Nelze vytvářet rezervace u neaktivního lékaře.");
        }

        // Ověření ordinačních hodin a výluky
        ordinacniDobaService.overDostupnostLekare(lekar.getId(), req.casOd(), req.casDo());

        // Ověření nekoliznosti
        List<Rezervace> kolize = rezervaceRepository.najdiKolidujiciRezervace(lekar.getId(), req.casOd(), req.casDo(), null);
        if (!kolize.isEmpty()) {
            throw new KolizeTerminuException("Lékař již má v tomto čase naplánovanou jinou rezervaci. Kolize není povolena.");
        }

        Rezervace nova = new Rezervace(pacient, lekar, req.casOd(), req.casDo(), req.poznamka());
        Rezervace ulozena = rezervaceRepository.save(nova);
        return mapToResponse(ulozena);
    }

    @Transactional
    public RezervaceDto.Response stornujRezervaci(Long id) {
        Rezervace rezervace = rezervaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervace s ID " + id + " nebyla nalezena."));

        if (rezervace.getStav() == StavRezervace.ZRUSENA) {
            throw new BusinessRuleException("Tato rezervace již byla dříve zrušena.");
        }

        if (rezervace.getStav() == StavRezervace.DOKONCENA) {
            throw new BusinessRuleException("Dokončenou návštěvu nelze stornovat.");
        }

        rezervace.stornovat();
        return mapToResponse(rezervace);
    }

    @Transactional
    public RezervaceDto.Response preobjednejRezervaci(Long id, Long novyLekarId, OffsetDateTime novyCasOd, OffsetDateTime novyCasDo) {
        Rezervace rezervace = rezervaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervace s ID " + id + " nebyla nalezena."));

        overMrizku15Minut(novyCasOd, novyCasDo);

        Lekar lekar = novyLekarId != null
                ? lekarRepository.findById(novyLekarId).orElseThrow(() -> new ResourceNotFoundException("Lékař nebyl nalezen."))
                : rezervace.getLekar();

        ordinacniDobaService.overDostupnostLekare(lekar.getId(), novyCasOd, novyCasDo);

        List<Rezervace> kolize = rezervaceRepository.najdiKolidujiciRezervace(lekar.getId(), novyCasOd, novyCasDo, rezervace.getId());
        if (!kolize.isEmpty()) {
            throw new KolizeTerminuException("Vybraný náhradní termín koliduje s jinou rezervací lékaře.");
        }

        rezervace.setLekar(lekar);
        rezervace.setCasOd(novyCasOd);
        rezervace.setCasDo(novyCasDo);
        rezervace.setStav(StavRezervace.PLANOVANA);

        return mapToResponse(rezervace);
    }

    private void overMrizku15Minut(OffsetDateTime od, OffsetDateTime doCas) {
        if (!doCas.isAfter(od)) {
            throw new BusinessRuleException("Konec termínu musí následovat po jeho začátku.");
        }

        if (od.getMinute() % 15 != 0 || od.getSecond() != 0 ||
            doCas.getMinute() % 15 != 0 || doCas.getSecond() != 0) {
            throw new BusinessRuleException("Čas rezervace musí přesně odpovídat 15minutové mřížce (např. 08:00, 08:15, 08:30, 08:45).");
        }

        long minuty = Duration.between(od, doCas).toMinutes();
        if (minuty < 15 || minuty % 15 != 0) {
            throw new BusinessRuleException("Délka rezervace musí být násobkem 15 minut (minimálně 15 minut).");
        }
    }

    private RezervaceDto.Response mapToResponse(Rezervace r) {
        return new RezervaceDto.Response(
                r.getId(),
                r.getPacient().getId(),
                r.getPacient().getJmeno(),
                r.getPacient().getPrijmeni(),
                r.getPacient().getTelefon(),
                r.getPacient().getCisloPojistence(),
                r.getLekar().getId(),
                r.getLekar().getCeleJmeno(),
                r.getCasOd(),
                r.getCasDo(),
                r.getStav(),
                r.getPoznamka(),
                r.getDatumZruseni()
        );
    }
}

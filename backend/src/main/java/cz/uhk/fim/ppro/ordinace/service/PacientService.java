package cz.uhk.fim.ppro.ordinace.service;

import cz.uhk.fim.ppro.ordinace.dto.PacientDto;
import cz.uhk.fim.ppro.ordinace.exception.BusinessRuleException;
import cz.uhk.fim.ppro.ordinace.exception.ResourceNotFoundException;
import cz.uhk.fim.ppro.ordinace.model.Pacient;
import cz.uhk.fim.ppro.ordinace.model.StavRezervace;
import cz.uhk.fim.ppro.ordinace.repository.PacientRepository;
import cz.uhk.fim.ppro.ordinace.repository.RezervaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PacientService {

    private final PacientRepository pacientRepository;
    private final RezervaceRepository rezervaceRepository;

    public PacientService(PacientRepository pacientRepository, RezervaceRepository rezervaceRepository) {
        this.pacientRepository = pacientRepository;
        this.rezervaceRepository = rezervaceRepository;
    }

    public List<PacientDto.Response> vyhledejPacienty(String dotaz) {
        List<Pacient> pacienti;
        if (dotaz == null || dotaz.trim().isEmpty()) {
            pacienti = pacientRepository.findAll();
        } else {
            pacienti = pacientRepository.hledejPacienty(dotaz.trim());
        }
        return pacienti.stream().map(this::mapToResponse).toList();
    }

    public PacientDto.Response ziskejPodleId(Long id) {
        Pacient p = pacientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pacient s ID " + id + " nebyl nalezen."));
        return mapToResponse(p);
    }

    @Transactional
    public PacientDto.Response vytvorPacienta(PacientDto.Request req) {
        if (pacientRepository.existsByCisloPojistence(req.cisloPojistence())) {
            throw new BusinessRuleException("Pacient s číslem pojištěnce " + req.cisloPojistence() + " již v evidenci existuje.");
        }

        Pacient pacient = new Pacient(
                req.jmeno().trim(),
                req.prijmeni().trim(),
                req.rokNarozeni(),
                req.telefon().trim(),
                req.cisloPojistence().trim(),
                req.kodPojistovny().trim()
        );

        Pacient ulozeny = pacientRepository.save(pacient);
        return mapToResponse(ulozeny);
    }

    @Transactional
    public PacientDto.Response upravPacienta(Long id, PacientDto.Request req) {
        Pacient pacient = pacientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pacient s ID " + id + " nebyl nalezen."));

        if (!pacient.getCisloPojistence().equals(req.cisloPojistence()) &&
                pacientRepository.existsByCisloPojistence(req.cisloPojistence())) {
            throw new BusinessRuleException("Pacient s číslem pojištěnce " + req.cisloPojistence() + " již v evidenci existuje.");
        }

        pacient.setJmeno(req.jmeno().trim());
        pacient.setPrijmeni(req.prijmeni().trim());
        pacient.setRokNarozeni(req.rokNarozeni());
        pacient.setTelefon(req.telefon().trim());
        pacient.setCisloPojistence(req.cisloPojistence().trim());
        pacient.setKodPojistovny(req.kodPojistovny().trim());

        return mapToResponse(pacient);
    }

    private PacientDto.Response mapToResponse(Pacient p) {
        long zrusene = rezervaceRepository.countByPacientIdAndStav(p.getId(), StavRezervace.ZRUSENA);
        return new PacientDto.Response(
                p.getId(),
                p.getJmeno(),
                p.getPrijmeni(),
                p.getRokNarozeni(),
                p.getTelefon(),
                p.getCisloPojistence(),
                p.getKodPojistovny(),
                p.getVytvoreno(),
                zrusene
        );
    }
}

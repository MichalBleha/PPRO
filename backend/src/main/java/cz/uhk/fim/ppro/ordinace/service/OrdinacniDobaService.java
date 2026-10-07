package cz.uhk.fim.ppro.ordinace.service;

import cz.uhk.fim.ppro.ordinace.dto.SlotDto;
import cz.uhk.fim.ppro.ordinace.dto.VylukaLekareDto;
import cz.uhk.fim.ppro.ordinace.exception.BusinessRuleException;
import cz.uhk.fim.ppro.ordinace.exception.MimoOrdinacniDobuException;
import cz.uhk.fim.ppro.ordinace.exception.ResourceNotFoundException;
import cz.uhk.fim.ppro.ordinace.model.Lekar;
import cz.uhk.fim.ppro.ordinace.model.OrdinacniHodina;
import cz.uhk.fim.ppro.ordinace.model.Rezervace;
import cz.uhk.fim.ppro.ordinace.model.StavRezervace;
import cz.uhk.fim.ppro.ordinace.model.VylukaLekare;
import cz.uhk.fim.ppro.ordinace.repository.LekarRepository;
import cz.uhk.fim.ppro.ordinace.repository.OrdinacniHodinaRepository;
import cz.uhk.fim.ppro.ordinace.repository.RezervaceRepository;
import cz.uhk.fim.ppro.ordinace.repository.VylukaLekareRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OrdinacniDobaService {

    private final LekarRepository lekarRepository;
    private final OrdinacniHodinaRepository ordinacniHodinaRepository;
    private final VylukaLekareRepository vylukaLekareRepository;
    private final RezervaceRepository rezervaceRepository;

    public OrdinacniDobaService(
            LekarRepository lekarRepository,
            OrdinacniHodinaRepository ordinacniHodinaRepository,
            VylukaLekareRepository vylukaLekareRepository,
            RezervaceRepository rezervaceRepository
    ) {
        this.lekarRepository = lekarRepository;
        this.ordinacniHodinaRepository = ordinacniHodinaRepository;
        this.vylukaLekareRepository = vylukaLekareRepository;
        this.rezervaceRepository = rezervaceRepository;
    }

    public void overDostupnostLekare(Long lekarId, OffsetDateTime casOd, OffsetDateTime casDo) {
        if (!casDo.isAfter(casOd)) {
            throw new BusinessRuleException("Konec termínu musí být po jeho začátku.");
        }

        // 1. Kontrola výluky lékaře
        List<VylukaLekare> vyluky = vylukaLekareRepository.najdiKolidujiciVyluky(lekarId, casOd, casDo);
        if (!vyluky.isEmpty()) {
            VylukaLekare v = vyluky.get(0);
            throw new MimoOrdinacniDobuException("Lékař má v tomto čase evidovanou nepřítomnost/výluku: " + v.getDuvod());
        }

        // 2. Kontrola ordinačních hodin
        int denVTydnu = casOd.getDayOfWeek().getValue(); // 1 = Pondělí, 7 = Neděle
        LocalTime casOdTime = casOd.toLocalTime();
        LocalTime casDoTime = casDo.toLocalTime();

        List<OrdinacniHodina> hodiny = ordinacniHodinaRepository.findByLekarIdAndDenVTydnu(lekarId, denVTydnu);
        boolean uvnitrOrdinacnichHodin = hodiny.stream().anyMatch(h ->
                h.getLzeObjednat() &&
                !casOdTime.isBefore(h.getCasOd()) &&
                !casDoTime.isAfter(h.getCasDo())
        );

        if (!uvnitrOrdinacnichHodin) {
            throw new MimoOrdinacniDobuException("Zvolený čas leží mimo platné ordinační hodiny lékaře pro tento den nebo v čase bez možnosti objednání.");
        }
    }

    public List<SlotDto> vygenerujSlotyProDen(Long lekarId, LocalDate datum) {
        ZoneId zone = ZoneId.systemDefault();
        int denVTydnu = datum.getDayOfWeek().getValue();

        List<OrdinacniHodina> hodiny = ordinacniHodinaRepository.findByLekarIdAndDenVTydnu(lekarId, denVTydnu);
        if (hodiny.isEmpty()) {
            return List.of();
        }

        ZonedDateTime denZacatek = datum.atStartOfDay(zone);
        ZonedDateTime denKonec = datum.plusDays(1).atStartOfDay(zone);

        List<Rezervace> rezervaceDne = rezervaceRepository.najdiAktivniRezervaceLekareVObdobi(
                lekarId,
                denZacatek.toOffsetDateTime(),
                denKonec.toOffsetDateTime()
        );

        List<VylukaLekare> vylukyDne = vylukaLekareRepository.najdiKolidujiciVyluky(
                lekarId,
                denZacatek.toOffsetDateTime(),
                denKonec.toOffsetDateTime()
        );

        List<SlotDto> sloty = new ArrayList<>();

        for (OrdinacniHodina h : hodiny) {
            LocalTime currTime = h.getCasOd();
            while (currTime.plusMinutes(15).compareTo(h.getCasDo()) <= 0) {
                OffsetDateTime slotOd = datum.atTime(currTime).atZone(zone).toOffsetDateTime();
                OffsetDateTime slotDo = slotOd.plusMinutes(15);

                if (!h.getLzeObjednat()) {
                    sloty.add(new SlotDto(slotOd, slotDo, false, "NELZE_OBJEDNAT", null, null, "Vyhrazeno pro akutní případy"));
                    currTime = currTime.plusMinutes(15);
                    continue;
                }

                // Kontrola výluky
                boolean jeVyluka = vylukyDne.stream().anyMatch(v -> v.getDatumOd().isBefore(slotDo) && v.getDatumDo().isAfter(slotOd));
                if (jeVyluka) {
                    sloty.add(new SlotDto(slotOd, slotDo, false, "VYLUKA", null, null, "Nepřítomnost lékaře"));
                    currTime = currTime.plusMinutes(15);
                    continue;
                }

                // Kontrola rezervace
                Rezervace kolidujici = rezervaceDne.stream()
                        .filter(r -> r.getStav() == StavRezervace.PLANOVANA && r.getCasOd().isBefore(slotDo) && r.getCasDo().isAfter(slotOd))
                        .findFirst()
                        .orElse(null);

                if (kolidujici != null) {
                    sloty.add(new SlotDto(
                            slotOd,
                            slotDo,
                            false,
                            "REZERVOVANO",
                            kolidujici.getId(),
                            kolidujici.getPacient().getPrijmeni() + " " + kolidujici.getPacient().getJmeno(),
                            kolidujici.getPoznamka()
                    ));
                } else {
                    sloty.add(new SlotDto(slotOd, slotDo, true, null, null, null, null));
                }

                currTime = currTime.plusMinutes(15);
            }
        }

        return sloty;
    }

    @Transactional
    public VylukaLekareDto.Response zaznamenejVyluku(VylukaLekareDto.Request req) {
        Lekar lekar = lekarRepository.findById(req.lekarId())
                .orElseThrow(() -> new ResourceNotFoundException("Lékař s ID " + req.lekarId() + " nebyl nalezen."));

        if (!req.datumDo().isAfter(req.datumOd())) {
            throw new BusinessRuleException("Konec výluky musí být po jejím začátku.");
        }

        VylukaLekare vyluka = new VylukaLekare(lekar, req.datumOd(), req.datumDo(), req.duvod().trim());
        vylukaLekareRepository.save(vyluka);

        // Automatické přepnutí dotčených rezervací do stavu VYZADUJE_PREOBJEDNANI pro frontu recepce
        List<Rezervace> dotcene = rezervaceRepository.najdiAktivniRezervaceLekareVObdobi(lekar.getId(), req.datumOd(), req.datumDo())
                .stream()
                .filter(r -> r.getStav() == StavRezervace.PLANOVANA)
                .toList();

        for (Rezervace r : dotcene) {
            r.setStav(StavRezervace.VYZADUJE_PREOBJEDNANI);
        }

        return new VylukaLekareDto.Response(
                vyluka.getId(),
                lekar.getId(),
                lekar.getCeleJmeno(),
                vyluka.getDatumOd(),
                vyluka.getDatumDo(),
                vyluka.getDuvod(),
                dotcene.size()
        );
    }
}

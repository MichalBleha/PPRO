package cz.uhk.fim.ppro.ordinace.service;

import cz.uhk.fim.ppro.ordinace.dto.VykazDto;
import cz.uhk.fim.ppro.ordinace.model.Lekar;
import cz.uhk.fim.ppro.ordinace.model.Navsteva;
import cz.uhk.fim.ppro.ordinace.model.NavstevaVykon;
import cz.uhk.fim.ppro.ordinace.repository.LekarRepository;
import cz.uhk.fim.ppro.ordinace.repository.NavstevaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class VykazService {

    private final NavstevaRepository navstevaRepository;
    private final LekarRepository lekarRepository;

    public VykazService(NavstevaRepository navstevaRepository, LekarRepository lekarRepository) {
        this.navstevaRepository = navstevaRepository;
        this.lekarRepository = lekarRepository;
    }

    public VykazDto.MesicniPrehled generujMesicniVykaz(int rok, int mesic, Long lekarId) {
        YearMonth ym = YearMonth.of(rok, mesic);
        ZoneId zone = ZoneId.systemDefault();
        OffsetDateTime zacatek = ym.atDay(1).atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime konec = ym.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toOffsetDateTime();

        String lekarJmeno = "Všichni lékaři";
        if (lekarId != null) {
            Lekar lekar = lekarRepository.findById(lekarId).orElse(null);
            if (lekar != null) {
                lekarJmeno = lekar.getCeleJmeno();
            }
        }

        List<Navsteva> navstevy = navstevaRepository.najdiNavstevySProvedenymiVykony(lekarId, zacatek, konec);

        Map<String, AggregatedVykon> mapaVykonu = new HashMap<>();
        int celkemBodu = 0;

        for (Navsteva n : navstevy) {
            if (n.getVykony() != null) {
                for (NavstevaVykon nv : n.getVykony()) {
                    String kod = nv.getVykon().getKodVykonu();
                    celkemBodu += nv.getPriznaneBody();

                    AggregatedVykon agg = mapaVykonu.computeIfAbsent(kod, k ->
                            new AggregatedVykon(kod, nv.getVykon().getNazev(), nv.getVykon().getBody(), 0, 0));
                    agg.pocet += nv.getPocet();
                    agg.bodyCelkem += nv.getPriznaneBody();
                }
            }
        }

        List<VykazDto.PolozkaVykazu> polozky = mapaVykonu.values().stream()
                .sorted(Comparator.comparing(AggregatedVykon::kod))
                .map(a -> new VykazDto.PolozkaVykazu(a.kod, a.nazev, a.pocet, a.bodyJednotka, a.bodyCelkem))
                .toList();

        return new VykazDto.MesicniPrehled(
                rok,
                mesic,
                lekarId,
                lekarJmeno,
                navstevy.size(),
                celkemBodu,
                polozky
        );
    }

    private static class AggregatedVykon {
        final String kod;
        final String nazev;
        final int bodyJednotka;
        int pocet;
        int bodyCelkem;

        AggregatedVykon(String kod, String nazev, int bodyJednotka, int pocet, int bodyCelkem) {
            this.kod = kod;
            this.nazev = nazev;
            this.bodyJednotka = bodyJednotka;
            this.pocet = pocet;
            this.bodyCelkem = bodyCelkem;
        }

        String kod() { return kod; }
    }
}

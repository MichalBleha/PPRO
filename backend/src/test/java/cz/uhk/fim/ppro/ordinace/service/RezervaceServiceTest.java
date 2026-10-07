package cz.uhk.fim.ppro.ordinace.service;

import cz.uhk.fim.ppro.ordinace.dto.RezervaceDto;
import cz.uhk.fim.ppro.ordinace.exception.BusinessRuleException;
import cz.uhk.fim.ppro.ordinace.exception.KolizeTerminuException;
import cz.uhk.fim.ppro.ordinace.exception.MimoOrdinacniDobuException;
import cz.uhk.fim.ppro.ordinace.model.Lekar;
import cz.uhk.fim.ppro.ordinace.model.Pacient;
import cz.uhk.fim.ppro.ordinace.model.Rezervace;
import cz.uhk.fim.ppro.ordinace.model.StavRezervace;
import cz.uhk.fim.ppro.ordinace.repository.LekarRepository;
import cz.uhk.fim.ppro.ordinace.repository.PacientRepository;
import cz.uhk.fim.ppro.ordinace.repository.RezervaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RezervaceServiceTest {

    @Mock
    private RezervaceRepository rezervaceRepository;

    @Mock
    private PacientRepository pacientRepository;

    @Mock
    private LekarRepository lekarRepository;

    @Mock
    private OrdinacniDobaService ordinacniDobaService;

    @InjectMocks
    private RezervaceService rezervaceService;

    private Pacient pacient;
    private Lekar lekar;

    @BeforeEach
    void setUp() {
        pacient = new Pacient("Jan", "Novák", 1980, "+420777111222", "8005121234", "111");
        pacient.setId(1L);

        lekar = new Lekar("MUDr.", "Jana", "Hrubá", true);
        lekar.setId(1L);
    }

    @Test
    @DisplayName("Úspěšné vytvoření rezervace v platném 15minutovém slotu")
    void testVytvoreniRezervaceUspesne() {
        OffsetDateTime casOd = OffsetDateTime.of(2026, 10, 12, 8, 0, 0, 0, ZoneOffset.ofHours(2));
        OffsetDateTime casDo = OffsetDateTime.of(2026, 10, 12, 8, 30, 0, 0, ZoneOffset.ofHours(2));

        RezervaceDto.Request req = new RezervaceDto.Request(1L, 1L, casOd, casDo, "Preventivní prohlídka");

        when(pacientRepository.findById(1L)).thenReturn(Optional.of(pacient));
        when(lekarRepository.findById(1L)).thenReturn(Optional.of(lekar));
        when(rezervaceRepository.najdiKolidujiciRezervace(eq(1L), eq(casOd), eq(casDo), isNull()))
                .thenReturn(List.of());

        Rezervace ulozena = new Rezervace(pacient, lekar, casOd, casDo, req.poznamka());
        ulozena.setId(10L);
        when(rezervaceRepository.save(any(Rezervace.class))).thenReturn(ulozena);

        RezervaceDto.Response res = rezervaceService.vytvorRezervaci(req);

        assertThat(res).isNotNull();
        assertThat(res.id()).isEqualTo(10L);
        assertThat(res.stav()).isEqualTo(StavRezervace.PLANOVANA);
        verify(ordinacniDobaService).overDostupnostLekare(1L, casOd, casDo);
    }

    @Test
    @DisplayName("Zamítnutí rezervace při kolizi s existujícím termínem")
    void testZamítnutiKolizeTerminu() {
        OffsetDateTime casOd = OffsetDateTime.of(2026, 10, 12, 8, 15, 0, 0, ZoneOffset.ofHours(2));
        OffsetDateTime casDo = OffsetDateTime.of(2026, 10, 12, 8, 30, 0, 0, ZoneOffset.ofHours(2));

        RezervaceDto.Request req = new RezervaceDto.Request(1L, 1L, casOd, casDo, "Kontrola");

        when(pacientRepository.findById(1L)).thenReturn(Optional.of(pacient));
        when(lekarRepository.findById(1L)).thenReturn(Optional.of(lekar));

        Rezervace existujici = new Rezervace(pacient, lekar, casOd, casDo, "Původní");
        when(rezervaceRepository.najdiKolidujiciRezervace(eq(1L), eq(casOd), eq(casDo), isNull()))
                .thenReturn(List.of(existujici));

        assertThatThrownBy(() -> rezervaceService.vytvorRezervaci(req))
                .isInstanceOf(KolizeTerminuException.class)
                .hasMessageContaining("Kolize není povolena");

        verify(rezervaceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Zamítnutí času neodpovídajícího 15minutové mřížce")
    void testZamítnutiNeplatneMrizky() {
        OffsetDateTime casOd = OffsetDateTime.of(2026, 10, 12, 8, 7, 0, 0, ZoneOffset.ofHours(2));
        OffsetDateTime casDo = OffsetDateTime.of(2026, 10, 12, 8, 22, 0, 0, ZoneOffset.ofHours(2));

        RezervaceDto.Request req = new RezervaceDto.Request(1L, 1L, casOd, casDo, "Neplatný čas");

        assertThatThrownBy(() -> rezervaceService.vytvorRezervaci(req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("15minutové mřížce");
    }

    @Test
    @DisplayName("Storno rezervace uvolní slot a zachová záznam")
    void testStornoRezervace() {
        OffsetDateTime casOd = OffsetDateTime.of(2026, 10, 12, 9, 0, 0, 0, ZoneOffset.ofHours(2));
        OffsetDateTime casDo = OffsetDateTime.of(2026, 10, 12, 9, 15, 0, 0, ZoneOffset.ofHours(2));

        Rezervace r = new Rezervace(pacient, lekar, casOd, casDo, "Původní termín");
        r.setId(5L);

        when(rezervaceRepository.findById(5L)).thenReturn(Optional.of(r));

        RezervaceDto.Response res = rezervaceService.stornujRezervaci(5L);

        assertThat(res.stav()).isEqualTo(StavRezervace.ZRUSENA);
        assertThat(res.datumZruseni()).isNotNull();
        assertThat(r.getStav()).isEqualTo(StavRezervace.ZRUSENA);
    }
}

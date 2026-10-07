package cz.uhk.fim.ppro.ordinace.service;

import cz.uhk.fim.ppro.ordinace.dto.NavstevaDto;
import cz.uhk.fim.ppro.ordinace.exception.BusinessRuleException;
import cz.uhk.fim.ppro.ordinace.model.*;
import cz.uhk.fim.ppro.ordinace.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NavstevaServiceTest {

    @Mock
    private NavstevaRepository navstevaRepository;

    @Mock
    private RezervaceRepository rezervaceRepository;

    @Mock
    private PacientRepository pacientRepository;

    @Mock
    private LekarRepository lekarRepository;

    @Mock
    private VykonRepository vykonRepository;

    @InjectMocks
    private NavstevaService navstevaService;

    private Pacient pacient;
    private Lekar lekar;
    private Vykon vykonKomplex;
    private Vykon vykonOdber;

    @BeforeEach
    void setUp() {
        pacient = new Pacient("Jan", "Novák", 1980, "+420777111222", "8005121234", "111");
        pacient.setId(1L);

        lekar = new Lekar("MUDr.", "Jana", "Hrubá", true);
        lekar.setId(1L);

        vykonKomplex = new Vykon("09511", "Komplexní vyšetření", 250, LocalDate.now());
        vykonKomplex.setId(10L);

        vykonOdber = new Vykon("09115", "Odběr krve", 45, LocalDate.now());
        vykonOdber.setId(11L);
    }

    @Test
    @DisplayName("Návštěvu nelze zaevidovat do budoucnosti")
    void testNelzeEvidovatNavstevuDoBudoucna() {
        OffsetDateTime budoucnost = OffsetDateTime.now().plusDays(2);
        NavstevaDto.Request req = new NavstevaDto.Request(
                null, 1L, 1L, budoucnost, budoucnost.plusMinutes(20), "Poznámka", List.of()
        );

        assertThatThrownBy(() -> navstevaService.zaevidujNavstevu(req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("do budoucna");
    }

    @Test
    @DisplayName("Správný výpočet celkových bodů při evidenci návštěvy s výkony")
    void testVypocetBoduVykonu() {
        OffsetDateTime cas = OffsetDateTime.now().minusHours(1);

        NavstevaDto.PolozkaVykonuRequest p1 = new NavstevaDto.PolozkaVykonuRequest(10L, 1); // 250 bodů
        NavstevaDto.PolozkaVykonuRequest p2 = new NavstevaDto.PolozkaVykonuRequest(11L, 2); // 2 * 45 = 90 bodů

        NavstevaDto.Request req = new NavstevaDto.Request(
                null, 1L, 1L, cas, cas.plusMinutes(20), "Běžná kontrola", List.of(p1, p2)
        );

        when(pacientRepository.findById(1L)).thenReturn(Optional.of(pacient));
        when(lekarRepository.findById(1L)).thenReturn(Optional.of(lekar));
        when(vykonRepository.findById(10L)).thenReturn(Optional.of(vykonKomplex));
        when(vykonRepository.findById(11L)).thenReturn(Optional.of(vykonOdber));

        when(navstevaRepository.save(any(Navsteva.class))).thenAnswer(invocation -> {
            Navsteva n = invocation.getArgument(0);
            n.setId(100L);
            return n;
        });

        NavstevaDto.Response res = navstevaService.zaevidujNavstevu(req);

        assertThat(res).isNotNull();
        assertThat(res.celkemBodu()).isEqualTo(340); // 250 + 90
        assertThat(res.vykony()).hasSize(2);
    }
}

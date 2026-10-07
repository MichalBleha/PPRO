package cz.uhk.fim.ppro.ordinace.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

public class NavstevaDto {

    public record PolozkaVykonuRequest(
            @NotNull(message = "ID výkonu je povinné")
            Long vykonId,

            @NotNull(message = "Počet provedení je povinný")
            @Min(value = 1, message = "Počet musí být alespoň 1")
            Integer pocet
    ) {}

    public record Request(
            Long rezervaceId, // volitelné pro neplánované návštěvy

            @NotNull(message = "ID pacienta je povinné")
            Long pacientId,

            @NotNull(message = "ID lékaře je povinné")
            Long lekarId,

            @NotNull(message = "Začátek návštěvy je povinný")
            OffsetDateTime zacatekNavstevy,

            OffsetDateTime konecNavstevy,

            String zpravaLekare,

            List<PolozkaVykonuRequest> vykony
    ) {}

    public record PolozkaVykonuResponse(
            Long id,
            Long vykonId,
            String kodVykonu,
            String nazev,
            Integer pocet,
            Integer bodyZaJeden,
            Integer priznaneBody
    ) {}

    public record Response(
            Long id,
            Long rezervaceId,
            Long pacientId,
            String pacientCeleJmeno,
            String pacientCisloPojistence,
            String kodPojistovny,
            Long lekarId,
            String lekarCeleJmeno,
            OffsetDateTime zacatekNavstevy,
            OffsetDateTime konecNavstevy,
            String zpravaLekare,
            int celkemBodu,
            List<PolozkaVykonuResponse> vykony
    ) {}
}

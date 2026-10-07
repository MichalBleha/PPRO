package cz.uhk.fim.ppro.ordinace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public class VylukaLekareDto {

    public record Request(
            @NotNull(message = "ID lékaře je povinné")
            Long lekarId,

            @NotNull(message = "Datum a čas začátku výluky je povinný")
            OffsetDateTime datumOd,

            @NotNull(message = "Datum a čas konce výluky je povinný")
            OffsetDateTime datumDo,

            @NotBlank(message = "Důvod výluky je povinný")
            String duvod
    ) {}

    public record Response(
            Long id,
            Long lekarId,
            String lekarCeleJmeno,
            OffsetDateTime datumOd,
            OffsetDateTime datumDo,
            String duvod,
            int pocetDotcenychRezervaci
    ) {}
}

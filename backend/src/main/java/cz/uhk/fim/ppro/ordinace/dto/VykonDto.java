package cz.uhk.fim.ppro.ordinace.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class VykonDto {

    public record Request(
            @NotBlank(message = "Kód výkonu je povinný")
            String kodVykonu,

            @NotBlank(message = "Název výkonu je povinný")
            String nazev,

            @NotNull(message = "Počet bodů je povinný")
            @Min(value = 0, message = "Body nesmí být záporné")
            Integer body,

            @NotNull(message = "Platnost od je povinná")
            LocalDate platnostOd
    ) {}

    public record Response(
            Long id,
            String kodVykonu,
            String nazev,
            Integer body,
            LocalDate platnostOd
    ) {}
}

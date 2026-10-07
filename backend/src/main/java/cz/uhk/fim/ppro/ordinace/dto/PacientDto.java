package cz.uhk.fim.ppro.ordinace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public class PacientDto {

    public record Request(
            @NotBlank(message = "Jméno je povinné")
            String jmeno,

            @NotBlank(message = "Příjmení je povinné")
            String prijmeni,

            @NotNull(message = "Rok narození je povinný")
            Integer rokNarozeni,

            @NotBlank(message = "Telefon je povinný")
            String telefon,

            @NotBlank(message = "Číslo pojištěnce je povinné")
            String cisloPojistence,

            @NotBlank(message = "Kód pojišťovny je povinný")
            String kodPojistovny
    ) {}

    public record Response(
            Long id,
            String jmeno,
            String prijmeni,
            Integer rokNarozeni,
            String telefon,
            String cisloPojistence,
            String kodPojistovny,
            OffsetDateTime vytvoreno,
            long pocetZrusenychRezervaci
    ) {}
}

package cz.uhk.fim.ppro.ordinace.dto;

import cz.uhk.fim.ppro.ordinace.model.StavRezervace;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public class RezervaceDto {

    public record Request(
            @NotNull(message = "ID pacienta je povinné")
            Long pacientId,

            @NotNull(message = "ID lékaře je povinné")
            Long lekarId,

            @NotNull(message = "Začátek termínu je povinný")
            OffsetDateTime casOd,

            @NotNull(message = "Konec termínu je povinný")
            OffsetDateTime casDo,

            String poznamka
    ) {}

    public record Response(
            Long id,
            Long pacientId,
            String pacientJmeno,
            String pacientPrijmeni,
            String pacientTelefon,
            String pacientCisloPojistence,
            Long lekarId,
            String lekarCeleJmeno,
            OffsetDateTime casOd,
            OffsetDateTime casDo,
            StavRezervace stav,
            String poznamka,
            OffsetDateTime datumZruseni
    ) {}
}

package cz.uhk.fim.ppro.ordinace.dto;

import java.time.OffsetDateTime;

public record SlotDto(
        OffsetDateTime casOd,
        OffsetDateTime casDo,
        boolean volno,
        String duvodObsazeni, // null, "REZERVOVANO", "VYLUKA", "MIMO_ORDINACNI_DOBU"
        Long rezervaceId,
        String pacientCeleJmeno,
        String poznamka
) {}

package cz.uhk.fim.ppro.ordinace.dto;

import java.time.LocalTime;
import java.util.List;

public class LekarDto {

    public record Response(
            Long id,
            String titul,
            String jmeno,
            String prijmeni,
            String celeJmeno,
            Boolean aktivni,
            List<OrdinacniHodinaResponse> ordinacniHodiny
    ) {}

    public record OrdinacniHodinaResponse(
            Long id,
            Integer denVTydnu,
            LocalTime casOd,
            LocalTime casDo,
            String pracovisteAdresa,
            Boolean lzeObjednat
    ) {}
}

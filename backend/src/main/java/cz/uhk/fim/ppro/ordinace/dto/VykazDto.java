package cz.uhk.fim.ppro.ordinace.dto;

import java.util.List;

public class VykazDto {

    public record PolozkaVykazu(
            String kodVykonu,
            String nazevVykonu,
            int celkemProvedeno,
            int bodyZaJednotku,
            int celkemBodu
    ) {}

    public record MesicniPrehled(
            int rok,
            int mesic,
            Long lekarId,
            String lekarCeleJmeno,
            int celkemNavstev,
            int celkemBodu,
            List<PolozkaVykazu> vykony
    ) {}
}

package cz.uhk.fim.ppro.ordinace.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "rezervace", indexes = {
    @Index(name = "idx_rezervace_lekar_termin", columnList = "lekar_id, cas_od, cas_do, stav"),
    @Index(name = "idx_rezervace_pacient", columnList = "pacient_id")
})
public class Rezervace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pacient_id", nullable = false)
    private Pacient pacient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lekar_id", nullable = false)
    private Lekar lekar;

    @Column(name = "cas_od", nullable = false)
    private OffsetDateTime casOd;

    @Column(name = "cas_do", nullable = false)
    private OffsetDateTime casDo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StavRezervace stav = StavRezervace.PLANOVANA;

    @Column(columnDefinition = "TEXT")
    private String poznamka;

    @Column(name = "datum_zruseni")
    private OffsetDateTime datumZruseni;

    public Rezervace() {
    }

    public Rezervace(Pacient pacient, Lekar lekar, OffsetDateTime casOd, OffsetDateTime casDo, String poznamka) {
        this.pacient = pacient;
        this.lekar = lekar;
        this.casOd = casOd;
        this.casDo = casDo;
        this.poznamka = poznamka;
        this.stav = StavRezervace.PLANOVANA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pacient getPacient() {
        return pacient;
    }

    public void setPacient(Pacient pacient) {
        this.pacient = pacient;
    }

    public Lekar getLekar() {
        return lekar;
    }

    public void setLekar(Lekar lekar) {
        this.lekar = lekar;
    }

    public OffsetDateTime getCasOd() {
        return casOd;
    }

    public void setCasOd(OffsetDateTime casOd) {
        this.casOd = casOd;
    }

    public OffsetDateTime getCasDo() {
        return casDo;
    }

    public void setCasDo(OffsetDateTime casDo) {
        this.casDo = casDo;
    }

    public StavRezervace getStav() {
        return stav;
    }

    public void setStav(StavRezervace stav) {
        this.stav = stav;
    }

    public String getPoznamka() {
        return poznamka;
    }

    public void setPoznamka(String poznamka) {
        this.poznamka = poznamka;
    }

    public OffsetDateTime getDatumZruseni() {
        return datumZruseni;
    }

    public void setDatumZruseni(OffsetDateTime datumZruseni) {
        this.datumZruseni = datumZruseni;
    }

    public void stornovat() {
        this.stav = StavRezervace.ZRUSENA;
        this.datumZruseni = OffsetDateTime.now();
    }
}

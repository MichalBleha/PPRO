package cz.uhk.fim.ppro.ordinace.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "navsteva", indexes = {
    @Index(name = "idx_navsteva_pacient", columnList = "pacient_id"),
    @Index(name = "idx_navsteva_lekar_datum", columnList = "lekar_id, zacatek_navstevy")
})
public class Navsteva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rezervace_id")
    private Rezervace rezervace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pacient_id", nullable = false)
    private Pacient pacient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lekar_id", nullable = false)
    private Lekar lekar;

    @Column(name = "zacatek_navstevy", nullable = false)
    private OffsetDateTime zacatekNavstevy;

    @Column(name = "konec_navstevy")
    private OffsetDateTime konecNavstevy;

    @Column(name = "zprava_lekare", columnDefinition = "TEXT")
    private String zpravaLekare;

    @OneToMany(mappedBy = "navsteva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NavstevaVykon> vykony = new ArrayList<>();

    public Navsteva() {
    }

    public Navsteva(Rezervace rezervace, Pacient pacient, Lekar lekar, OffsetDateTime zacatekNavstevy, OffsetDateTime konecNavstevy, String zpravaLekare) {
        this.rezervace = rezervace;
        this.pacient = pacient;
        this.lekar = lekar;
        this.zacatekNavstevy = zacatekNavstevy;
        this.konecNavstevy = konecNavstevy;
        this.zpravaLekare = zpravaLekare;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Rezervace getRezervace() {
        return rezervace;
    }

    public void setRezervace(Rezervace rezervace) {
        this.rezervace = rezervace;
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

    public OffsetDateTime getZacatekNavstevy() {
        return zacatekNavstevy;
    }

    public void setZacatekNavstevy(OffsetDateTime zacatekNavstevy) {
        this.zacatekNavstevy = zacatekNavstevy;
    }

    public OffsetDateTime getKonecNavstevy() {
        return konecNavstevy;
    }

    public void setKonecNavstevy(OffsetDateTime konecNavstevy) {
        this.konecNavstevy = konecNavstevy;
    }

    public String getZpravaLekare() {
        return zpravaLekare;
    }

    public void setZpravaLekare(String zpravaLekare) {
        this.zpravaLekare = zpravaLekare;
    }

    public List<NavstevaVykon> getVykony() {
        return vykony;
    }

    public void setVykony(List<NavstevaVykon> vykony) {
        this.vykony = vykony;
    }

    public void pridejVykon(NavstevaVykon navstevaVykon) {
        this.vykony.add(navstevaVykon);
        navstevaVykon.setNavsteva(this);
    }
}

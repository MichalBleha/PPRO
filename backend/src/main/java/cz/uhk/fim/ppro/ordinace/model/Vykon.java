package cz.uhk.fim.ppro.ordinace.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vykon")
public class Vykon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kod_vykonu", nullable = false, unique = true, length = 20)
    private String kodVykonu;

    @Column(nullable = false, length = 255)
    private String nazev;

    @Column(nullable = false)
    private Integer body;

    @Column(name = "platnost_od", nullable = false)
    private LocalDate platnostOd;

    public Vykon() {
    }

    public Vykon(String kodVykonu, String nazev, Integer body, LocalDate platnostOd) {
        this.kodVykonu = kodVykonu;
        this.nazev = nazev;
        this.body = body;
        this.platnostOd = platnostOd;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKodVykonu() {
        return kodVykonu;
    }

    public void setKodVykonu(String kodVykonu) {
        this.kodVykonu = kodVykonu;
    }

    public String getNazev() {
        return nazev;
    }

    public void setNazev(String nazev) {
        this.nazev = nazev;
    }

    public Integer getBody() {
        return body;
    }

    public void setBody(Integer body) {
        this.body = body;
    }

    public LocalDate getPlatnostOd() {
        return platnostOd;
    }

    public void setPlatnostOd(LocalDate platnostOd) {
        this.platnostOd = platnostOd;
    }
}

package cz.uhk.fim.ppro.ordinace.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pacient", indexes = {
    @Index(name = "idx_pacient_prijmeni", columnList = "prijmeni"),
    @Index(name = "idx_pacient_telefon", columnList = "telefon"),
    @Index(name = "idx_pacient_search", columnList = "prijmeni, telefon")
})
public class Pacient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String jmeno;

    @Column(nullable = false, length = 100)
    private String prijmeni;

    @Column(name = "rok_narozeni", nullable = false)
    private Integer rokNarozeni;

    @Column(nullable = false, length = 20)
    private String telefon;

    @Column(name = "cislo_pojistence", nullable = false, unique = true, length = 20)
    private String cisloPojistence;

    @Column(name = "kod_pojistovny", nullable = false, length = 10)
    private String kodPojistovny;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime vytvoreno;

    @PrePersist
    protected void onCreate() {
        if (vytvoreno == null) {
            vytvoreno = OffsetDateTime.now();
        }
    }

    public Pacient() {
    }

    public Pacient(String jmeno, String prijmeni, Integer rokNarozeni, String telefon, String cisloPojistence, String kodPojistovny) {
        this.jmeno = jmeno;
        this.prijmeni = prijmeni;
        this.rokNarozeni = rokNarozeni;
        this.telefon = telefon;
        this.cisloPojistence = cisloPojistence;
        this.kodPojistovny = kodPojistovny;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJmeno() {
        return jmeno;
    }

    public void setJmeno(String jmeno) {
        this.jmeno = jmeno;
    }

    public String getPrijmeni() {
        return prijmeni;
    }

    public void setPrijmeni(String prijmeni) {
        this.prijmeni = prijmeni;
    }

    public Integer getRokNarozeni() {
        return rokNarozeni;
    }

    public void setRokNarozeni(Integer rokNarozeni) {
        this.rokNarozeni = rokNarozeni;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getCisloPojistence() {
        return cisloPojistence;
    }

    public void setCisloPojistence(String cisloPojistence) {
        this.cisloPojistence = cisloPojistence;
    }

    public String getKodPojistovny() {
        return kodPojistovny;
    }

    public void setKodPojistovny(String kodPojistovny) {
        this.kodPojistovny = kodPojistovny;
    }

    public OffsetDateTime getVytvoreno() {
        return vytvoreno;
    }

    public void setVytvoreno(OffsetDateTime vytvoreno) {
        this.vytvoreno = vytvoreno;
    }
}

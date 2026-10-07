package cz.uhk.fim.ppro.ordinace.model;

import jakarta.persistence.*;

@Entity
@Table(name = "lekar")
public class Lekar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String titul;

    @Column(nullable = false, length = 100)
    private String jmeno;

    @Column(nullable = false, length = 100)
    private String prijmeni;

    @Column(nullable = false)
    private Boolean aktivni = true;

    public Lekar() {
    }

    public Lekar(String titul, String jmeno, String prijmeni, Boolean aktivni) {
        this.titul = titul;
        this.jmeno = jmeno;
        this.prijmeni = prijmeni;
        this.aktivni = aktivni != null ? aktivni : true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitul() {
        return titul;
    }

    public void setTitul(String titul) {
        this.titul = titul;
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

    public Boolean getAktivni() {
        return aktivni;
    }

    public void setAktivni(Boolean aktivni) {
        this.aktivni = aktivni;
    }

    public String getCeleJmeno() {
        if (titul != null && !titul.isBlank()) {
            return titul + " " + jmeno + " " + prijmeni;
        }
        return jmeno + " " + prijmeni;
    }
}

package cz.uhk.fim.ppro.ordinace.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "vyluka_lekare", indexes = {
    @Index(name = "idx_vyluka_lekare_lekar_interval", columnList = "lekar_id, datum_od, datum_do")
})
public class VylukaLekare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lekar_id", nullable = false)
    private Lekar lekar;

    @Column(name = "datum_od", nullable = false)
    private OffsetDateTime datumOd;

    @Column(name = "datum_do", nullable = false)
    private OffsetDateTime datumDo;

    @Column(nullable = false, length = 255)
    private String duvod;

    public VylukaLekare() {
    }

    public VylukaLekare(Lekar lekar, OffsetDateTime datumOd, OffsetDateTime datumDo, String duvod) {
        this.lekar = lekar;
        this.datumOd = datumOd;
        this.datumDo = datumDo;
        this.duvod = duvod;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Lekar getLekar() {
        return lekar;
    }

    public void setLekar(Lekar lekar) {
        this.lekar = lekar;
    }

    public OffsetDateTime getDatumOd() {
        return datumOd;
    }

    public void setDatumOd(OffsetDateTime datumOd) {
        this.datumOd = datumOd;
    }

    public OffsetDateTime getDatumDo() {
        return datumDo;
    }

    public void setDatumDo(OffsetDateTime datumDo) {
        this.datumDo = datumDo;
    }

    public String getDuvod() {
        return duvod;
    }

    public void setDuvod(String duvod) {
        this.duvod = duvod;
    }
}

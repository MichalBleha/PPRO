package cz.uhk.fim.ppro.ordinace.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "ordinacni_hodina", indexes = {
    @Index(name = "idx_ordinacni_hodina_lekar", columnList = "lekar_id, den_v_tydnu")
})
public class OrdinacniHodina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lekar_id", nullable = false)
    private Lekar lekar;

    @Column(name = "den_v_tydnu", nullable = false)
    private Integer denVTydnu; // 1 = Pondělí ... 7 = Neděle

    @Column(name = "cas_od", nullable = false)
    private LocalTime casOd;

    @Column(name = "cas_do", nullable = false)
    private LocalTime casDo;

    @Column(name = "pracoviste_adresa", nullable = false, length = 255)
    private String pracovisteAdresa;

    @Column(name = "lze_objednat", nullable = false)
    private Boolean lzeObjednat = true;

    public OrdinacniHodina() {
    }

    public OrdinacniHodina(Lekar lekar, Integer denVTydnu, LocalTime casOd, LocalTime casDo, String pracovisteAdresa, Boolean lzeObjednat) {
        this.lekar = lekar;
        this.denVTydnu = denVTydnu;
        this.casOd = casOd;
        this.casDo = casDo;
        this.pracovisteAdresa = pracovisteAdresa;
        this.lzeObjednat = lzeObjednat != null ? lzeObjednat : true;
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

    public Integer getDenVTydnu() {
        return denVTydnu;
    }

    public void setDenVTydnu(Integer denVTydnu) {
        this.denVTydnu = denVTydnu;
    }

    public LocalTime getCasOd() {
        return casOd;
    }

    public void setCasOd(LocalTime casOd) {
        this.casOd = casOd;
    }

    public LocalTime getCasDo() {
        return casDo;
    }

    public void setCasDo(LocalTime casDo) {
        this.casDo = casDo;
    }

    public String getPracovisteAdresa() {
        return pracovisteAdresa;
    }

    public void setPracovisteAdresa(String pracovisteAdresa) {
        this.pracovisteAdresa = pracovisteAdresa;
    }

    public Boolean getLzeObjednat() {
        return lzeObjednat;
    }

    public void setLzeObjednat(Boolean lzeObjednat) {
        this.lzeObjednat = lzeObjednat;
    }
}

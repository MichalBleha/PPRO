package cz.uhk.fim.ppro.ordinace.model;

import jakarta.persistence.*;

@Entity
@Table(name = "navsteva_vykon", uniqueConstraints = {
    @UniqueConstraint(name = "uq_navsteva_vykon", columnNames = {"navsteva_id", "vykon_id"})
}, indexes = {
    @Index(name = "idx_navsteva_vykon_navsteva", columnList = "navsteva_id")
})
public class NavstevaVykon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "navsteva_id", nullable = false)
    private Navsteva navsteva;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vykon_id", nullable = false)
    private Vykon vykon;

    @Column(nullable = false)
    private Integer pocet = 1;

    @Column(name = "priznane_body", nullable = false)
    private Integer priznaneBody;

    public NavstevaVykon() {
    }

    public NavstevaVykon(Navsteva navsteva, Vykon vykon, Integer pocet, Integer priznaneBody) {
        this.navsteva = navsteva;
        this.vykon = vykon;
        this.pocet = pocet != null ? pocet : 1;
        this.priznaneBody = priznaneBody;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Navsteva getNavsteva() {
        return navsteva;
    }

    public void setNavsteva(Navsteva navsteva) {
        this.navsteva = navsteva;
    }

    public Vykon getVykon() {
        return vykon;
    }

    public void setVykon(Vykon vykon) {
        this.vykon = vykon;
    }

    public Integer getPocet() {
        return pocet;
    }

    public void setPocet(Integer pocet) {
        this.pocet = pocet;
    }

    public Integer getPriznaneBody() {
        return priznaneBody;
    }

    public void setPriznaneBody(Integer priznaneBody) {
        this.priznaneBody = priznaneBody;
    }
}

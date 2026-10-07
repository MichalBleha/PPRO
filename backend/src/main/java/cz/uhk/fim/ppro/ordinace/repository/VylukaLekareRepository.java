package cz.uhk.fim.ppro.ordinace.repository;

import cz.uhk.fim.ppro.ordinace.model.VylukaLekare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface VylukaLekareRepository extends JpaRepository<VylukaLekare, Long> {

    List<VylukaLekare> findByLekarIdOrderByDatumOdAsc(Long lekarId);

    @Query("SELECT v FROM VylukaLekare v WHERE v.lekar.id = :lekarId AND " +
           "(v.datumOd < :konec AND v.datumDo > :zacatek)")
    List<VylukaLekare> najdiKolidujiciVyluky(
            @Param("lekarId") Long lekarId,
            @Param("zacatek") OffsetDateTime zacatek,
            @Param("konec") OffsetDateTime konec
    );
}

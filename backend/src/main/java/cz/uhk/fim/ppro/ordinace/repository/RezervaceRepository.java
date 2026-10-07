package cz.uhk.fim.ppro.ordinace.repository;

import cz.uhk.fim.ppro.ordinace.model.Rezervace;
import cz.uhk.fim.ppro.ordinace.model.StavRezervace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface RezervaceRepository extends JpaRepository<Rezervace, Long> {

    @Query("SELECT r FROM Rezervace r WHERE r.lekar.id = :lekarId AND " +
           "r.stav = 'PLANOVANA' AND " +
           "(r.casOd < :casDo AND r.casDo > :casOd) AND " +
           "(:excludeId IS NULL OR r.id != :excludeId)")
    List<Rezervace> najdiKolidujiciRezervace(
            @Param("lekarId") Long lekarId,
            @Param("casOd") OffsetDateTime casOd,
            @Param("casDo") OffsetDateTime casDo,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT r FROM Rezervace r WHERE r.lekar.id = :lekarId AND " +
           "r.casOd >= :od AND r.casDo <= :do AND r.stav != 'ZRUSENA' " +
           "ORDER BY r.casOd ASC")
    List<Rezervace> najdiAktivniRezervaceLekareVObdobi(
            @Param("lekarId") Long lekarId,
            @Param("od") OffsetDateTime od,
            @Param("do") OffsetDateTime doCas
    );

    List<Rezervace> findByPacientIdOrderByCasOdDesc(Long pacientId);

    long countByPacientIdAndStav(Long pacientId, StavRezervace stav);

    List<Rezervace> findByStavOrderByCasOdAsc(StavRezervace stav);
}

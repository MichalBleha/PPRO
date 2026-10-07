package cz.uhk.fim.ppro.ordinace.repository;

import cz.uhk.fim.ppro.ordinace.model.Navsteva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface NavstevaRepository extends JpaRepository<Navsteva, Long> {

    List<Navsteva> findByPacientIdOrderByZacatekNavstevyDesc(Long pacientId);

    @Query("SELECT DISTINCT n FROM Navsteva n " +
           "LEFT JOIN FETCH n.vykony nv " +
           "LEFT JOIN FETCH nv.vykon " +
           "WHERE (:lekarId IS NULL OR n.lekar.id = :lekarId) AND " +
           "n.zacatekNavstevy >= :od AND n.zacatekNavstevy <= :do " +
           "ORDER BY n.zacatekNavstevy DESC")
    List<Navsteva> najdiNavstevySProvedenymiVykony(
            @Param("lekarId") Long lekarId,
            @Param("od") OffsetDateTime od,
            @Param("do") OffsetDateTime doCas
    );
}

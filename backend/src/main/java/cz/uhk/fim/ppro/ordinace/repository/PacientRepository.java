package cz.uhk.fim.ppro.ordinace.repository;

import cz.uhk.fim.ppro.ordinace.model.Pacient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacientRepository extends JpaRepository<Pacient, Long> {

    Optional<Pacient> findByCisloPojistence(String cisloPojistence);

    boolean existsByCisloPojistence(String cisloPojistence);

    @Query("SELECT p FROM Pacient p WHERE " +
           "LOWER(p.prijmeni) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "p.telefon LIKE CONCAT('%', :query, '%') OR " +
           "p.cisloPojistence LIKE CONCAT('%', :query, '%') " +
           "ORDER BY p.prijmeni, p.jmeno")
    List<Pacient> hledejPacienty(@Param("query") String query);
}

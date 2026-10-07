package cz.uhk.fim.ppro.ordinace.repository;

import cz.uhk.fim.ppro.ordinace.model.Vykon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VykonRepository extends JpaRepository<Vykon, Long> {

    Optional<Vykon> findByKodVykonu(String kodVykonu);

    boolean existsByKodVykonu(String kodVykonu);
}

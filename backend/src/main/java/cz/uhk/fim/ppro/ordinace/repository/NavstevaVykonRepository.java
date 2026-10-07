package cz.uhk.fim.ppro.ordinace.repository;

import cz.uhk.fim.ppro.ordinace.model.NavstevaVykon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NavstevaVykonRepository extends JpaRepository<NavstevaVykon, Long> {

    List<NavstevaVykon> findByNavstevaId(Long navstevaId);
}

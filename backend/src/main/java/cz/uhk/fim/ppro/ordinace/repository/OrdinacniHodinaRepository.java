package cz.uhk.fim.ppro.ordinace.repository;

import cz.uhk.fim.ppro.ordinace.model.OrdinacniHodina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdinacniHodinaRepository extends JpaRepository<OrdinacniHodina, Long> {

    List<OrdinacniHodina> findByLekarIdAndDenVTydnu(Long lekarId, Integer denVTydnu);

    List<OrdinacniHodina> findByLekarIdOrderByDenVTydnuAscCasOdAsc(Long lekarId);
}

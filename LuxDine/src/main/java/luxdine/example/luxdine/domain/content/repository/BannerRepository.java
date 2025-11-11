package luxdine.example.luxdine.domain.content.repository;

import luxdine.example.luxdine.domain.content.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BannerRepository extends JpaRepository<Banner,Long> {

}

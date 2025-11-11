package luxdine.example.luxdine.domain.user.repository;


import luxdine.example.luxdine.domain.user.entity.VipTiers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VipTierRepository extends JpaRepository<VipTiers,Long> {
}

package luxdine.example.luxdine.domain.feedback.repository;

import luxdine.example.luxdine.domain.feedback.entity.FeedBacks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedBacks, Long> {

}

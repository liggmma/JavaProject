package luxdine.example.luxdine.service.feedback;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.feedback.entity.FeedBacks;
import luxdine.example.luxdine.domain.feedback.repository.FeedbackRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedBackService {
    FeedbackRepository feedBackRepository;

    public List<FeedBacks> get10RecentFeedBacks() {
        return feedBackRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate")).stream().limit(10).toList();
    }
}

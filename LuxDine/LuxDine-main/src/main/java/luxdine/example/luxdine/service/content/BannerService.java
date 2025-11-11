package luxdine.example.luxdine.service.content;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.content.dto.request.BannerCreationRequest;
import luxdine.example.luxdine.domain.content.entity.Banner;
import luxdine.example.luxdine.mapper.BannerMapper;
import luxdine.example.luxdine.domain.content.repository.BannerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BannerService {

    private BannerMapper bannerMapper;
    private BannerRepository bannerRepository;

    public String createBanner(BannerCreationRequest request){

        Banner banner = bannerMapper.toBanner(request);
        try {
            banner = bannerRepository.save(banner);
        } catch (DataIntegrityViolationException exception) {
            return "Error: " + exception.getMessage();
        }
        return banner.getId().toString();
    }

    public List<Banner> getAllBanner(){
        return bannerRepository.findAll().stream().toList();
    }

    public List<Banner> getAllActiveBanner(){
        List<Banner> result = new java.util.ArrayList<>(bannerRepository.findAll().stream().toList());
        result.removeIf(banner -> !banner.isActive());
        return result;
    }

    public void deleteBanner(long id){
        bannerRepository.deleteById(id);
    }


}

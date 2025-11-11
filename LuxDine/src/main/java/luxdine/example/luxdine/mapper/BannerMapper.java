package luxdine.example.luxdine.mapper;

import luxdine.example.luxdine.domain.content.dto.request.BannerCreationRequest;
import luxdine.example.luxdine.domain.content.entity.Banner;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BannerMapper {
    Banner toBanner(BannerCreationRequest bannerCreationRequest);
}

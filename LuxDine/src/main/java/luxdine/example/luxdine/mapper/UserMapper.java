package luxdine.example.luxdine.mapper;

import luxdine.example.luxdine.domain.user.dto.request.ProfileUpdateRequest;
import luxdine.example.luxdine.domain.user.dto.request.UserCreationRequest;
import luxdine.example.luxdine.domain.user.dto.response.UserResponse;
import luxdine.example.luxdine.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    User toUser(UserCreationRequest userCreationRequest);
    UserResponse toUserResponse(User user);
    void updateUserProfile(@MappingTarget User user, ProfileUpdateRequest request);
}

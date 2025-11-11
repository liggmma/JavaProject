package luxdine.example.luxdine.mapper;

import luxdine.example.luxdine.domain.order.dto.response.OrderResponse;
import luxdine.example.luxdine.domain.order.entity.Orders;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {
    OrderResponse toOrderResponse(Orders orders);
}

package luxdine.example.luxdine.mapper;

import lombok.RequiredArgsConstructor;
import luxdine.example.luxdine.domain.catalog.dto.response.BundleDetailResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.BundleItemResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.BundleResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.CategoryResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.MenuItemResponse;
import luxdine.example.luxdine.domain.catalog.entity.BundleItems;
import luxdine.example.luxdine.domain.catalog.entity.Bundles;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BundleMapper {

    /**
     * Convert Bundles entity → BundleResponse (danh sách)
     */
    public BundleResponse toBundleResponse(Bundles bundle) {
        if (bundle == null) return null;

        List<BundleItemResponse> itemResponses = bundle.getBundleItems() != null
                ? bundle.getBundleItems().stream()
                .map(this::toBundleItemResponse)
                .collect(Collectors.toList())
                : List.of();

        return BundleResponse.builder()
                .id(bundle.getId())
                .name(bundle.getName())
                .description(bundle.getDescription())
                .price(bundle.getPrice())
                .slug(bundle.getSlug())
                .imageUrl(bundle.getImageUrl())
                .isActive(bundle.isActive())
                .bundleType(bundle.getBundleType())
                .creationDate(Date.from(bundle.getCreationDate()))
                .updatedDate(Date.from(bundle.getUpdatedDate()))
                .items(itemResponses)
                .build();
    }

    /**
     * Convert Bundles entity → BundleDetailResponse (chi tiết)
     */
    public BundleDetailResponse toBundleDetailResponse(Bundles bundle) {
        if (bundle == null) return null;

        List<BundleItemResponse> itemResponses = bundle.getBundleItems() != null
                ? bundle.getBundleItems().stream()
                .map(this::toBundleItemResponse)
                .collect(Collectors.toList())
                : List.of();

        return BundleDetailResponse.builder()
                .id(bundle.getId())
                .name(bundle.getName())
                .description(bundle.getDescription())
                .price(bundle.getPrice())
                .slug(bundle.getSlug())
                .imageUrl(bundle.getImageUrl())
                .isActive(bundle.isActive())
                .bundleType(bundle.getBundleType().name())
                .creationDate(Date.from(bundle.getCreationDate()))
                .updatedDate(Date.from(bundle.getUpdatedDate()))
                .items(itemResponses)
                .totalItems(itemResponses.size())
                .build();
    }

    /**
     * Convert BundleItems entity → BundleItemResponse
     */
    private BundleItemResponse toBundleItemResponse(BundleItems bundleItem) {
        if (bundleItem == null || bundleItem.getItem() == null) return null;

        // Tạo CategoryResponse nếu có category
        CategoryResponse categoryResponse = null;
        if (bundleItem.getItem().getCategory() != null) {
            categoryResponse = CategoryResponse.builder()
                    .id(bundleItem.getItem().getCategory().getId())
                    .name(bundleItem.getItem().getCategory().getName())
                    .slug(bundleItem.getItem().getCategory().getSlug())
                    .items(null)
                    .build();
        }

        // ← CHỈ MAP CÁC FIELD CÓ TRONG Items ENTITY
        MenuItemResponse menuItemResponse = MenuItemResponse.builder()
                .id(bundleItem.getItem().getId())
                .name(bundleItem.getItem().getName())
                .description(bundleItem.getItem().getDescription())
                .price(bundleItem.getItem().getPrice())
                .imageUrl(bundleItem.getItem().getImageUrl())
                .slug(bundleItem.getItem().getSlug())
                .category(categoryResponse)
                // ← CÁC FIELD SAU SET NULL/DEFAULT VÌ KHÔNG CÓ TRONG Items ENTITY
                .soldCount(null)           // Nếu Items không có field này
                .visibility(null)          // Nếu Items không có field này
                .isAvailable(null)         // Nếu Items không có field này
                .createdAt(null)           // Nếu Items không có field này
                .updatedAt(null)           // Nếu Items không có field này
                .allergens(null)           // Nếu Items không có field này
                .build();

        return BundleItemResponse.builder()
                .id(bundleItem.getId())
                .item(menuItemResponse)
                .quantity(bundleItem.getQuantity())
                .sortOrder(bundleItem.getSortOrder())
                .build();
    }
}
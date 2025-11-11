package luxdine.example.luxdine.service.catalog;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.common.util.SlugUtils;
import luxdine.example.luxdine.domain.catalog.dto.request.CreateBundleRequest;
import luxdine.example.luxdine.domain.catalog.dto.request.UpdateBundleRequest;
import luxdine.example.luxdine.domain.catalog.dto.response.BundleResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.BundleDetailResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.BundleItemResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.MenuItemResponse;
import luxdine.example.luxdine.domain.catalog.entity.Bundles;
import luxdine.example.luxdine.domain.catalog.entity.BundleItems;
import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.catalog.enums.BundleType;
import luxdine.example.luxdine.domain.catalog.repository.BundlesRepository;
import luxdine.example.luxdine.domain.catalog.repository.BundleItemsRepository;
import luxdine.example.luxdine.domain.catalog.repository.ItemsRepository;
import luxdine.example.luxdine.exception.AppException;
import luxdine.example.luxdine.exception.ErrorCode;

import luxdine.example.luxdine.service.CloudinaryService;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BundleService {

    BundlesRepository bundlesRepository;
    BundleItemsRepository bundleItemsRepository;
    ItemsRepository itemsRepository;
    CloudinaryService cloudinaryService;

        // ==================== PUBLIC/CUSTOMER METHODS ====================

    public List<Bundles> getAllActiveBundlesEntity() {
        return bundlesRepository.findByIsActiveTrue();
    }

    // Lấy danh sách tất cả bundles đang hoạt động
    public List<BundleResponse> getAllBundles() {
        return bundlesRepository.findByIsActiveTrue().stream()
                .map(this::mapToBundleResponse)
                .collect(Collectors.toList());
    }

    // Lấy chi tiết theo ID
    public Optional<BundleDetailResponse> getBundleDetailById(Long id) {
        return bundlesRepository.findById(id)
                .map(this::mapToBundleDetailResponse);
    }

    // Lấy chi tiết theo slug
    public Optional<BundleDetailResponse> getBundleDetailBySlug(String slug) {
        return bundlesRepository.findBySlug(slug)
                .map(this::mapToBundleDetailResponse);
    }

    

    // ================= Mapping methods =================

    private BundleResponse mapToBundleResponse(Bundles bundle) {
        List<BundleItemResponse> items = bundleItemsRepository
                .findByBundleOrderBySortOrder(bundle).stream()
                .map(this::mapToBundleItemResponse)
                .collect(Collectors.toList());

        return BundleResponse.builder()
                .id(bundle.getId())
                .name(bundle.getName())
                .description(bundle.getDescription())
                .price(bundle.getPrice())
                .slug(bundle.getSlug())
                .imageUrl(bundle.getImageUrl())
                .isActive(bundle.isActive())
                .bundleType(bundle.getBundleType())
                .items(items)
                .build();
    }

    private BundleDetailResponse mapToBundleDetailResponse(Bundles bundle) {
        List<BundleItemResponse> items = bundleItemsRepository
                .findByBundleOrderBySortOrder(bundle).stream()
                .map(this::mapToBundleItemResponse)
                .collect(Collectors.toList());

        // Mock data (có thể thay bằng dữ liệu thật nếu có)
        List<String> highlights = List.of(
                "Chef's Special",
                "Seasonal Ingredients",
                "Premium Selection"
        );

        return BundleDetailResponse.builder()
                .id(bundle.getId())
                .name(bundle.getName())
                .description(bundle.getDescription())
                .price(bundle.getPrice())
                .slug(bundle.getSlug())
                .imageUrl(bundle.getImageUrl())
                .isActive(bundle.isActive())
                .bundleType(bundle.getBundleType().name()) // ép Enum sang String
                .items(items)
                .totalItems(items.size())
                .build();

    }

    private BundleItemResponse mapToBundleItemResponse(BundleItems bundleItem) {
        MenuItemResponse item = MenuItemResponse.builder()
                .id(bundleItem.getItem().getId())
                .name(bundleItem.getItem().getName())
                .description(bundleItem.getItem().getDescription())
                .price(bundleItem.getItem().getPrice())
                .imageUrl(bundleItem.getItem().getImageUrl())
                .isAvailable(bundleItem.getItem().isAvailable())
                .build();

        return BundleItemResponse.builder()
                .id(bundleItem.getId())
                .item(item)
                .quantity(bundleItem.getQuantity())
                .sortOrder(bundleItem.getSortOrder())
                .build();
    }
    // 🟢 Lấy tất cả bundle theo loại (ví dụ: COMBO, TASTING_MENU)
    public List<BundleResponse> getBundlesByType(BundleType type) {
        return bundlesRepository.findByBundleTypeAndIsActiveTrue(type).stream()
                .map(this::mapToBundleResponse)
                .collect(Collectors.toList());
    }

    //Lấy hết combo active/inactive
    @Transactional(readOnly = true)
        public List<BundleResponse> getBundlesByTypeForAdmin(String type, boolean includeInactive) {
            BundleType bundleType = BundleType.valueOf(type.toUpperCase());
            List<Bundles> bundles = includeInactive
                    ? bundlesRepository.findByBundleType(bundleType)
                    : bundlesRepository.findByBundleTypeAndIsActiveTrue(bundleType);
            return bundles.stream()
                    .map(this::mapToBundleResponse)
                    .collect(Collectors.toList());
        }

    // 🟢 Lấy tất cả COMBO bundles
    public List<BundleResponse> getAllComboBundles() {
        return getBundlesByType(BundleType.COMBO);
    }

     // ==================== ADMIN METHODS (CRUD) ====================

    /**
     * [ADMIN] Lấy tất cả bundles (kể cả inactive)
     */
    public List<BundleResponse> getAllBundlesForAdmin() {
        return bundlesRepository.findAll().stream()
                .map(this::mapToBundleResponse)
                .collect(Collectors.toList());
    }

    /**
     * [ADMIN] Lấy bundle theo ID (không filter active)
     */
    public BundleDetailResponse getBundleByIdForAdmin(Long id) {
        Bundles bundle = bundlesRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BUNDLE_NOT_FOUND));
        return mapToBundleDetailResponse(bundle);
    }

    /**
     * [ADMIN] Tạo bundle mới
     */
    @Transactional
    public BundleDetailResponse createBundle(CreateBundleRequest request, MultipartFile imageFile) {
        log.info("Creating new bundle: {}", request.getName());

        String imageUrl;
        try {
            // Kiểm tra file có tồn tại không
            if (imageFile == null || imageFile.isEmpty()) {
                throw new AppException(ErrorCode.IMAGE_FILE_NOT_PROVIDED);
            }
            // Upload ảnh với tên của bundle để dễ quản lý
            imageUrl = cloudinaryService.uploadImage(imageFile, "bundles", request.getName());
            log.info("Image uploaded to Cloudinary: {}", imageUrl);

        } catch (IOException e) {
            log.error("Image upload failed", e);
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid file type", e);
            throw new AppException(ErrorCode.INVALID_FILE_TYPE, e.getMessage());
        }

        request.setImageUrl(imageUrl);

        // Validate items exist
        List<Items> items = validateAndGetItems(request.getItems());

        // Tạo slug từ tên
        String slug = SlugUtils.generateSlug(request.getName());

        // Kiểm tra trùng tên hoặc slug
        if (bundlesRepository.findByName(request.getName()).isPresent()) {
            throw new AppException(ErrorCode.BUNDLE_ALREADY_EXISTS);
        }

        // Tạo Bundle entity
        Bundles bundle = Bundles.builder()
                .name(request.getName())
                .description(request.getDescription())
                .slug(slug)
                .bundleType(request.getBundleType())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .isActive(true)
                .bundleItems(new ArrayList<>())
                .build();

        // Save bundle trước
        bundle = bundlesRepository.save(bundle);

        // Tạo BundleItems
        List<BundleItems> bundleItems = createBundleItems(bundle, request.getItems(), items);
        bundle.setBundleItems(bundleItems);

        log.info("Bundle created successfully with ID: {}", bundle.getId());
        return mapToBundleDetailResponse(bundle);
    }

    /**
     * [ADMIN] Cập nhật bundle
     */
    @Transactional
    public BundleDetailResponse updateBundle(Long id, UpdateBundleRequest request) {
        log.info("Updating bundle ID: {}", id);

        Bundles bundle = bundlesRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BUNDLE_NOT_FOUND));

        // Validate items
        List<Items> items = validateAndGetItems(request.getItems());

        // Update basic fields
        bundle.setName(request.getName());
        bundle.setDescription(request.getDescription());
        bundle.setBundleType(request.getBundleType());
        bundle.setPrice(request.getPrice());
        bundle.setImageUrl(request.getImageUrl());

        if (request.getIsActive() != null) {
            bundle.setActive(request.getIsActive());
        }

        // Update slug if name changed
        String newSlug = SlugUtils.generateSlug(request.getName());
        bundle.setSlug(newSlug);

        // Xóa tất cả BundleItems cũ
        bundleItemsRepository.deleteAll(bundle.getBundleItems());

        // Tạo BundleItems mới
        List<BundleItems> newBundleItems = createBundleItems(bundle, request.getItems(), items);
        bundle.setBundleItems(newBundleItems);

        bundle = bundlesRepository.save(bundle);

        log.info("Bundle updated successfully: {}", bundle.getId());
        return mapToBundleDetailResponse(bundle);
    }

    /**
    * [ADMIN] Xóa bundle 
    */
    @Transactional
    public void deleteBundle(Long id) {
        log.info("Deleting bundle ID: {}", id);

        Bundles bundle = bundlesRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BUNDLE_NOT_FOUND));

        bundleItemsRepository.deleteAll(bundle.getBundleItems());
        
        bundlesRepository.delete(bundle);

        log.info("Bundle and related items deleted successfully: {}", id);
    }

    /**
     * [ADMIN] Toggle availability của bundle
     */
    @Transactional
    public void toggleBundleAvailability(Long id, boolean isActive) {
        log.info("Toggling bundle ID: {} to isActive={}", id, isActive);

        Bundles bundle = bundlesRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BUNDLE_NOT_FOUND));

        bundle.setActive(isActive);
        bundlesRepository.save(bundle);

        log.info("Bundle availability toggled successfully");
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validate và lấy Items từ database
     */
    private List<Items> validateAndGetItems(List<CreateBundleRequest.BundleItemRequest> itemRequests) {
        List<Long> itemIds = itemRequests.stream()
                .map(CreateBundleRequest.BundleItemRequest::getItemId)
                .collect(Collectors.toList());

        List<Items> items = itemsRepository.findAllById(itemIds);

        if (items.size() != itemIds.size()) {
            throw new AppException(ErrorCode.ITEM_NOT_FOUND);
        }

        return items;
    }

    /**
     * Tạo danh sách BundleItems
     */
    private List<BundleItems> createBundleItems(
            Bundles bundle,
            List<CreateBundleRequest.BundleItemRequest> itemRequests,
            List<Items> items
    ) {
        List<BundleItems> bundleItems = new ArrayList<>();

        for (int i = 0; i < itemRequests.size(); i++) {
            CreateBundleRequest.BundleItemRequest itemRequest = itemRequests.get(i);
            Items item = items.stream()
                    .filter(it -> it.getId().equals(itemRequest.getItemId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

            BundleItems bundleItem = BundleItems.builder()
                    .bundle(bundle)
                    .item(item)
                    .quantity(itemRequest.getQuantity())
                    .sortOrder(itemRequest.getSortOrder() != null ? itemRequest.getSortOrder() : i)
                    .creationDate(new Date())
                    .updatedDate(new Date())
                    .build();

            bundleItems.add(bundleItemsRepository.save(bundleItem));
        }

        return bundleItems;
    }

   
}

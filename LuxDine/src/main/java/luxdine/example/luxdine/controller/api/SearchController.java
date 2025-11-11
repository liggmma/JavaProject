package luxdine.example.luxdine.controller.api;

import lombok.RequiredArgsConstructor;
import luxdine.example.luxdine.domain.catalog.dto.response.SearchSuggestResponse;
import luxdine.example.luxdine.domain.catalog.enums.ItemVisibility;
import luxdine.example.luxdine.domain.catalog.repository.BundlesRepository;
import luxdine.example.luxdine.domain.catalog.repository.ItemsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/search")
public class SearchController {

    private final ItemsRepository itemsRepo;
    private final BundlesRepository bundlesRepo;

    @GetMapping("/suggest")
    public List<SearchSuggestResponse> suggest(
            @RequestParam("q") String q,
            @RequestParam(value = "limit", defaultValue = "8") int limit
    ) {
        final String kw = q == null ? "" : q.trim();
        if (kw.isEmpty()) return List.of();

        final var page = PageRequest.of(0, Math.max(1, Math.min(limit, 10)));

        var items = itemsRepo
                .findByIsAvailableTrueAndVisibilityAndNameContainingIgnoreCase(
                        ItemVisibility.PUBLIC, kw, page);

        var bundles = bundlesRepo
                .findByIsActiveTrueAndNameContainingIgnoreCase(kw, page);

        var out = new ArrayList<SearchSuggestResponse>(limit);
        items.forEach(i -> out.add(SearchSuggestResponse.item(i.getName(), i.getSlug())));
        bundles.forEach(b -> out.add(SearchSuggestResponse.bundle(b.getName(), b.getSlug())));

        // Có thể trộn theo tỉ lệ 1-1 hoặc theo độ ưu tiên; ở đây giới hạn tổng
        return out.size() > limit ? out.subList(0, limit) : out;
    }
}

package luxdine.example.luxdine.domain.catalog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class SearchSuggestResponse {
    // ITEM | BUNDLE
    private String type;
    private String name;
    private String slug;
    private String url;

    public static SearchSuggestResponse item(String name, String slug) {
        return new SearchSuggestResponse("ITEM", name, slug, "/browse/dish/" + slug);
    }
    public static SearchSuggestResponse bundle(String name, String slug) {
        return new SearchSuggestResponse("BUNDLE", name, slug, "/browse/bundle/" + slug);
    }
}
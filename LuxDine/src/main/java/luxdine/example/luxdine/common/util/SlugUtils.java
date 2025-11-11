package luxdine.example.luxdine.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    /**
     * Convert tên tiếng Việt có dấu thành slug
     * Ví dụ: "Combo Phở Bò Đặc Biệt" → "combo-pho-bo-dac-biet"
     */
    public static String generateSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String noWhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        
        return slug.toLowerCase(Locale.ENGLISH)
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
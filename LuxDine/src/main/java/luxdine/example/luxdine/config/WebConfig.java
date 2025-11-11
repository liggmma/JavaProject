package luxdine.example.luxdine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.InstantFormatter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatterForFieldType(OffsetDateTime.class, new OffsetDateTimeFormatter());
        registry.addFormatterForFieldType(java.time.Instant.class, new InstantFormatter());
    }

    private static class OffsetDateTimeFormatter implements org.springframework.format.Formatter<OffsetDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        @Override
        public OffsetDateTime parse(String text, java.util.Locale locale) {
            if (text == null || text.isEmpty()) {
                return null;
            }
            try {
                return OffsetDateTime.parse(text, FORMATTER);
            } catch (Exception e) {
                System.err.println("Failed to parse OffsetDateTime: '" + text + "'. Error: " + e.getMessage());
                return null;
            }
        }

        @Override
        public String print(OffsetDateTime object, java.util.Locale locale) {
            return object != null ? object.format(FORMATTER) : "";
        }
    }
}


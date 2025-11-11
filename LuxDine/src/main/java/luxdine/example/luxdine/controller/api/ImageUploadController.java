package luxdine.example.luxdine.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.service.CloudinaryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/uploads")
@RequiredArgsConstructor
@Slf4j
public class ImageUploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/bundle", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadBundle(@RequestParam("image") MultipartFile image,
                                          @RequestParam(value = "name", required = false) String name) {
        try {
            String url = cloudinaryService.uploadImage(image, "bundles", name);
            return ResponseEntity.ok(Map.of("success", true, "imageUrl", url));
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid upload request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        } catch (IOException ex) {
            log.error("Upload failed", ex);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Upload thất bại"));
        }
    }

    @PostMapping(value = "/menu-item", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadMenuItem(@RequestParam("image") MultipartFile image,
                                            @RequestParam(value = "name", required = false) String name) {
        try {
            String url = cloudinaryService.uploadImage(image, "menu-items", name);
            return ResponseEntity.ok(Map.of("success", true, "imageUrl", url));
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid upload request: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        } catch (IOException ex) {
            log.error("Upload failed", ex);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Upload thất bại"));
        }
    }
    
   @PostMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<?> testUpload(@RequestParam("image") MultipartFile image) {
    System.out.println(" CONTROLLER HIT! File: " + image.getOriginalFilename());
    
    try {
        String url = cloudinaryService.uploadImage(image, "test", "test_image");
        System.out.println("Upload success: " + url);
        return ResponseEntity.ok(Map.of("success", true, "imageUrl", url));

    } catch (Exception ex) {
        System.err.println("Upload failed: " + ex.getMessage());
        ex.printStackTrace();
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
    }
}
}
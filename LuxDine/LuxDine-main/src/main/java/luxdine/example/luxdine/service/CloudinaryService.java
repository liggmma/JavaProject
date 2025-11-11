package luxdine.example.luxdine.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.common.util.FileUploadUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folder, String baseName) throws IOException {
        FileUploadUtil.assertAllowed(file, FileUploadUtil.IMAGE_PATTERN);

        String publicId = FileUploadUtil.getFileName(baseName != null ? baseName : "image");
        Map<?, ?> result = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "folder", folder,
                "public_id", publicId,
                "resource_type", "image",
                "overwrite", true,
                "use_filename", false
            )
        );
        String url = (String) result.get("secure_url");
        log.info("Uploaded image: {}", url);
        return url;
    }

    public void deleteImage(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}
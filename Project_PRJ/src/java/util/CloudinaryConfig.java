package util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

public class CloudinaryConfig {

    private static Cloudinary cloudinary;

    static {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dkfnv5kdv",
                "api_key", "129311218875993",
                "api_secret", "i-vVra4aZ7m-uIagO0HG6zxoHS0"
        ));
    }

    public static Cloudinary getCloudinary() {
        return cloudinary;
    }
}

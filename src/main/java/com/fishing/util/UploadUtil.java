package com.fishing.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

public final class UploadUtil {

    public static final long MAX_SIZE = 5L * 1024 * 1024;

    private static final Cloudinary CLOUDINARY = createCloudinary();

    private UploadUtil() {}

    private static Cloudinary createCloudinary() {
        String cloudName = System.getenv("CLOUDINARY_CLOUD_NAME");
        String apiKey = System.getenv("CLOUDINARY_API_KEY");
        String apiSecret = System.getenv("CLOUDINARY_API_SECRET");

        if (cloudName == null || cloudName.isBlank()
                || apiKey == null || apiKey.isBlank()
                || apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalStateException(
                    "Chưa cấu hình đầy đủ biến môi trường Cloudinary."
            );
        }

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public static String saveImage(Part part) throws IOException {
        if (part == null || part.getSize() == 0) {
            return null;
        }

        if (part.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("Ảnh vượt quá 5MB.");
        }

        byte[] imageBytes;

        try (InputStream in = part.getInputStream()) {
            imageBytes = in.readNBytes((int) MAX_SIZE + 1);
        }

        if (imageBytes.length > MAX_SIZE) {
            throw new IllegalArgumentException("Ảnh vượt quá 5MB.");
        }

        String ext = detectExtension(imageBytes);

        if (ext == null) {
            throw new IllegalArgumentException(
                    "Chỉ chấp nhận ảnh JPG, PNG, GIF hoặc WebP."
            );
        }

        String publicId = "products/" + UUID.randomUUID();

        Map<?, ?> result = CLOUDINARY.uploader().upload(
                imageBytes,
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", "image"
                )
        );

        Object url = result.get("secure_url");

        if (url == null) {
            throw new IOException("Cloudinary không trả về URL ảnh.");
        }

        return url.toString();
    }

    public static void deleteQuietly(String url) {
        if (url == null || !url.contains("res.cloudinary.com/")) {
            return;
        }

        try {
            String publicId = extractPublicId(url);

            if (publicId != null) {
                CLOUDINARY.uploader().destroy(
                        publicId,
                        ObjectUtils.asMap("resource_type", "image")
                );
            }
        } catch (Exception ignored) {
            // Không làm gián đoạn luồng chính nếu xóa ảnh thất bại.
        }
    }

    private static String extractPublicId(String url) {
        try {
            String path = java.net.URI.create(url).getPath();
            String marker = "/image/upload/";

            int index = path.indexOf(marker);
            if (index < 0) {
                return null;
            }

            String filePath = path.substring(index + marker.length());

            // Bỏ phần phiên bản v123456 nếu có.
            if (filePath.matches("v\\d+/.*")) {
                filePath = filePath.replaceFirst("^v\\d+/", "");
            }

            int dot = filePath.lastIndexOf('.');
            if (dot >= 0) {
                filePath = filePath.substring(0, dot);
            }

            return filePath.startsWith("products/") ? filePath : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String detectExtension(byte[] b) {
        if (b.length >= 3
                && (b[0] & 0xFF) == 0xFF
                && (b[1] & 0xFF) == 0xD8
                && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }

        if (b.length >= 8
                && (b[0] & 0xFF) == 0x89
                && b[1] == 'P'
                && b[2] == 'N'
                && b[3] == 'G'
                && b[4] == 0x0D
                && b[5] == 0x0A
                && b[6] == 0x1A
                && b[7] == 0x0A) {
            return "png";
        }

        if (b.length >= 4
                && b[0] == 'G'
                && b[1] == 'I'
                && b[2] == 'F'
                && b[3] == '8') {
            return "gif";
        }

        if (b.length >= 12
                && b[0] == 'R'
                && b[1] == 'I'
                && b[2] == 'F'
                && b[3] == 'F'
                && b[8] == 'W'
                && b[9] == 'E'
                && b[10] == 'B'
                && b[11] == 'P') {
            return "webp";
        }

        return null;
    }
}
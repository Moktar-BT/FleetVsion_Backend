package com.Transami.Transami.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) throws IOException {

        // ✅ Validation
        if (file.isEmpty()) {
            throw new RuntimeException("Fichier vide");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new RuntimeException("Seulement les images sont autorisées");
        }

        // ✅ Upload Cloudinary
        Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "transami/logos",
                        "public_id", UUID.randomUUID().toString()
                )
        );

        return uploadResult.get("secure_url").toString();
    }
}
package com.boardwise.backend.user_service.services;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class R2StorageService {

    private final S3Client s3Client;
    
    @Value("${R2_BUCKET_PROFILES}")
    private String bucketName;
    
    @Value("${R2_DEV_URL}") // <-- for when we have domain [CHANGE ME DURING PROD.]
    private String publicUrl;

    public R2StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String encodedName = file.getOriginalFilename().replace(" ", "-");
        String fileName = folder + "/" + UUID.randomUUID() + "_" + encodedName;
        byte[] fileBytes = file.getBytes();
 
        if (fileBytes.length == 0) {
            throw new IOException("File bytes are empty before upload");
        }

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .contentLength((long) fileBytes.length)
                .build();

        s3Client.putObject(putObjectRequest, 
                RequestBody.fromBytes(fileBytes));

        return fileName;
    }

    public String getFileUrl(String fileName) {
        if (publicUrl != null && !publicUrl.isEmpty()) {
            return publicUrl + "/" + fileName;
        }
        
        return String.format("https://%s.r2.cloudflarestorage.com/%s", 
                bucketName, fileName);
    }
}

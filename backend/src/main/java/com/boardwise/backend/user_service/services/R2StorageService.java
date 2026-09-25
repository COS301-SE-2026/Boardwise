package com.boardwise.backend.user_service.services;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class R2StorageService {

    private final S3Client s3Client;
    
    @Value("${r2.bucket-profiles}")
    private String bucketName;
    
    @Value("${r2.profiles.public-url}")
    private String publicUrl;

    public String uploadFile(MultipartFile file, String folder) throws IOException, IllegalArgumentException {
        final String ogFileName = file.getOriginalFilename().toLowerCase();
        if(
            !(ogFileName.endsWith(".png") || ogFileName.endsWith(".jpg") || 
            ogFileName.endsWith("jpeg") || ogFileName.endsWith(".GIF") || 
            ogFileName.endsWith(".webp"))
        ){
            throw new IllegalArgumentException("Uploaded file is not in a legal format."); 
        }

        String encodedName = ogFileName.replace(" ", "-");
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

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank() || fileUrl.contains("/seeded-data/")) { // i just hope no one names their file "seeded-data"
            return;
        }

        if (fileUrl.contains(publicUrl)) {
            fileUrl = fileUrl.substring(publicUrl.length());
        }    

        // request object
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                                                                    .bucket(bucketName)
                                                                    .key(fileUrl)
                                                                    .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    public String getFileUrl(String fileName) {
        String encodedFileName = Arrays.stream(fileName.split("/"))
                                        .map(seg -> URLEncoder.encode(seg, StandardCharsets.UTF_8).replace("+", "%20"))
                                        .collect(Collectors.joining("/"));
                                    
        if (publicUrl != null && !publicUrl.isEmpty()) {
            return publicUrl.endsWith("/") ? 
                publicUrl + encodedFileName :
                publicUrl + "/" + encodedFileName;
        }

        return String.format("https://%s.r2.cloudflarestorage.com/%s", 
                bucketName, fileName);
    }
}

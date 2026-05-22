package com.boardwise.backend.user_service.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@ExtendWith(MockitoExtension.class)
class R2StorageServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private R2StorageService r2StorageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(r2StorageService, "bucketName", "test-bucket");
        // Trailing slash is required: getFileUrl does publicUrl + fileName,
        // so without it the result is "https://test.r2.dev/testuser/..." not
        // "https://test.r2.devtestuser/..."
        ReflectionTestUtils.setField(r2StorageService, "publicUrl", "https://test.r2.dev/");
    }

    // --- UPLOAD ------------------------------------------------------------------

    @Test
    void shouldUploadFileSuccessfully() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test image.jpg", "image/jpeg", "test content".getBytes());

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().eTag("test-etag").build());

        String fileName = r2StorageService.uploadFile(file, "testuser");

        assertNotNull(fileName);
        // uploadFile returns the storage key, not a full URL
        assertTrue(fileName.startsWith("testuser/"));
        // spaces in the original filename are replaced with dashes
        assertTrue(fileName.contains("test-image.jpg"));
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void shouldThrowExceptionWhenFileEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThrows(IOException.class,
                () -> r2StorageService.uploadFile(emptyFile, "testuser"));

        // S3 should never be called when the file is empty
        verifyNoInteractions(s3Client);
    }

    @Test
    void shouldUseCustomDomain_WhenPublicUrlIsSet() {
        String fileName = "testuser/avatar.jpg";
        String url = r2StorageService.getFileUrl(fileName);

        // publicUrl("https://test.r2.dev/") + fileName("testuser/avatar.jpg")
        assertEquals("https://test.r2.dev/testuser/avatar.jpg", url);
    }

    @Test
    void shouldFallBackToR2Url_WhenPublicUrlIsEmpty() {
        // Override publicUrl with empty string to exercise the fallback branch
        ReflectionTestUtils.setField(r2StorageService, "publicUrl", "");

        String fileName = "testuser/avatar.jpg";
        String url = r2StorageService.getFileUrl(fileName);

        // Fallback: https://{bucket}.r2.cloudflarestorage.com/{fileName}
        assertEquals("https://test-bucket.r2.cloudflarestorage.com/testuser/avatar.jpg", url);
    }

    @Test
    void shouldPropagateS3Exception_WhenUploadFails() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", "content".getBytes());

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(new RuntimeException("S3 unavailable"));

        assertThrows(RuntimeException.class,
                () -> r2StorageService.uploadFile(file, "testuser"));
    }
}
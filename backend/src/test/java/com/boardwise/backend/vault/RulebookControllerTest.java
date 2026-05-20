package com.boardwise.backend.vault;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.boardwise.backend.shared.exception.GlobalExceptionHandler;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.vault.controller.RulebookController;
import com.boardwise.backend.vault.dto.response.DownloadUrlResponseDto;
import com.boardwise.backend.vault.dto.response.EditEventResponseDto;
import com.boardwise.backend.vault.dto.response.EditHistoryResponseDto;
import com.boardwise.backend.vault.dto.response.RulebookResponseDto;
import com.boardwise.backend.vault.dto.response.RulebookTextResponseDto;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.service.RulebookService;

@WebMvcTest(RulebookController.class)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
public class RulebookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RulebookService rulebookService;

    @MockitoBean
    private JWTService jwtUtil;

    private ObjectId rulebookId;
    private ObjectId contributorId;
    private ObjectId editorId;
    private RulebookResponseDto mockRulebookResponseDto;
    private RulebookTextResponseDto mockRulebookTextResponseDto;
    private DownloadUrlResponseDto mockDownloadUrlResponseDto;
    private EditHistoryResponseDto mockEditHistoryResponseDto;

    @BeforeEach
    void setUp() {
        rulebookId = new ObjectId();
        contributorId = new ObjectId();
        editorId = new ObjectId();

        mockRulebookResponseDto = RulebookResponseDto.builder()
                .id(rulebookId.toHexString())
                .gameName("Catan")
                .edition("3rd Edition")
                .status("Ready")
                .version(3)
                .contributorId(contributorId.toHexString())
                .lockHeldBy(null)
                .uploadedAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        mockRulebookTextResponseDto = RulebookTextResponseDto.builder()
                .rulebookId(rulebookId.toHexString())
                .content("These are the rules for Catan.")
                .version(3)
                .lockHeldBy(null)
                .updatedAt(Instant.now())
                .build();

        mockDownloadUrlResponseDto = DownloadUrlResponseDto.builder()
                .downloadUrl("https://r2.example.com/presigned-url")
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        mockEditHistoryResponseDto = EditHistoryResponseDto.builder()
                .rulebookId(rulebookId.toHexString())
                .totalEdits(1)
                .edits(List.of(
                        EditEventResponseDto.builder()
                                .id(new ObjectId().toHexString())
                                .rulebookId(rulebookId.toHexString())
                                .editorId(editorId.toHexString())
                                .delta("Added setup instructions.")
                                .versionAfter(3)
                                .committedAt(Instant.now())
                                .build()))
                .build();
    }

    // --- GET /api/vault/rulebooks ---

    @Test
    @WithMockUser
    void listRulebooks_returns200WithPagedResults() throws Exception {
        when(rulebookService.searchRulebooks(anyString(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(mockRulebookResponseDto)));

        mockMvc.perform(get("/api/vault/rulebooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].gameName").value("Catan"))
                .andExpect(jsonPath("$.content[0].status").value("Ready"));
    }

    @Test
    @WithMockUser
    void listRulebooks_returns200WithSearchParam() throws Exception {
        when(rulebookService.searchRulebooks(eq("Catan"), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(mockRulebookResponseDto)));

        mockMvc.perform(get("/api/vault/rulebooks")
                .param("search", "Catan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].gameName").value("Catan"));
    }

    @Test
    @WithMockUser
    void listRulebooks_returns200WithEmptyPage() throws Exception {
        when(rulebookService.searchRulebooks(anyString(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/vault/rulebooks")
                .param("search", "Unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    // --- GET /api/vault/rulebooks/{id} ---

    @Test
    @WithMockUser
    void getRulebook_returns200ForValidId() throws Exception {
        when(rulebookService.getRulebookById(any(ObjectId.class)))
                .thenReturn(mockRulebookResponseDto);

        mockMvc.perform(get("/api/vault/rulebooks/{id}", rulebookId.toHexString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rulebookId.toHexString()))
                .andExpect(jsonPath("$.gameName").value("Catan"));
    }

    @Test
    @WithMockUser
    void getRulebook_returns404ForUnknownId() throws Exception {
        when(rulebookService.getRulebookById(any(ObjectId.class)))
                .thenThrow(new RulebookNotFoundException(rulebookId));

        mockMvc.perform(get("/api/vault/rulebooks/{id}", rulebookId.toHexString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser
    void getRulebook_returns400ForMalformedId() throws Exception {
        mockMvc.perform(get("/api/vault/rulebooks/{id}", "not-a-valid-id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // --- GET /api/vault/rulebooks/{id}/text ---

    @Test
    @WithMockUser
    void getRulebookText_returns200WithContent() throws Exception {
        when(rulebookService.getRulebookText(any(ObjectId.class)))
                .thenReturn(mockRulebookTextResponseDto);

        mockMvc.perform(get("/api/vault/rulebooks/{id}/text", rulebookId.toHexString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("These are the rules for Catan."))
                .andExpect(jsonPath("$.version").value(3));
    }

    @Test
    @WithMockUser
    void getRulebookText_returns404ForUnknownId() throws Exception {
        when(rulebookService.getRulebookText(any(ObjectId.class)))
                .thenThrow(new RulebookNotFoundException(rulebookId));

        mockMvc.perform(get("/api/vault/rulebooks/{id}/text", rulebookId.toHexString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // --- GET /api/vault/rulebooks/{id}/download ---

    @Test
    @WithMockUser
    void downloadRulebook_returns200WithPresignedUrl() throws Exception {
        when(rulebookService.getDownloadUrl(any(ObjectId.class)))
                .thenReturn(mockDownloadUrlResponseDto);

        mockMvc.perform(get("/api/vault/rulebooks/{id}/download", rulebookId.toHexString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloadUrl").value("https://r2.example.com/presigned-url"))
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    @WithMockUser
    void downloadRulebook_returns404WhenPdfNotAvailable() throws Exception {
        when(rulebookService.getDownloadUrl(any(ObjectId.class)))
                .thenThrow(new RulebookNotFoundException(rulebookId));

        mockMvc.perform(get("/api/vault/rulebooks/{id}/download", rulebookId.toHexString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // --- GET /api/vault/rulebooks/{id}/history ---

    @Test
    @WithMockUser
    void getEditHistory_returns200WithEvents() throws Exception {
        when(rulebookService.getEditHistory(any(ObjectId.class)))
                .thenReturn(mockEditHistoryResponseDto);

        mockMvc.perform(get("/api/vault/rulebooks/{id}/history", rulebookId.toHexString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEdits").value(1))
                .andExpect(jsonPath("$.edits[0].delta").value("Added setup instructions."));
    }

    @Test
    @WithMockUser
    void getEditHistory_returns404ForUnknownRulebook() throws Exception {
        when(rulebookService.getEditHistory(any(ObjectId.class)))
                .thenThrow(new RulebookNotFoundException(rulebookId));

        mockMvc.perform(get("/api/vault/rulebooks/{id}/history", rulebookId.toHexString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    // --- 401 Unauthorized ---

    @Test
    void allEndpoints_return401WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/vault/rulebooks"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/vault/rulebooks/{id}", rulebookId.toHexString()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/vault/rulebooks/{id}/text", rulebookId.toHexString()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/vault/rulebooks/{id}/download", rulebookId.toHexString()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/vault/rulebooks/{id}/history", rulebookId.toHexString()))
                .andExpect(status().isUnauthorized());
    }
}

package com.boardwise.backend.vault.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.boardwise.backend.shared.config.SecurityConfig;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.shared.security.JwtFilter;
import com.boardwise.backend.user_service.repos.TokenBlackListRepository;
import com.boardwise.backend.user_service.services.MyUserDetailsService;
import com.boardwise.backend.vault.controller.RulebookController;
import com.boardwise.backend.vault.dto.response.DownloadUrlResponseDto;
import com.boardwise.backend.vault.dto.response.EditHistoryResponseDto;
import com.boardwise.backend.vault.dto.response.RulebookResponseDto;
import com.boardwise.backend.vault.dto.response.RulebookSummaryResponseDto;
import com.boardwise.backend.vault.dto.response.RulebookTextResponseDto;
import com.boardwise.backend.vault.exception.R2PresignException;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.service.RulebookService;

@WebMvcTest(RulebookController.class)
@Import({SecurityConfig.class, JwtFilter.class})
public class RulebookControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RulebookService rulebookService;

    @MockitoBean
    MyUserDetailsService userDetailsService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private TokenBlackListRepository tokenBlackListRepository;
    
    private final ObjectId validId = new ObjectId();

    // ---------- AC-VLT-02: List/ Search Rulebooks ----------
    private RulebookSummaryResponseDto sampleSummaryDto(){
        return RulebookSummaryResponseDto.builder()
            .id(validId.toHexString())
            .coverUrl("https://covers.mock.com/catan.png")
            .title("Catan")
            .language("English")
            .edition("5th")
            .version(2L)
            .genres(List.of("Strategy"))
            .minPlayers(3)
            .maxPlayers(4)
            .duration(90)
            .minAge(3)
            .build();
    }
    @Nested
    class ListRulebooksTests{
        @Test
        @WithMockUser
        void listRulebooksReturns200WithResults() throws Exception{
            // Arrange
            when(rulebookService.searchRulebooks(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(sampleSummaryDto())));
            
            // Act and Assert
            mockMvc.perform(get("/api/vault/rulebooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Catan"));
        }
        
        @Test
        @WithMockUser
        void listRulebooksReturns204WhenEmpty() throws Exception{
            // Arrange
            when(rulebookService.searchRulebooks(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.empty());
            
            // Act and Assert
            mockMvc.perform(get("/api/vault/rulebooks"))
                .andExpect(status().isNoContent());
        }
        
        @Test
        @WithMockUser
        void listRulebooksAppliesDefaultPageAndLimit() throws Exception{
            // Arrange
            when(rulebookService.searchRulebooks(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.empty());
            // Act and Assert
            mockMvc.perform(get("/api/vault/rulebooks"))
                .andExpect(status().isNoContent());
            verify(rulebookService).searchRulebooks(isNull(),isNull(),isNull(),isNull(), isNull(), isNull(),eq(1), eq(20));
        }
        
        @Test
        @WithMockUser
        void listRulebooksBindsAllQueryParams() throws Exception{
            // Arrange
            when(rulebookService.searchRulebooks(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.empty());
            // Act and Assert
            mockMvc.perform(get("/api/vault/rulebooks")
                .param("search", "catan")
                .param("genre", "strategy")
                .param("languages", "English", "French")
                .param("playerCount", "4")
                .param("duration", "90")
                .param("minAge", "10")
                .param("page", "2")
                .param("limit", "50"))
                .andExpect(status().isNoContent());
            verify(rulebookService).searchRulebooks(
                eq("catan"), eq("strategy"), eq(List.of("English", "French")),
                eq(4), eq(90), eq(10), eq(2), eq(50));
        }
    }
    // ---------- AC-VLT-03: Get Rulebook Detail ----------
    @Test
    @WithMockUser
    void getRulebookReturns200AndCorrectBody() throws Exception{
        // Arrange
        when(rulebookService.getRulebookById(validId)).thenReturn(RulebookResponseDto.builder().title("Catan").build());
        // Act and Assert
        mockMvc.perform(get("/api/vault/rulebooks/{id}", validId.toHexString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Catan"));
    }
    // ---------- AC-VLT-04: Download Raw PDF ----------
    @Test
    @WithMockUser
    void downloadRulebookReturns200() throws Exception {
        // Arrange
        when(rulebookService.getDownloadUrl(validId)).thenReturn(DownloadUrlResponseDto.builder()
            .downloadUrl("https://r2.mock.com/signed").expiresAt(Instant.now().plusSeconds(300)).build());
        // Act and Assert
        mockMvc.perform(get("/api/vault/rulebooks/{id}/download", validId.toHexString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloadUrl").value("https://r2.mock.com/signed"));
    }
    // ---------- AC-VLT-05: Get Rulebook Text State ----------
    @Test
    @WithMockUser
    void getRulebookTextReturns200() throws Exception {
        // Arrange
        when(rulebookService.getRulebookText(validId)).thenReturn(RulebookTextResponseDto.builder()
            .rulebookId(validId.toHexString()).chunks(List.of()).version(1L).lockHeldBy("").build());
        // Act and Assert
        mockMvc.perform(get("/api/vault/rulebooks/{id}/text", validId.toHexString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rulebookId").value(validId.toHexString()));
    }
    // ---------- AC-VLT-09: Get Rulebook Edit History ----------
    @Test
    @WithMockUser
    void getEditHistoryReturns200() throws Exception {
        // Arrange
        when(rulebookService.getEditHistory(validId)).thenReturn(EditHistoryResponseDto.builder()
        .rulebookId(validId.toHexString()).totalEdits(0).edits(List.of()).build());
        // Act and Assert
        mockMvc.perform(get("/api/vault/rulebooks/{id}/history", validId.toHexString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEdits").value(0));
    }

    // ---------- All other tests ----------
    @ParameterizedTest
    @WithMockUser
    @ValueSource(strings = {
        "/api/vault/rulebooks/{id}",
        "/api/vault/rulebooks/{id}/download",
        "/api/vault/rulebooks/{id}/text",
        "/api/vault/rulebooks/{id}/history"
    })
    void malformedObjectIdReturns400OnAllIdRoutes(String urlTemplate) throws Exception{
        mockMvc.perform(get(urlTemplate, "not-a-valid-id"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void rulebookNotFoundExceptionIsHandledByAdviceAndReturns() throws Exception{
        // Arrange
        when(rulebookService.getRulebookById(validId))
            .thenThrow(new RulebookNotFoundException(validId));
        // Act and assert
        mockMvc.perform(get("/api/vault/rulebooks/{id}/", validId.toHexString()))
            .andExpect(status().isNotFound());
    }
    
    @Test
    @WithMockUser
    void r2PresignedExceptionIsHandledByAdviceAndReturns() throws Exception{
        // Arrange
        when(rulebookService.getDownloadUrl(validId))
            .thenThrow(new R2PresignException("upstream failure"));
        // Act and assert
        mockMvc.perform(get("/api/vault/rulebooks/{id}/download", validId.toHexString()))
            .andExpect(status().isBadGateway());
    }
}

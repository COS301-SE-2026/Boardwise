package com.boardwise.backend.vault.service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.boardwise.backend.vault.dto.response.DownloadUrlResponseDto;
import com.boardwise.backend.vault.dto.response.EditEventResponseDto;
import com.boardwise.backend.vault.dto.response.EditHistoryResponseDto;
import com.boardwise.backend.vault.dto.response.RulebookResponseDto;
import com.boardwise.backend.vault.dto.response.RulebookSummaryResponseDto;
import com.boardwise.backend.vault.dto.response.RulebookTextResponseDto;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.exception.BoardgameNotFoundException;
import com.boardwise.backend.vault.exception.InvalidPaginationException;
import com.boardwise.backend.vault.exception.R2PresignException;
import com.boardwise.backend.vault.model.EditEvent;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.model.RulebookText;

import com.boardwise.backend.vault.repository.EditEventRepository;
import com.boardwise.backend.vault.repository.RulebookRepository;
import com.boardwise.backend.vault.repository.RulebookTextRepository;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import com.boardwise.backend.user_service.models.Boardgame;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.BoardGameRepository;
import com.boardwise.backend.user_service.repos.UserRepository;

@Service
@RequiredArgsConstructor // automatically generates a constructor for specific fields(Removes need for manual boilerplate code)
public class RulebookService {
    private final RulebookRepository rulebookRepository;
    private final RulebookTextRepository rulebookTextRepository;
    private final EditEventRepository editEventRepository;
    private final BoardGameRepository boardgameRepository;
    private final UserRepository userRepository;

    @Value("${r2.rulebooks.public-dev-url}")
    private String r2PublicDomain;

    @Value("${r2.bucket-rulebooks}")
    private String rulebooksBucket;

    private final S3Presigner s3Presigner;

    // AC-VLT-02: List / Search Rulebooks
    public Page<RulebookSummaryResponseDto> searchRulebooks(
        String search, String genre, List<String> languages,
        Integer playerCount, Integer duration, Integer minAge,
        int page, int limit){
        Pageable pageable = PageRequest.of(
            page-1,
            Math.min(limit, 100),
            Sort.by(Sort.Direction.DESC, "updatedAt")
        );

        Page<Rulebook> dtoPage = rulebookRepository.searchWithFilters(
            search, genre, languages, playerCount, duration, minAge, pageable);

        return dtoPage.map(this::toRulebookSummaryResponse);
    }

    // AC-VLT-03: Get Rulebook Detail
    public RulebookResponseDto getRulebookById(ObjectId id){
        Rulebook rulebook = findRulebookOrThrow(id);
        return toRulebookResponse(rulebook);
    }

    // AC-VLT-04: Download Raw PDF
    public DownloadUrlResponseDto getDownloadUrl(ObjectId id) {
        Rulebook rulebook = findRulebookOrThrow(id);

        if (rulebook.getR2PdfKey() == null) {
            throw new RulebookNotFoundException(
                    "PDF not yet available for rulebook: " + id);
        }

        try{
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(rulebooksBucket)
                .key(rulebook.getR2PdfKey())
                .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .getObjectRequest(getObjectRequest)
                .build();

            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);

            return DownloadUrlResponseDto.builder()
                    .downloadUrl(presigned.url().toString())
                    .expiresAt(java.time.Instant.now().plusSeconds(300))
                    .build();
        }catch(Exception e){
            throw new R2PresignException("Failed to generate download URL: " + e.getMessage());
        }
    }

    // AC-VLT-05: Get Rulebook Text State
    public RulebookTextResponseDto getRulebookText(ObjectId id){
        Rulebook rulebook = findRulebookOrThrow(id);

        RulebookText text = rulebookTextRepository
            .findByRulebookId(id)
            .orElseThrow(() -> new RulebookNotFoundException("Text content not found for rulebook: " + id));

            String username = "";
            if(rulebook.getLockHeldBy() != null){
                User user = findUserOrThrow(rulebook.getLockHeldBy());
                username = user.getUsername();
            }

        return RulebookTextResponseDto.builder()
            .rulebookId(id.toHexString())
            .chunks(text.getChunks())
            .version(rulebook.getVersion())
            .lockHeldBy(username)
            .updatedAt(text.getUpdatedAt())
            .build();
    }

    // AC-VLT-09: Get Rulebook Edit History
    public EditHistoryResponseDto getEditHistory(ObjectId id){
        findRulebookOrThrow(id);

        List<EditEvent> events = editEventRepository.findByRulebookIdOrderByCommittedAtAsc(id);

        List<EditEventResponseDto> eventResponses = events.stream()
            .map(this::toEditEventResponse)
            .collect(Collectors.toList());

        return EditHistoryResponseDto.builder()
            .rulebookId(id.toHexString())
            .totalEdits(eventResponses.size())
            .edits(eventResponses)
            .build();
    }

    // --- private helpers ---
    private Boardgame findBoardgameOrThrow(ObjectId id){
        if(id == null){
            throw new IllegalArgumentException("Boardgame ID cannot be null");
        }
        return boardgameRepository.findById(id.toHexString()).orElseThrow(() -> new BoardgameNotFoundException(id));
    }

    private Rulebook findRulebookOrThrow(ObjectId id) {
        return rulebookRepository.findById(id)
            .orElseThrow(() -> new RulebookNotFoundException(id));
    }

    private User findUserOrThrow(ObjectId id) {
        return userRepository.findById(id.toHexString())
                .orElseThrow(() -> new IllegalArgumentException("User does not exist."));
    }

    private String resolveCoverUrl(String coverImageUrl, String r2CoverKey){
        if(coverImageUrl != null && !coverImageUrl.trim().isEmpty()){
            return coverImageUrl;
        }

        // Fallback to R2 default image
        String cleanKey = (r2CoverKey != null && r2CoverKey.startsWith("/")) ? r2CoverKey.substring(1) : r2CoverKey; // Strips away leading slashes to prevent creating an invalid url

        return r2PublicDomain + "/" + cleanKey;
    }

    private RulebookSummaryResponseDto toRulebookSummaryResponse(Rulebook rulebook){
        String coverUrl = resolveCoverUrl(
            !rulebook.getCoverUrl().isBlank() ? rulebook.getCoverUrl() : "" ,
            rulebook.getR2CoverKey());

        return RulebookSummaryResponseDto.builder()
                .id(rulebook.getId() != null ? rulebook.getId().toHexString() : null)
                .coverUrl(coverUrl.isEmpty() ? resolveCoverUrl("", rulebook.getR2CoverKey()) : coverUrl)
                .title(rulebook.getTitle())
                .language(rulebook.getLanguage())
                .edition(rulebook.getEdition())
                .version(rulebook.getVersion())
                .genres(rulebook.getGenres())
                .minPlayers(rulebook.getMinPlayers())
                .maxPlayers(rulebook.getMaxPlayers())
                .duration(rulebook.getDuration())
                .minAge(rulebook.getMinAge())
                .build();
    }

    private RulebookResponseDto toRulebookResponse(Rulebook rulebook) {
        String coverUrl = resolveCoverUrl(
                !rulebook.getCoverUrl().isBlank() ? rulebook.getCoverUrl() : "",
                rulebook.getR2CoverKey());

        String username = "";
        if(rulebook.getLockHeldBy() != null){
            User user = findUserOrThrow(rulebook.getLockHeldBy());
            username = user.getUsername();
        }

        return RulebookResponseDto.builder()
                .id(rulebook.getId().toHexString())
                .coverUrl(coverUrl.isEmpty() ? resolveCoverUrl("", rulebook.getR2CoverKey()) : coverUrl)
                .title(rulebook.getTitle())
                .edition(rulebook.getEdition())
                .genres(rulebook.getGenres())
                .version(rulebook.getVersion())
                .status(rulebook.getStatus())
                .contributorUsername(rulebook.getContributorUsername())
                .description(rulebook.getDescription())
                .language(rulebook.getLanguage())
                .lockHeldBy(username)
                .lockExpiresAt(rulebook.getLockExpiresAt())
                .uploadedAt(rulebook.getUploadedAt())
                .updatedAt(rulebook.getUpdatedAt())
                .minPlayers(rulebook.getMinPlayers())
                .maxPlayers(rulebook.getMaxPlayers())
                .duration(rulebook.getDuration())
                .minAge(rulebook.getMinAge())
                .build();
    }

    private EditEventResponseDto toEditEventResponse(EditEvent event) {
        // User user = findUserOrThrow(event.getEditorId());
        User user = userRepository.findById(event.getEditorId().toHexString()).orElseGet(() -> {
            User deletedUser = new User();
            deletedUser.setId(event.getEditorId().toHexString());
            deletedUser.setUsername("Deleted User");
            return deletedUser;
        });

        return EditEventResponseDto.builder()
            .id(event.getId().toHexString())
            .rulebookId(event.getRulebookId().toHexString())
            .editor(user.getUsername())
            .chunkId(event.getChunkId().toHexString())
            .editType(event.getEditType().toString())
            .previousContent(event.getPreviousContent())
            .newContent(event.getNewContent())
            .versionPostEdit(event.getVersionPostEdit())
            .committedAt(event.getCommittedAt())
            .build();
    }
}

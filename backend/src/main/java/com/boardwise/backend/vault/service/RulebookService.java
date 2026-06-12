package com.boardwise.backend.vault.service;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
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
import com.boardwise.backend.vault.model.EditEvent;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.model.RulebookText;
import com.boardwise.backend.vault.model.WriteLock;
import com.boardwise.backend.vault.repository.EditEventRepository;
import com.boardwise.backend.vault.repository.RulebookRepository;
import com.boardwise.backend.vault.repository.RulebookTextRepository;
import com.boardwise.backend.vault.repository.WriteLockRepository;

import lombok.RequiredArgsConstructor;

import com.boardwise.backend.user_service.models.Boardgame;
import com.boardwise.backend.user_service.repos.BoardGameRepository;

@Service
@RequiredArgsConstructor // automatically generates a constructor for specific fields(Removes need for manual boilerplate code)
public class RulebookService {
    private final RulebookRepository rulebookRepository;
    private final RulebookTextRepository rulebookTextRepository;
    private final WriteLockRepository writeLockRepository;
    private final EditEventRepository editEventRepository;
    private final BoardGameRepository boardgameRepository;

    // AC-VLT-02: List / Search Rulebooks
    public Page<RulebookSummaryResponseDto> searchRulebooks(String search, int page, int limit){
        Pageable pageable = PageRequest.of(
            page-1,
            Math.min(limit, 100),
            Sort.by(Sort.Direction.DESC, "updatedAt")
        );

        Page<RulebookSummaryResponseDto> dtoPage =
            rulebookRepository.findByStatusAndGameNameContainingIgnoreCase("Ready", search, pageable).map(this::toRulebookSummaryResponse);

        return dtoPage;
    }

    // AC-VLT-03: Get Rulebook Detail
    public RulebookResponseDto getRulebookById(ObjectId id){
        Rulebook rulebook = findRulebookOrThrow(id);
        return toRulebookResponse(rulebook);
    }

    // AC-VLT-05: Get Rulebook Text State
    public RulebookTextResponseDto getRulebookText(ObjectId id){
        findRulebookOrThrow(id);

        RulebookText text = rulebookTextRepository
            .findByRulebookId(id)
            .orElseThrow(() -> new RulebookNotFoundException("Text content not found for rulebook: " + id));

        WriteLock lock = writeLockRepository
            .findByRulebookId(id)
            .orElse(null);

        return RulebookTextResponseDto.builder()
            .rulebookId(id.toHexString())
            .content(text.getContent())
            .version(text.getVersion())
            .lockHeldBy(lock != null ? lock.getHeldByUserId().toHexString() : null)
            .updatedAt(text.getUpdatedAt())
            .build();
    }

    // AC-VLT-04: Download Raw PDF - pre-signed URL generation placeholder
    // R2 pre-signing will be wired in once R2Config is active
    public DownloadUrlResponseDto getDownloadUrl(ObjectId id) {
        Rulebook rulebook = findRulebookOrThrow(id);

        if (rulebook.getR2PdfKey() == null) {
            throw new RulebookNotFoundException(
                    "PDF not yet available for rulebook: " + id);
        }

        // TODO: wire R2Presigner bean here in Phase 6
        return DownloadUrlResponseDto.builder()
                .downloadUrl("presigned-url-placeholder")
                .expiresAt(java.time.Instant.now().plusSeconds(300))
                .build();
    }

    // US-VLT-06: Edit History
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
        return boardgameRepository.findById(id.toHexString()).orElseThrow(() -> new BoardgameNotFoundException(id));
    }

    private Rulebook findRulebookOrThrow(ObjectId id) {
        return rulebookRepository.findById(id)
            .orElseThrow(() -> new RulebookNotFoundException(id));
    }

    private RulebookSummaryResponseDto toRulebookSummaryResponse(Rulebook rulebook){
        // fetch genres from boardgame document
        Boardgame game = findBoardgameOrThrow(rulebook.getGameId());

        return RulebookSummaryResponseDto.builder()
                .id(rulebook.getId().toHexString())
                .gameName(rulebook.getGameName())
                .edition(rulebook.getEdition())
                .version(rulebook.getVersion())
                .genres(game.getGenres())
                .build();
    }

    private RulebookResponseDto toRulebookResponse(Rulebook rulebook) {
        WriteLock lock = writeLockRepository
            .findByRulebookId(rulebook.getId())
            .orElse(null);

        // fetch genres from boardgame document
        Boardgame game = findBoardgameOrThrow(rulebook.getGameId());

        return RulebookResponseDto.builder()
                .id(rulebook.getId().toHexString())
                .gameName(rulebook.getGameName())
                .edition(rulebook.getEdition())
                .genres(game.getGenres())
                .status(rulebook.getStatus())
                .version(rulebook.getVersion())
                .contributorId(rulebook.getContributorId().toHexString())
                .lockHeldBy(lock != null ? lock.getHeldByUserId().toHexString() : null)
                .uploadedAt(rulebook.getUploadedAt())
                .updatedAt(rulebook.getUpdatedAt())
                .build();
    }

    private EditEventResponseDto toEditEventResponse(EditEvent event) {
        return EditEventResponseDto.builder()
            .id(event.getId().toHexString())
            .rulebookId(event.getRulebookId().toHexString())
            .editorId(event.getEditorId().toHexString())
            .delta(event.getDelta())
            .versionAfter(event.getVersionAfter())
            .committedAt(event.getCommittedAt())
            .build();
    }
}

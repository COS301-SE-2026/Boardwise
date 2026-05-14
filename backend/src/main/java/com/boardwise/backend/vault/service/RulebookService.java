package com.boardwise.backend.vault.service;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.boardwise.backend.vault.dto.response.RulebookResponseDto;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.model.WriteLock;
import com.boardwise.backend.vault.repository.RulebookRepository;
import com.boardwise.backend.vault.repository.WriteLockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // automatically generates a constructor for specific fields(Removes need for manual boilerplate code)
public class RulebookService {
    private final RulebookRepository rulebookRepository;
    private final WriteLockRepository writeLockRepository;

    // VC-002: List / Search Rulebooks
    public Page<RulebookResponseDto> searchRulebooks(String search, int page, int limit){
        Pageable pageable = PageRequest.of(
            page-1,
            Math.min(limit, 100),
            Sort.by(Sort.Direction.DESC, "updated_at")
        );

        Page<RulebookResponseDto> dtoPage =
            rulebookRepository.findByStatusAndGameNameContainingIgnoreCase("Ready", search, pageable).map(this::toRulebookResponse);

        return dtoPage; // Page has the lockHeldBy field set to null. A book that is in state Ready does not have a write lock
    }

    // VC-003: Get Rulebook Detail
    public RulebookResponseDto getRulebookById(ObjectId id){
        Rulebook rulebook = findRulebookOrThrow(id);
        return toRulebookResponse(rulebook);
    }

    

    // --- private helpers ---
    private Rulebook findRulebookOrThrow(ObjectId id) {
        return rulebookRepository.findById(id)
            .orElseThrow(() -> new RulebookNotFoundException(id));
    }

    private RulebookResponseDto toRulebookResponse(Rulebook rulebook) {
        WriteLock lock = writeLockRepository
            .findByRulebookId(rulebook.getId())
            .orElse(null);

        return RulebookResponseDto.builder()
                .id(rulebook.getId().toHexString())
                .gameName(rulebook.getGameName())
                .edition(rulebook.getEdition())
                .status(rulebook.getStatus())
                .version(rulebook.getVersion())
                .contributorId(rulebook.getContributorId().toHexString())
                .lockHeldBy(lock != null ? lock.getHeldByUserId().toHexString() : null)
                .uploadedAt(rulebook.getUploadedAt())
                .updatedAt(rulebook.getUpdatedAt())
                .build();
    }
}

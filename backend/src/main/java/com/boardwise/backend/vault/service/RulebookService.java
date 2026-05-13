package com.boardwise.backend.vault.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.boardwise.backend.vault.dto.response.RulebookResponseDto;
import com.boardwise.backend.vault.mapper.RulebookMapper;
import com.boardwise.backend.vault.repository.RulebookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // automatically generates a constructor for specific fields(Removes need for manual boilerplate code)
public class RulebookService {
    private final RulebookRepository rulebookRepository;
    private final RulebookMapper rulebookMapper; // Injected instance of the mapper

    // VC-002: List / Search Rulebooks
    public Page<RulebookResponseDto> searchRulebooks(String search, int page, int limit){
        Pageable pageable = PageRequest.of(
            page-1,
            Math.min(limit, 100),
            Sort.by(Sort.Direction.DESC, "updated_at")
        );
        return rulebookRepository
            .findByStatusAndGameNameContainingIgnoreCase("Ready", search, pageable)
            .map(rulebookMapper::toDto);
    }
    // ----- private helpers -----

}

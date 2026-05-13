package com.boardwise.backend.vault.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boardwise.backend.vault.dto.response.RulebookResponseDto;
import com.boardwise.backend.vault.service.RulebookService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vault/rulebooks")
@RequiredArgsConstructor
public class RulebookController {
    private final RulebookService rulebookService;

    // VC-002: List / Search Rulebooks
    @GetMapping
    public ResponseEntity<Page<RulebookResponseDto>> listRulebooks(
        @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int limit){
            return ResponseEntity.ok(
                rulebookService.searchRulebooks(search, page, limit)
            );
    }
}

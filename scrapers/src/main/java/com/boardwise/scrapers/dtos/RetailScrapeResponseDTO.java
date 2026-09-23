package com.boardwise.scrapers.dtos;

import java.util.List;

public record  RetailScrapeResponseDTO(
    List<LevelUpStoreResDTO> levelUpStoreResDTOs, 
    List<TableTopGuruResDTO> tableTopGuruResDTOs, 
    List<TimelessBoardGamesResDTO> timelessBoardGamesResDTOs, 
    List<BgbsaDTO> bgbsaDTOs) {
    public static final RetailScrapeResponseDTO EMPTY = new RetailScrapeResponseDTO(List.of(), List.of(), List.of(), List.of());

    public boolean isEmpty() {
        return bgbsaDTOs.isEmpty() && levelUpStoreResDTOs.isEmpty() && tableTopGuruResDTOs.isEmpty() && timelessBoardGamesResDTOs.isEmpty();
    }
    }

package com.boardwise.scrapers.services;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.boardwise.scrapers.dtos.RuleBookReqResponse;
import com.boardwise.scrapers.exceptions.FailedToScrape;
import com.boardwise.scrapers.exceptions.ResourceNotFound;
import com.boardwise.scrapers.models.Boardgame;
import com.boardwise.scrapers.models.Rulebook;
import com.boardwise.scrapers.repositories.BoardGameRepository;
import com.boardwise.scrapers.repositories.RulebookRepository;
import com.boardwise.scrapers.services.rulebook_scrapers.RuleBookOrgScraper;


@Service
public class PdfService {
    private final BoardGameRepository boardgameRepository;
    private final RulebookRepository rulebookRepository;
    private final RuleBookOrgScraper ruleBookOrgScraper;
    
    public PdfService(BoardGameRepository boardgameRepository, RulebookRepository rulebookRepository, RuleBookOrgScraper ruleBookOrgScraper){
        this.boardgameRepository = boardgameRepository;
        this.rulebookRepository = rulebookRepository;
        this.ruleBookOrgScraper =  ruleBookOrgScraper;
    }


    public RuleBookReqResponse scrapeForRulebook(String boardgame) {
        if (boardgame == null || boardgame.isBlank()) {
            throw new IllegalArgumentException("Board game title cannot be blank");
        }

        
        Boardgame game = boardgameRepository.findByTitle(boardgame) 
            .orElseThrow(() -> new ResourceNotFound(boardgame + " seems to not exist, might have to create it"));

        ObjectId gameId = new ObjectId(game.getId());

        Optional<Rulebook> existing = findUsable(gameId);
        if (existing.isPresent()) {
            return new RuleBookReqResponse(existing.get().getId());
        }

        try {
            ruleBookOrgScraper.processSingleGame(game);
        } catch (Exception e) {
            throw new FailedToScrape("error occurred during rulebook processing: " + e.getMessage());
        }

        Rulebook stored = findUsable(gameId)
                .orElseThrow(() -> new FailedToScrape("No rulebook found for " + boardgame));

        return new RuleBookReqResponse(stored.getId());
    }

    private Optional<Rulebook> findUsable(ObjectId gameId) {
        return rulebookRepository.findByGameId(gameId).stream()
                .filter(r -> !"Failed".equals(r.getStatus()))
                .findFirst();
    }


}

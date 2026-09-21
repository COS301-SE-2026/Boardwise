package com.boardwise.scrapers.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boardwise.scrapers.dtos.RetailSourceItemDTO;
import com.boardwise.scrapers.services.listing_scraper.RetailScraper;

import java.util.HashMap;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
@RequestMapping("/internal/retail")
public class RetailScraperController {
    private final RetailScraper retailScraper;

    RetailScraperController(RetailScraper retailScraper){
        this.retailScraper = retailScraper;
    }

    @GetMapping("/listings")
    public ResponseEntity<HashMap<String,List<RetailSourceItemDTO>>> getPersonalisedListings(@RequestParam List<String> boardgames) {
        HashMap<String,List<RetailSourceItemDTO>> res = new HashMap<>();

        try{
            for(String game: boardgames){
                res.put(game, retailScraper.scrapeForBoardgame(game));
            }
            return ResponseEntity.ok(res);
        }
        catch(Exception e){
            return ResponseEntity.internalServerError().build();
        }
       
    }
    
}

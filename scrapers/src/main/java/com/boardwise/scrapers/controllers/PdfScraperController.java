package com.boardwise.scrapers.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boardwise.scrapers.dtos.RuleBookReqResponse;
import com.boardwise.scrapers.exceptions.FailedToScrape;
import com.boardwise.scrapers.exceptions.ResourceNotFound;
import com.boardwise.scrapers.services.PdfServiceHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("internal/pdfscraper")
public class PdfScraperController {
    private final Logger logger = Logger.getLogger(PdfScraperController.class.getName());
    private final PdfServiceHandler pdfService;
    PdfScraperController( PdfServiceHandler pdfService){
        this.pdfService = pdfService;

    }
    @GetMapping("/rulebook")
    public ResponseEntity<RuleBookReqResponse> scrapeForRulebook(@RequestParam String title) {
        try{
            return ResponseEntity.ok(pdfService.scrapeForRulebook(title));
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.badRequest().build();
        }
        catch( ResourceNotFound e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        catch(FailedToScrape e){
            logger.log(Level.WARNING, "failed to scrape for " + title + "(user request)");
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
    
}

package com.boardwise.backend.scraper;
import com.boardwise.backend.scraper.dtos.*;
import java.util.List;

//Interface class 
interface WebScraper {
    public List<ScrapeResponse> scrape(String toSearch);    
}

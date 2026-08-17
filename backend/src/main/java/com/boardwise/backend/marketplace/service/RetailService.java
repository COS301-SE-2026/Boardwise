package com.boardwise.backend.marketplace.service;

import java.util.ArrayList;
import java.util.List;

import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;
import com.boardwise.backend.marketplace.service.webscrapers.BobShopScraper;
import com.boardwise.backend.marketplace.service.webscrapers.TakealotScraper;
import com.boardwise.backend.marketplace.service.webscrapers.ToysRUsScraper;

public class RetailService {

    private final TakealotScraper ts;
    private final BobShopScraper bss;
    private final ToysRUsScraper trus;

    public RetailService(TakealotScraper ts, BobShopScraper bss, ToysRUsScraper trus) {
        this.ts = ts;
        this.bss = bss;
        this.trus = trus;
    }

    public  List<RetailSourceItemDTO> findWebListings(String s){
        try{
            List<RetailSourceItemDTO> overall = new ArrayList<>();

            List<RetailSourceItemDTO> takealotResults = ts.scrape(s);
            List<RetailSourceItemDTO> bobShopResults = bss.scrape(s);
            List<RetailSourceItemDTO> toysrusResults = trus.scrape(s);

            addToList(overall, takealotResults);
            addToList(overall,bobShopResults);
            addToList(overall,toysrusResults);
            
            if(overall.size() < 0){
                throw new RuntimeException("Error while finding :" + s);
            }
            else if(overall.isEmpty()){
                return new ArrayList<>();
            }
            return overall;
        }
        catch (RuntimeException e){
            throw new RuntimeException(e.getMessage());
        }
    };

    private void addToList(List<RetailSourceItemDTO> overall,List<RetailSourceItemDTO> ls){
        if (ls == null) return;
        for(RetailSourceItemDTO a:ls){
            overall.add(a);
        }
    }
}

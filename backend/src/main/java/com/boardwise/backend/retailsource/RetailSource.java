package com.boardwise.backend.retailsource;

import java.util.ArrayList;
import java.util.List;

import com.boardwise.backend.retailsource.dtos.RetailSourceItemDTO;

public class RetailSource {
    private static  final List<RetailSourceItemDTO> overall = new ArrayList<>();

    private static TakealotScraper ts = new TakealotScraper();
    private static BobShopScraper bss = new BobShopScraper();
    private static ToysRUsScraper trus = new ToysRUsScraper();

    public static  List<RetailSourceItemDTO> findWebListings(String s){
        try{
            List<RetailSourceItemDTO> takealotResults = ts.scrape(s);
            List<RetailSourceItemDTO> bobShopResults = bss.scrape(s);
            List<RetailSourceItemDTO> toysrusResults = trus.scrape(s);

            addToList(takealotResults);
            addToList(bobShopResults);
            addToList(toysrusResults);
            if(overall.size() < 0){
                throw new RuntimeException("Error while ");
            }
        }
        catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
        return overall;
    };

    private static void addToList(List<RetailSourceItemDTO> ls){
        for(RetailSourceItemDTO a:ls){
            overall.add(a);
        }
    }
    
}

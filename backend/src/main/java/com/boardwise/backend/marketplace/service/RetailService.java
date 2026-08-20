package com.boardwise.backend.marketplace.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;
import com.boardwise.backend.marketplace.service.webscrapers.BobShopScraper;
import com.boardwise.backend.marketplace.service.webscrapers.TakealotScraper;
import com.boardwise.backend.marketplace.service.webscrapers.ToysRUsScraper;

@Service
public class RetailService {

    private static final int PAGE_SIZE = 20;

    private final TakealotScraper ts;
    private final BobShopScraper bss;
    private final ToysRUsScraper trus;

    public RetailService(TakealotScraper ts, BobShopScraper bss, ToysRUsScraper trus) {
        this.ts = ts;
        this.bss = bss;
        this.trus = trus;
    }

    protected List<RetailSourceItemDTO> findWebListings(String s) {
         System.out.println("findWebListings called with s=[" + s + "]");
        if (!StringUtils.hasText(s)) {
            System.out.println("Short-circuited: blank query");
            return new ArrayList<>();
        }
        if (!StringUtils.hasText(s)) {
            return new ArrayList<>();
        }

        // individual processes happening concurrently
        CompletableFuture<List<RetailSourceItemDTO>> takealotFuture = CompletableFuture.supplyAsync(() -> safeScrape(ts::scrape, s, "Takealot"));
        CompletableFuture<List<RetailSourceItemDTO>> bobShopFuture = CompletableFuture.supplyAsync(() -> safeScrape(bss::scrape, s, "BobShop"));
        CompletableFuture<List<RetailSourceItemDTO>> toysRUsFuture = CompletableFuture.supplyAsync(() -> safeScrape(trus::scrape, s, "ToysRUs"));

        CompletableFuture.allOf(takealotFuture, bobShopFuture, toysRUsFuture).join();

        List<RetailSourceItemDTO> overall = new ArrayList<>();
        overall.addAll(takealotFuture.join());
        overall.addAll(bobShopFuture.join());
        overall.addAll(toysRUsFuture.join());

        return overall;
    }

    private List<RetailSourceItemDTO> safeScrape(java.util.function.Function<String, List<RetailSourceItemDTO>> scraper,
            String query, String sourceName) {
        try {
            List<RetailSourceItemDTO> result = scraper.apply(query);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Page<RetailSourceItemDTO> getRetailListingsPage(String s, Integer pageNum) {
        List<RetailSourceItemDTO> overall = findWebListings(s);

        int page = (pageNum == null || pageNum < 0) ? 0 : pageNum;
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        int start = (int) pageable.getOffset();
        if (start >= overall.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, overall.size());
        }
        int end = Math.min(start + pageable.getPageSize(), overall.size());

        return new PageImpl<>(overall.subList(start, end), pageable, overall.size());
    }
}
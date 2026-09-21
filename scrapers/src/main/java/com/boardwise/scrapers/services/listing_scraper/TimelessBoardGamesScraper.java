package com.boardwise.scrapers.services.listing_scraper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import com.boardwise.scrapers.dtos.TimelessBoardGamesResDTO;

@Service
public class TimelessBoardGamesScraper {
    private static final String URL = "https://www.timelessboardgames.co.za/";
    private static final String RETAILER = "Timeless Board Games";
    private static final int MAX_PAGES = 5;
    private static final long PAGE_DELAY_MS = 500;

    private final Logger logger = Logger.getLogger(TimelessBoardGamesScraper.class.getName());

    public TimelessBoardGamesScraper() {}

    public List<TimelessBoardGamesResDTO> scrapeForBoardgame(String boardgame) {
        List<TimelessBoardGamesResDTO> results = new ArrayList<>();
        String query = URLEncoder.encode(boardgame, StandardCharsets.UTF_8);

        for (int page = 1; page <= MAX_PAGES; page++) {
            String searchUrl = URL + "online-shop/?page=" + page
                    + "&filter_product_name=" + query + "&filter=yes";
            try {
                Document doc = Jsoup.connect(searchUrl)
                        .userAgent("Mozilla/5.0")
                        .timeout(10_000)
                        .get();

                for (Element card : doc.select("div.w3-card.w3-display-container")) {
                    try {
                        TimelessBoardGamesResDTO item = parseCard(card);
                        if (item != null) {
                            results.add(item);
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Skipping bad card on " + searchUrl, e);
                    }
                }

                if (doc.selectFirst("a[href*='page=" + (page + 1) + "']") == null) {
                    break;
                }

                Thread.sleep(PAGE_DELAY_MS);

            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to scrape " + searchUrl, e);
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return results;
    }

    private TimelessBoardGamesResDTO parseCard(Element card) {
        Element link = card.selectFirst("p.w3-medium a");
        if (link == null) {
            return null; 
        }

        String title = link.text();
        String url = URL + link.attr("href").replaceFirst("^/", "");
        String imageUrl = card.select("div[id^=desc-image-] img").attr("abs:src");

        boolean isAvailable = card.select("span.w3-tag").stream()
                .noneMatch(t -> t.text().equalsIgnoreCase("Out of stock"));

        Double originalPrice = null;
        Double salePrice = null;
        boolean isOnSale = false;

        Element priceEl = card.selectFirst("p.w3-small");
        if (priceEl != null && !priceEl.text().isBlank()) {
            Double current = parsePrice(priceEl.ownText());
            Element strike = priceEl.selectFirst("strike");
            if (strike != null) {
                isOnSale = true;
                originalPrice = parsePrice(strike.text());
                salePrice = current;
            } else {
                originalPrice = current;
            }
        }

        TimelessBoardGamesResDTO toRet= new TimelessBoardGamesResDTO(title, RETAILER, url, isAvailable, isOnSale, originalPrice, salePrice, imageUrl);
        System.out.println(toRet);
        return  toRet;
    }

    private Double parsePrice(String raw) {
        String cleaned = raw.replaceAll("[^0-9.]", ""); 
        return cleaned.isEmpty() ? null : Double.valueOf(cleaned);
    }

}
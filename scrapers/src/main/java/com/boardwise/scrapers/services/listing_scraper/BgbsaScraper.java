package com.boardwise.scrapers.services.listing_scraper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.boardwise.scrapers.dtos.LevelUpStoreResDTO;

@Service
public class BgbsaScraper {

    private static final String URL = "https://www.bgbsa.co.za/";
    private static final String LISTINGS_URL = URL + "listings";
    private static final long DETAIL_FETCH_DELAY_MS = 500L;

    private final Logger logger = Logger.getLogger(BgbsaScraper.class.getName());

    private static final Pattern LISTING_TEXT = Pattern.compile(
        "^(.*?)\\s*\\((\\d+(?:\\.\\d+)?)\\)\\s*R([\\d,]+\\.\\d{2})(?:\\s*\\(Bundle:\\s*\\d+\\s*items?\\))?$"
    );

    public BgbsaScraper() {}

    public List<LevelUpStoreResDTO> scrapeForBoardgame() {
        String boardgame = "Catan";
        List<LevelUpStoreResDTO> results = new ArrayList<>();
        if (boardgame == null || boardgame.isBlank()) {
            return results;
        }

        try {
            Document doc = Jsoup.connect(LISTINGS_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(15_000)
                    .get();

            Elements listingLinks = doc.select("a[href*='/listings/']");
            String normalizedTerm = normalize(boardgame);

            for (Element link : listingLinks) {
                try {
                    LevelUpStoreResDTO item = parseListing(link, normalizedTerm);
                    if (item != null) {
                        results.add(item);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Skipping bad listing from " + LISTINGS_URL, e);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to scrape " + LISTINGS_URL, e);
        }
        System.out.println("BGBSA SCRAPER" + results);
        return results;
    }

    private LevelUpStoreResDTO parseListing(Element link, String normalizedTerm) {
        String fullText = link.text().trim();
        if (fullText.isBlank()) {
            return null;
        }

        Matcher m = LISTING_TEXT.matcher(fullText);
        if (!m.matches()) {
            return null;
        }

        String title = m.group(1).trim();
        if (!normalize(title).contains(normalizedTerm)) {
            return null;
        }

        Double price = parsePrice(m.group(3));
        String href = link.absUrl("href");
        String imageUrl = fetchImageForListing(href);

        return new LevelUpStoreResDTO(title, "BGBSA", price, false, null, href, imageUrl);
    }

    private String fetchImageForListing(String listingUrl) {
        try {
            Thread.sleep(DETAIL_FETCH_DELAY_MS);
            Document detail = Jsoup.connect(listingUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(15_000)
                    .get();

            Element img = detail.selectFirst("img[src*='/storage/'][src*='conversions']");
            return img != null ? img.absUrl("src") : "";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to fetch detail page for image: " + listingUrl, e);
            return "";
        }
    }

    private Double parsePrice(String rawPrice) {
        String cleaned = rawPrice.replace(",", "");
        return cleaned.isEmpty() ? null : Double.valueOf(cleaned);
    }

    private String normalize(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
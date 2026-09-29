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

import com.boardwise.scrapers.dtos.BgbsaDTO;

@Service
public class BgbsaScraper {

    private static final String URL = "https://www.bgbsa.co.za/";
    private static final String LISTINGS_URL = URL + "listings";
    private static final String RETAILER = "BGBSA";
    private static final int PAGE_SIZE = 50;         
    private static final int MAX_PAGES = 25;         
    private static final long PAGE_DELAY_MS = 300L;
    private static final long INDEX_TTL_MS = 10 * 60 * 1000L;

    private final Logger logger = Logger.getLogger(BgbsaScraper.class.getName());

    private static final Pattern LISTING_TEXT = Pattern.compile(
        "^(.*?)\\s*\\((\\d+(?:\\.\\d+)?)\\)\\s*R([\\d,]+\\.\\d{2})(?:\\s*\\(Bundle:\\s*\\d+\\s*items?\\))?$"
    );

    private record Listing(String title, String normalizedTitle, Double price, String url, String imageUrl) {}

    private volatile List<Listing> index = List.of();
    private volatile long indexLoadedAt = 0;

    public BgbsaScraper() {}

    public List<BgbsaDTO> scrapeForBoardgame(String boardgame) {
        List<BgbsaDTO> results = new ArrayList<>();
        if (boardgame == null || boardgame.isBlank()) {
            return results;
        }

        String term = normalize(boardgame);
        for (Listing l : getIndex()) {
            if (l.normalizedTitle().contains(term)) {
                results.add(new BgbsaDTO(l.title(), RETAILER, l.price(), false, null, l.url(), l.imageUrl()));
            }
        }
        return results;
    }

    private List<Listing> getIndex() {
        if (isFresh()) {
            return index;
        }
        synchronized (this) {   
            if (isFresh()) {
                return index;
            }
            List<Listing> loaded = loadAllPages();
            if (!loaded.isEmpty()) {
                index = loaded;
                indexLoadedAt = System.currentTimeMillis();
            }
            return index;
        }
    }

    private boolean isFresh() {
        return System.currentTimeMillis() - indexLoadedAt < INDEX_TTL_MS;
    }

    private List<Listing> loadAllPages() {
        List<Listing> all = new ArrayList<>();

        for (int page = 1; page <= MAX_PAGES; page++) {
            try {
                Document doc = Jsoup.connect(LISTINGS_URL + "?page=" + page)
                        .userAgent("Mozilla/5.0")
                        .timeout(15_000)
                        .get();

                Elements links = doc.select("a[href*='/listings/']");
                for (Element link : links) {
                    try {
                        Listing l = parseListing(link);
                        if (l != null) {
                            all.add(l);
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Skipping bad listing on page " + page, e);
                    }
                }

                if (links.size() < PAGE_SIZE) {
                    break;  // last page
                }
                Thread.sleep(PAGE_DELAY_MS);

            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to scrape " + LISTINGS_URL + " page " + page, e);
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return all;
    }

    private Listing parseListing(Element link) {
        Matcher m = LISTING_TEXT.matcher(link.text().trim());
        if (!m.matches()) {
            return null;
        }

        String title = m.group(1).trim();
        Element img = link.selectFirst("img[src*='/storage/']");
        String imageUrl = img != null ? img.absUrl("src") : "";

        return new Listing(title, normalize(title), parsePrice(m.group(3)), link.absUrl("href"), imageUrl);
    }

    private Double parsePrice(String rawPrice) {
        String cleaned = rawPrice.replace(",", "");
        return cleaned.isEmpty() ? null : Double.valueOf(cleaned);
    }

    private String normalize(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
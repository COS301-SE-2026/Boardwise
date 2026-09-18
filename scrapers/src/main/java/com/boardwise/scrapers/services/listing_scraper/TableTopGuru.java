package com.boardwise.scrapers.services.listing_scraper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import com.boardwise.scrapers.dtos.TableTopGuruResDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TableTopGuru {
    private static final String URL = "https://tabletopguru.co.za/";
    private static final String RETAILER = "Tabletop Guru";
    private static final boolean SKIP_PRE_LOVED = true;

    private final Logger logger = Logger.getLogger(TableTopGuru.class.getName());
    private final ObjectMapper mapper = new ObjectMapper();

    public TableTopGuru() {}

    public List<TableTopGuruResDTO> scrapeForBoardgame(String boardgame) {
        List<TableTopGuruResDTO> results = new ArrayList<>();
        String query = URLEncoder.encode( boardgame, StandardCharsets.UTF_8);

        String searchUrl = URL + "search/suggest.json?q=" + query+ "&resources%5Btype%5D=product&resources%5Blimit%5D=10";

        try {
            Connection.Response res = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .ignoreContentType(true)
                    .execute();

            JsonNode products = mapper.readTree(res.body())
                    .path("resources").path("results").path("products");

            for (JsonNode p : products) {
                try {
                    TableTopGuruResDTO item = parseProduct(p);
                    if (item != null) {
                        results.add(item);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Skipping bad product from " + searchUrl, e);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to scrape " + searchUrl, e);
        }
        return results;
    }

    private TableTopGuruResDTO parseProduct(JsonNode p) {
        String title = p.path("title").asText();

        if (SKIP_PRE_LOVED && title.toLowerCase().contains("pre-loved")) {
            return null;
        }

        String path = p.path("url").asText().split("\\?")[0];
        String url = URL + path.replaceFirst("^/", "");

        String imageUrl = p.path("featured_image").path("url").asText("");
        if (imageUrl.isEmpty()) {
            imageUrl = p.path("image").asText("");
        }

        boolean isAvailable = p.path("available").asBoolean(true);

        Double price = parsePrice(p.path("price"));
        Double compareAt = parsePrice(p.path("compare_at_price_max"));
        boolean isOnSale = price != null && compareAt != null && compareAt > price;

        Double originalPrice = isOnSale ? compareAt : price;
        Double salePrice = isOnSale ? price : null;

        return new TableTopGuruResDTO(title, RETAILER, url, isAvailable, isOnSale, originalPrice, salePrice, imageUrl);
    }

    private Double parsePrice(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        String cleaned = node.asText().replaceAll("[^0-9.]", ""); 
        return cleaned.isEmpty() ? null : Double.valueOf(cleaned);
    }


}
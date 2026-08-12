package com.boardwise.backend.retailsource;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.boardwise.backend.retailsource.dtos.RetailSourceItemDTO;
import com.boardwise.backend.retailsource.dtos.ScrapeResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class BobShopScraper implements WebScraper {

    public BobShopScraper() {
    }

    private final String RETAILERNAME = "Bobshop";
    private final int MAXNUMITEMS = 15;
    private final String site = "https://www.bobshop.co.za";

    public List<RetailSourceItemDTO> scrape(String toSearch) {
        if (toSearch.isBlank()) {
            return null;
        }
        try (Playwright playwright = Playwright.create()) {
            // chromium
            Browser chromium = playwright.chromium().launch();
            BrowserContext context = chromium.newContext();
            Page page = context.newPage();

            // website
            page.navigate(site);

            // find search bar
            Locator searchBar = page.getByPlaceholder("Search for anything");

            if (searchBar.count() == 0) {
                throw new RuntimeException("Error while trying to find the search bar on Bob Shop");
            }

            // update value found in search bar
            searchBar.fill(toSearch);
            searchBar.press("Enter");

            page.waitForLoadState();
            page.waitForSelector("a.product-card-container");

            // find article
            List<Locator> cards = page.locator("a.product-card-container").all();
            System.out.println(cards.size());

            if (cards.isEmpty()) {
                return null;// for some reason?? should lowkey an exception because wow
            }

            List<ScrapeResponse> matching = new ArrayList<>();

            List<RetailSourceItemDTO> retailSourceItemDTOs = new ArrayList<>();

            for (Locator card : cards) {
                String classAttr = card.getAttribute("class");
                if (classAttr != null && classAttr.contains("sponsored"))
                    continue;

                String title = card.locator("div.product-card-title").innerText().trim();
                if (title.isBlank())
                    continue;

                String url = card.getAttribute("href");

                // Price extraction

                Locator priceEl = card.locator("currency-output.font-weight-bolder");
                String foundPrice = null;
                if (priceEl.count() > 0) {
                    String rands = priceEl.locator("span").first().innerText().replace("R", "").trim();
                    if (rands.contains(","))
                        rands = rands.replace(",", ""); // R1,000 (SA system is js weird like that)
                    String cents = priceEl.locator("sup").first().innerText().trim();
                    foundPrice = rands + "." + cents;
                }

                Double price = Double.parseDouble(foundPrice);

                Locator imageEl = card.locator("image-container");

                String imageUrl = null;
                if (imageEl.count() > 0) {
                    imageUrl = imageEl.first().getAttribute("src");
                }

                System.out.print("Card details: " + title + ":\n" + (imageUrl != null ? imageUrl : "No image") + "\n");

                // Jaro-Winkler - similarity between 2 sequences
                float val = JaroWinklerSimilarity(toSearch, title);

                if (val >= stringMatch) {
                    SimpleEntry<String, Float> toAdd = new SimpleEntry<String, Float>(url, val);
                    retailSourceItemDTOs.add(new RetailSourceItemDTO(title, RETAILERNAME , url, price, imageUrl, val));
                    matching.add(new ScrapeResponse(site, toAdd));
                }

                if (matching.size() >= MAXNUMITEMS)
                    break;

            }

            page.close();

            matching.sort(Comparator.comparingDouble(r -> r.details().getValue())); // sort in terms of float

            return retailSourceItemDTOs;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

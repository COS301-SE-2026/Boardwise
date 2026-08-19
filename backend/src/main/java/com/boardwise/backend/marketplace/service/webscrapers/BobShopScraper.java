package com.boardwise.backend.marketplace.service.webscrapers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;


@Service
public class BobShopScraper implements WebScraper {

    public BobShopScraper() {
    }

    private final String RETAILERNAME = "Bobshop";
    private final int MAXNUMITEMS = 15;
    private final String site = "https://www.bobshop.co.za";

    @Override
    public List<RetailSourceItemDTO> scrape(String toSearch) {
        if (toSearch == null || toSearch.isBlank()) {
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
            
            if (cards.isEmpty()) {
                return null;// for some reason?? should lowkey an exception because wow
            }

            List<RetailSourceItemDTO> retailSourceItemDTOs = new ArrayList<>();

            for (Locator card : cards) {
                // data from DOM
                String classAttr = card.getAttribute("class");
                String title = card.locator("div.product-card-title").innerText();
                String url = card.getAttribute("href");

                // Price extraction

                Locator priceEl = card.locator("currency-output.font-weight-bolder");
                String randsRaw = null;
                String centsRaw = null;
                if (priceEl.count() > 0) {
                    randsRaw = priceEl.locator("span").first().innerText();
                    centsRaw = priceEl.locator("sup").first().innerText();
                }


                Locator imageEl = card.locator("image-container");
                String imageUrl = imageEl.count() > 0 ? imageEl.first().getAttribute("src") : null;

                buildItem(toSearch, title, classAttr, url, randsRaw, centsRaw, imageUrl).ifPresent(retailSourceItemDTOs::add);

                if (retailSourceItemDTOs.size() >= MAXNUMITEMS)
                    break;

            }

            page.close();

            sortBySimilarity(retailSourceItemDTOs); // sort in terms of float

            return retailSourceItemDTOs;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected boolean isSponsored(String classAttr){
        return classAttr != null && classAttr.contains("sponsored");
    }

    protected Double parsePrice(String randsRaw, String centsRaw) {
        if (randsRaw == null || centsRaw == null) {
            return null;
        }
        String rands = randsRaw.replace("R", "").trim();
        if (rands.contains(","))
            rands = rands.replace(",", ""); // R1,000 (SA system is js weird like that)
        String cents = centsRaw.trim();
        try {
            return Double.parseDouble(rands + "." + cents);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Optional<RetailSourceItemDTO> buildItem(
            String toSearch, String rawTitle, String classAttr, String url,
            String randsRaw, String centsRaw, String imageUrl) {

        if (isSponsored(classAttr))
            return Optional.empty();

        String title = rawTitle == null ? "" : rawTitle.trim();
        if (title.isBlank())
            return Optional.empty();

        Double price = parsePrice(randsRaw, centsRaw);

        // Jaro-Winkler - similarity between 2 sequences
        float val = JaroWinklerSimilarity(toSearch, title);

        if (val < STRINGMATCH)
            return Optional.empty();

        return Optional.of(new RetailSourceItemDTO(title, RETAILERNAME, url, price, imageUrl, val));
    }

    protected void sortBySimilarity(List<RetailSourceItemDTO> items) {
        items.sort(Comparator.comparingDouble(RetailSourceItemDTO::JaroWinklerSimilarityScore));
    }

}

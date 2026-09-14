package com.boardwise.scrapers.services.rulebook_scrapers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import com.boardwise.scrapers.dtos.RulebookPdfDTO;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;

@Service
public class RuleBookOrgScraper {
    private static final String URL = "rule-book.org";
    private final Logger logger = Logger.getLogger(RuleBookOrgScraper.class.getName());

    private final Playwright playwright;
    private final Browser browser;

    private final HashMap<String,List<RulebookPdfDTO>> pdfs; // todo: change to a queue for queue based requests 

    public RuleBookOrgScraper() {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch();
        pdfs = new HashMap<>(500);
    }

    public List<RulebookPdfDTO> scrapeForPdfs(String boardgame) {
        List<RulebookPdfDTO> results = new ArrayList<>();

        try (Page page = browser.newPage()) {
            page.navigate("https://" + URL);
            page.waitForLoadState();

            page.getByPlaceholder("search for a board game").fill(boardgame);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();
            page.waitForSelector("[data-slot='card-title']");

            Locator resultCards = page.locator("[data-slot='card-title']");
            List<String> titles = resultCards.allTextContents();
            logger.info("Found " + titles.size() + " results for '" + boardgame + "': " + titles);

            for (String title : titles) {
                RulebookPdfDTO dto = downloadOne(page, boardgame, title);
                if (dto != null) {
                    results.add(dto);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to scrape " + boardgame + " from " + URL, e);
        }

        return results;
    }

    private RulebookPdfDTO downloadOne(Page page, String boardgame, String title) {
        try {
            page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions()
                    .setName(title)
                    .setExact(true))
                .click();

            page.waitForURL("**/pdf");
            page.navigate(page.url()); 
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForFunction(
                "() => Array.from(document.images).every(img => img.complete && img.naturalWidth > 0)",
                null, new Page.WaitForFunctionOptions().setTimeout(80000)
            );
            page.waitForTimeout(10000);

            byte[] pdfBytes = page.pdf();
            logger.info("Captured PDF for: " + title + " (" + pdfBytes.length + " bytes)");

            page.navigate("https://" + URL);
            page.getByPlaceholder("search for a board game").fill(boardgame);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();
            page.waitForSelector("[data-slot='card-title']");

            return new RulebookPdfDTO(title, pdfBytes);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed on '" + title + "' for " + boardgame, e);
            return null;
        }
    }
    

    @PreDestroy
    public void shutdown() {
        browser.close();
        playwright.close();
    }
}
package com.boardwise.scrapers.services.rulebook_scrapers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jakarta.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.boardwise.scrapers.dtos.RulebookPdfDTO;
import com.boardwise.scrapers.models.Boardgame;
import com.boardwise.scrapers.repositories.BoardGameRepository;
import com.boardwise.scrapers.repositories.RulebookRepository;
import com.boardwise.scrapers.repositories.UserRepository;
import com.boardwise.scrapers.repositories.UserRepository.GameOwnershipCount;
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

    private final HashMap<String, List<RulebookPdfDTO>> pdfs;

    private final int MAXNUMRULEBOOKS = 5000;

    private final UserRepository userRepository;
    private final BoardGameRepository boardGameRepository;
    private final RulebookRepository rulebookRepository;

    private final MongoTemplate mongoTemplate;

    private final RestClient restClient;
    private final RestClient pythonUploadClient;

    @Value("${internal.secret}")
    private String internalSecret;

    private static final long MAX_PDF_BYTES = 50L * 1024 * 1024;

    private static final Pattern VERSION_SUFFIX = Pattern.compile(
        "(?i)\\s*(\\(.*?\\)|\\b\\d+(st|nd|rd|th)?\\s*(edition|ed\\.?)\\b|\\b(edition|ed\\.?)\\b|\\b(19|20)\\d{2}\\b)\\s*"
    );
    private static final Pattern RULEBOOK_SUFFIX = Pattern.compile("(?i)\\b(rulebook|r[eè]gles?|rules?)\\b");

    private volatile Map<String, Boolean> normalizedTitleCache;

    public RuleBookOrgScraper(
            UserRepository userRepository,
            BoardGameRepository boardGameRepository,
            RulebookRepository rulebookRepository,
            MongoTemplate mongoTemplate,
            @Qualifier("externalRulebookClient") RestClient restClient,
            @Qualifier("pythonUploadClient") RestClient pythonUploadClient) {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch();
        pdfs = new HashMap<>(500);
        this.userRepository = userRepository;
        this.boardGameRepository = boardGameRepository;
        this.rulebookRepository = rulebookRepository;
        this.mongoTemplate = mongoTemplate;
        this.restClient = restClient;
        this.pythonUploadClient = pythonUploadClient;
    }

    private void buildTitleCache() {
        Query query = new Query();
        query.fields().include("title");

        List<Boardgame> games = mongoTemplate.find(query, Boardgame.class);

        Map<String, Boolean> cache = new HashMap<>(games.size() * 2);
        for (Boardgame game : games) {
            cache.put(normalizeTitle(game.getTitle()), Boolean.TRUE);
        }
        normalizedTitleCache = cache;
        logger.info("Built title cache with " + cache.size() + " entries");
    }

    private boolean gameExistsInDb(String scrapedTitle) {
        if (normalizedTitleCache == null) {
            buildTitleCache();
        }
        return normalizedTitleCache.containsKey(normalizeTitle(scrapedTitle));
    }

    private String normalizeTitle(String rawTitle) {
        String t = rawTitle;

        Matcher suffixMatch = RULEBOOK_SUFFIX.matcher(t);
        if (suffixMatch.find()) {
            t = t.substring(0, suffixMatch.start()).trim();
        }

        t = VERSION_SUFFIX.matcher(t).replaceAll(" ").trim();

        return t.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private String stripRulebookSuffix(String rawTitle) {
        Matcher suffixMatch = RULEBOOK_SUFFIX.matcher(rawTitle);
        if (suffixMatch.find()) {
            return rawTitle.substring(0, suffixMatch.start()).trim();
        }
        return rawTitle.trim();
    }

    public List<RulebookPdfDTO> scrapeForPdfs(String boardgame) {
        List<RulebookPdfDTO> results = new ArrayList<>();

        String requestUrl = "?search=" + boardgame + "&language=en";
        String hello = restClient.get().uri(requestUrl).retrieve().body(String.class);
        System.out.println(hello);

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
                if (!gameExistsInDb(title)) {
                    logger.info("Could not find game: " + title + " in database, now skipping");
                    continue;
                }

                String cleanTitle = stripRulebookSuffix(title);

                RulebookPdfDTO dto = downloadOne(page, boardgame, title, cleanTitle);
                if (dto != null) {
                    results.add(dto);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to scrape " + boardgame + " from " + URL, e);
        }

        return results;
    }

    private RulebookPdfDTO downloadOne(Page page, String boardgame, String title, String cleanTitle) {
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
                    null, new Page.WaitForFunctionOptions().setTimeout(80000));
            page.waitForTimeout(10000);

            byte[] pdfBytes = page.pdf();
            logger.info("Captured PDF for: " + cleanTitle + " (" + pdfBytes.length + " bytes)");

            if (pdfBytes.length > MAX_PDF_BYTES) {
                logger.warning("Skipping '" + cleanTitle + "' — PDF exceeds 50MB limit (" + pdfBytes.length + " bytes)");
                resetToSearch(page, boardgame);
                return null;
            }

            boolean uploaded = uploadToPythonService(boardgame, cleanTitle, pdfBytes, "en");

            resetToSearch(page, boardgame);

            if (!uploaded) {
                logger.warning("Skipping '" + cleanTitle + "' — upload to Python service failed");
                return null;
            }

            return new RulebookPdfDTO(cleanTitle, pdfBytes);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed on '" + cleanTitle + "' for " + boardgame, e);
            return null;
        }
    }

    private void resetToSearch(Page page, String boardgame) {
        page.navigate("https://" + URL);
        page.getByPlaceholder("search for a board game").fill(boardgame);
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();
        page.waitForSelector("[data-slot='card-title']");
    }

    private boolean uploadToPythonService(String boardgame, String title, byte[] pdfBytes, String language) {
        ByteArrayResource fileResource = new ByteArrayResource(pdfBytes) {
            @Override
            public String getFilename() {
                return title + ".pdf";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("title", title);
        body.add("language", language);
        body.add("file", fileResource);

        try {
            String response = pythonUploadClient.post()
                    .uri("vault/rulebooks/internal/upload")
                    .header("X-Internal-Token", internalSecret)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            logger.info("Uploaded rulebook '" + title + "' to Python service: " + response);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to upload '" + title + "' to Python service", e);
            return false;
        }
    }

    @PreDestroy
    public void shutdown() {
        browser.close();
        playwright.close();
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 31536000000L)
    public void scrapeRulebooksForExistingGames() {
        //TODO: Discuss How to ensure we don't rescrape a game we alr have a rulebook for:

        buildTitleCache();

        long count = rulebookRepository.count();

        if (count >= MAXNUMRULEBOOKS) {
            throw new RuntimeException("Mongodb has reached the policy volume");
        }
        List<GameOwnershipCount> top = new ArrayList<>(userRepository.findMostOwnedGameIds(MAXNUMRULEBOOKS));

        if (!top.isEmpty()) {
            for (GameOwnershipCount curr : top) {
                String currentId = curr.getId();
                if (currentId == null) continue;
                Optional<Boardgame> game = boardGameRepository.findById(currentId);

                if (!game.isEmpty()) {
                    String gameTitle = game.get().getTitle();
                    logger.info("Attempting to search for Popular Game: " + gameTitle);
                    scrapeForPdfs(gameTitle);
                    logger.fine("Successfuly found and scraped PDFS for: " + gameTitle);
                } else {
                    logger.warning("Could not find boardGame with details: " + curr + " — skipping");
                    continue;
                }
            }
        }

        Query query = new Query();
        query.cursorBatchSize(1000);

        try (Stream<Boardgame> stream = mongoTemplate.stream(query, Boardgame.class)) {
            stream.forEach(entity -> {
                scrapeForPdfs(entity.getTitle());
            });
        }

        //e.g. Monopoly returns all related RB's e.g. ANti- monopoly
    }
}
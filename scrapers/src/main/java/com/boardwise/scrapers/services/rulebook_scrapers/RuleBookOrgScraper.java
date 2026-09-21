package com.boardwise.scrapers.services.rulebook_scrapers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jakarta.annotation.PreDestroy;

import org.bson.types.ObjectId;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
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

import com.boardwise.scrapers.dtos.RulebookOrgItemDTO;
import com.boardwise.scrapers.dtos.RulebookOrgResponseDTO;
import com.boardwise.scrapers.dtos.RulebookPdfDTO;
import com.boardwise.scrapers.models.Boardgame;
import com.boardwise.scrapers.models.Rulebook;
import com.boardwise.scrapers.repositories.BoardGameRepository;
import com.boardwise.scrapers.repositories.RulebookRepository;
import com.boardwise.scrapers.repositories.UserRepository;
import com.boardwise.scrapers.repositories.UserRepository.GameOwnershipCount;
import com.microsoft.playwright.Browser;
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
    private static final long SCRAPE_DELAY_MS = 1500L;

    private static final Pattern VERSION_SUFFIX = Pattern.compile(
        "(?i)\\s*(\\(.*?\\)|\\b\\d+(st|nd|rd|th)?\\s*(edition|ed\\.?)\\b|\\b(edition|ed\\.?)\\b|\\b(19|20)\\d{2}\\b)\\s*"
    );
    private static final Pattern RULEBOOK_SUFFIX = Pattern.compile("(?i)\\b(rulebook|r[eè]gles?|rules?)\\b");
    private static final Pattern UNSAFE_FILENAME_CHARS = Pattern.compile("[\\\\/:*?\"<>|\\p{Cntrl}]");

    private volatile Set<String> existingRulebookGameIdsCache;
    private volatile Set<String> existingGameTitlesCache;

    public RuleBookOrgScraper(
            UserRepository userRepository,
            BoardGameRepository boardGameRepository,
            RulebookRepository rulebookRepository,
            MongoTemplate mongoTemplate,
            @Qualifier("externalRulebookClient") RestClient restClient,
            @Qualifier("pythonUploadClient") RestClient pythonUploadClient) {
        this.playwright = Playwright.create();
        this.browser = playwright.chromium().launch();
        this.userRepository = userRepository;
        this.boardGameRepository = boardGameRepository;
        this.rulebookRepository = rulebookRepository;
        this.mongoTemplate = mongoTemplate;
        this.restClient = restClient;
        this.pythonUploadClient = pythonUploadClient;
    }

    @Scheduled(initialDelayString = "30s", fixedRateString = "10h")
    public void scrapeRulebooksForExistingGames() {

        long currentRulebookCount = rulebookRepository.count();

        if (currentRulebookCount >= MAXNUMRULEBOOKS) {
            logger.info("Rulebook maximum limit reached (" + MAXNUMRULEBOOKS + "). Skipping scrape.");
            return;
        }

        long totalGames = mongoTemplate.count(new Query(), Boardgame.class);

        if (totalGames == 0) {
            logger.info("Boardgame collection is empty. Retrying next cycle.");
            return;
        }

        refreshCaches();

        Set<String> processedThisRun = new HashSet<>();
        List<GameOwnershipCount> top = userRepository.findMostOwnedGameIds(MAXNUMRULEBOOKS);

        for (GameOwnershipCount curr : top) {
            if (currentRulebookCount >= MAXNUMRULEBOOKS) {
                break;
            }

            String currentId = curr.getId();

            if (currentId == null || processedThisRun.contains(currentId)) continue;

            if (hasExistingRulebook(currentId)) {
                processedThisRun.add(currentId);
                continue;
            }

            Optional<Boardgame> game = boardGameRepository.findById(currentId);

            if (game.isPresent()) {
                processedThisRun.add(currentId);

                if (processSingleGame(game.get())) {
                    existingRulebookGameIdsCache.add(currentId);
                    currentRulebookCount++;
                }
            }
        }

        if (currentRulebookCount >= MAXNUMRULEBOOKS) {
            return;
        }

        Query query = new Query();
        query.fields().include("_id", "title");
        query.cursorBatchSize(500);

        try (Stream<Boardgame> stream = mongoTemplate.stream(query, Boardgame.class)) {
            final long[] rulebookCount = {currentRulebookCount};

            stream.forEach(entity -> {
                if (rulebookCount[0] >= MAXNUMRULEBOOKS) {
                    return;
                }

                String id = entity.getId();

                if (id == null || processedThisRun.contains(id) || hasExistingRulebook(id)) {
                    return;
                }

                processedThisRun.add(id);

                if (processSingleGame(entity)) {
                    existingRulebookGameIdsCache.add(id);
                    rulebookCount[0]++;
                }
            });
        }
    }

    private synchronized void refreshCaches() {
        existingRulebookGameIdsCache = null;
        existingGameTitlesCache = null;
        buildExistingRulebookCache();
    }

    private synchronized void buildExistingRulebookCache() {
        if (existingRulebookGameIdsCache != null) {
            return;
        }

        List<Rulebook> existingRulebooks = rulebookRepository.findByStatusNot("Failed");
        Set<String> rulebookCache = new HashSet<>(existingRulebooks.size() * 2);

        for (Rulebook rb : existingRulebooks) {
            if (rb.getGameId() != null) {
                rulebookCache.add(rb.getGameId().toHexString());
            }
        }

        existingRulebookGameIdsCache = rulebookCache;

        Query query = new Query();
        query.fields().include("title");
        query.cursorBatchSize(500);

        Set<String> gameTitles = new HashSet<>();

        try (Stream<Boardgame> stream = mongoTemplate.stream(query, Boardgame.class)) {
            stream.forEach(game -> {
                if (game.getTitle() != null) {
                    gameTitles.add(normalizeTitle(game.getTitle()));
                }
            });
        }

        existingGameTitlesCache = gameTitles;

        logger.info("Built existing rulebooks cache: " + rulebookCache.size() + " entries.");
        logger.info("Built existing game titles cache: " + gameTitles.size() + " entries.");
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

    private boolean hasExistingRulebook(String boardgameId) {
        if (existingRulebookGameIdsCache == null) {
            buildExistingRulebookCache();
        }

        return existingRulebookGameIdsCache.contains(boardgameId);
    }

    private String sanitizeFilename(String rawTitle) {
        String cleaned = UNSAFE_FILENAME_CHARS.matcher(rawTitle).replaceAll("");
        cleaned = cleaned.replace("..", "").trim();
        return cleaned.isEmpty() ? "rulebook" : cleaned;
    }

    /**
     * Strips active content and, for Playwright captures, drops the leading
     * site-chrome page. Single load/save pass.
     */
    private byte[] cleanPdf(byte[] pdfBytes, boolean dropFirstPage) throws IOException {
        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            COSDictionary catalogDict = catalog.getCOSObject();
            catalogDict.removeItem(COSName.getPDFName("AA"));
            catalogDict.removeItem(COSName.getPDFName("OpenAction"));

            for (PDPage page : doc.getPages()) {
                page.getCOSObject().removeItem(COSName.getPDFName("AA"));
            }

            if (dropFirstPage && doc.getNumberOfPages() > 1) {
                doc.removePage(0);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    public List<RulebookPdfDTO> scrapeForPdfs(String boardgame) {
        return scrapeForPdfs(boardgame, null);
    }

    /**
     * @param gameId the boardgame's Mongo _id, so a successful upload can be recorded
     *               against it. Pass null if unknown (no Rulebook row will be written).
     */
    public List<RulebookPdfDTO> scrapeForPdfs(String boardgame, String gameId) {
        try {
            String requestUrl = "?search=" + URLEncoder.encode(boardgame, StandardCharsets.UTF_8) + "&language=en";
            RulebookOrgResponseDTO res = restClient.get()
                    .uri(requestUrl)
                    .retrieve()
                    .body(RulebookOrgResponseDTO.class);

            if (res != null && res.results() != null && !res.results().isEmpty()) {
                return downloadFromApi(res.results(), boardgame, gameId);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "REST API failed for " + boardgame + ", falling back to website", e);
        }

        List<RulebookPdfDTO> results = new ArrayList<>();

        try (Page page = browser.newPage()) {
            page.navigate("https://" + URL);
            page.waitForLoadState();

            page.getByPlaceholder("search for a board game").fill(boardgame);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search")).click();
            page.waitForSelector("[data-slot='card-title']");

            List<String> titles = page.locator("[data-slot='card-title']").allTextContents();
            String targetTitle = normalizeTitle(boardgame);
            Set<String> seenTitles = new HashSet<>();

            for (String title : titles) {
                String normalized = normalizeTitle(title);

                if (!normalized.equals(targetTitle)) {
                    continue;
                }

                if (!seenTitles.add(normalized)) {
                    continue;
                }

                String cleanTitle = stripRulebookSuffix(title);
                RulebookPdfDTO dto = downloadOne(page, boardgame, gameId, title, cleanTitle);

                if (dto != null) {
                    results.add(dto);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to scrape " + boardgame + " from " + URL, e);
        }

        return results;
    }

    private RulebookPdfDTO downloadOne(Page page, String boardgame, String gameId, String title, String cleanTitle) {
        try {
            page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(title).setExact(true)).click();
            page.waitForURL("**/pdf");
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForFunction("() => Array.from(document.images).every(img => img.complete && img.naturalWidth > 0)", null, new Page.WaitForFunctionOptions().setTimeout(80000));

            byte[] pdfBytes = page.pdf();

            if (pdfBytes.length > MAX_PDF_BYTES) {
                resetToSearch(page, boardgame);
                return null;
            }

            pdfBytes = cleanPdf(pdfBytes, true);
            String safeTitle = sanitizeFilename(cleanTitle);

            if (!uploadToPythonService(boardgame, safeTitle, pdfBytes, "en")) {
                resetToSearch(page, boardgame);
                return null;
            }

            recordRulebook(gameId, safeTitle, "en");
            resetToSearch(page, boardgame);
            return new RulebookPdfDTO(safeTitle, pdfBytes);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed on '" + cleanTitle + "' for " + boardgame, e);
            return null;
        }
    }

    private List<RulebookPdfDTO> downloadFromApi(List<RulebookOrgItemDTO> rulebooks, String boardgame, String gameId) {
        List<RulebookPdfDTO> results = new ArrayList<>();
        String targetTitle = normalizeTitle(boardgame);
        Set<String> seenTitles = new HashSet<>();

        for (RulebookOrgItemDTO rulebook : rulebooks) {
            if (rulebook.link() == null || rulebook.link().isEmpty()) {
                continue;
            }

            String normalized = normalizeTitle(rulebook.name());

            if (!normalized.equals(targetTitle)) {
                continue;
            }

            if (!seenTitles.add(normalized)) {
                continue;
            }

            try {
                byte[] pdfBytes = restClient.get().uri(rulebook.link()).retrieve().body(byte[].class);

                if (pdfBytes == null || pdfBytes.length == 0 || pdfBytes.length > MAX_PDF_BYTES) {
                    continue;
                }

                pdfBytes = cleanPdf(pdfBytes, false);
                String safeTitle = sanitizeFilename(stripRulebookSuffix(rulebook.name()));

                if (uploadToPythonService(boardgame, safeTitle, pdfBytes, rulebook.language())) {
                    recordRulebook(gameId, safeTitle, rulebook.language());
                    results.add(new RulebookPdfDTO(safeTitle, pdfBytes));
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to download '" + rulebook.name() + "'", e);
            }
        }

        return results;
    }

    private void recordRulebook(String gameId, String title, String language) {
        if (gameId == null) {
            return;
        }

        try {
            Instant now = Instant.now();

            Rulebook rulebook = Rulebook.builder()
                    .gameId(new ObjectId(gameId))
                    .title(title)
                    .language(language)
                    .status("Processing")
                    .version(1L)
                    .uploadedAt(now)
                    .updatedAt(now)
                    .build();

            rulebookRepository.save(rulebook);

            if (existingRulebookGameIdsCache != null) {
                existingRulebookGameIdsCache.add(gameId);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to record rulebook for gameId " + gameId, e);
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
            pythonUploadClient.post()
                    .uri("vault/rulebooks/internal/upload")
                    .header("X-Internal-Token", internalSecret)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to upload '" + title + "' to Python service", e);
            return false;
        }
    }

    private boolean processSingleGame(Boardgame game) {
        if (game.getTitle() == null || game.getId() == null) {
            return false;
        }

        List<RulebookPdfDTO> results = scrapeForPdfs(game.getTitle(), game.getId());

        sleepBetweenScrapes();

        return !results.isEmpty();
    }

    private void sleepBetweenScrapes() {
        try{
            Thread.sleep(SCRAPE_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void shutdown() {
        browser.close();
        playwright.close();
    }

}
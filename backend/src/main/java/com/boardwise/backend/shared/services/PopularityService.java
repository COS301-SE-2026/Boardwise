package com.boardwise.backend.shared.services;

import java.io.StringReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.boardwise.backend.shared.model.BggStats;
import com.boardwise.backend.shared.model.Boardgame;
import com.boardwise.backend.shared.model.BoardgameStatsSnapshot;
import com.boardwise.backend.shared.repository.BoardGameRepository;
import com.boardwise.backend.shared.repository.BoardgameStatsSnapshotRepository;
import com.boardwise.backend.shared.services.scoring.PopularityScorer;

// keeps bgg stats and the popularity score current.
// separate from the ingest in BoardGameService because the two walk the catalogue in
// opposite directions: ingest moves forward through bggId finding games it's never
// seen, this cycles over games we already have, stalest first. popularity changes
// over time so a score written once goes stale within weeks
@Service
public class PopularityService {

    private static final Logger log = LoggerFactory.getLogger(PopularityService.class);

    private final BoardGameRepository gameRepo;
    private final BoardgameStatsSnapshotRepository snapshotRepo;
    private final RestClient client;
    private final PopularityScorer scorer;

    private final int batchSize;   // bgg caps /thing at 20 ids per request

    public PopularityService(BoardGameRepository gameRepo,
                             BoardgameStatsSnapshotRepository snapshotRepo,
                             @Qualifier("bggRestClient") RestClient client,
                             PopularityScorer scorer,
                             @Value("${popularity.refresh.batch-size}") int batchSize) {
        this.gameRepo = gameRepo;
        this.snapshotRepo = snapshotRepo;
        this.client = client;
        this.scorer = scorer;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${popularity.refresh.interval.ms}")
    public void refreshStats() {
        List<Boardgame> batch = gameRepo.findByBggIdNotNullOrderByLastStatsRefreshedAtAsc(Limit.of(batchSize));

        if (batch.isEmpty())
            return;

        String ids = batch.stream()
                .map(game -> Integer.toString(game.getBggId()))
                .collect(Collectors.joining(","));

        String response;
        try {
            ResponseEntity<String> entity = client.get()
                    .uri("/thing?id=" + ids + "&subtype=boardgame&stats=1")
                    .retrieve()
                    .toEntity(String.class);

            // bgg sends 202 while it's still building the export, so the data really
            // isn't there yet. leave the cursor alone and let the next tick retry,
            // the schedule interval is the backoff so we don't block the scheduler
            if (entity.getStatusCode().value() == 202) {
                log.info("BGG queued the stats request for {} games, retrying next tick", batch.size());
                return;
            }

            response = entity.getBody();
        } catch (RestClientException e) {
            // 4xx/5xx. stamp the batch anyway so one bad id can't stall the cursor
            // on the same games forever
            log.warn("BGG stats request failed for {} games, skipping batch: {}", batch.size(), e.getMessage());
            stampAndSave(batch);
            return;
        }

        if (response == null || response.isBlank()) {
            log.warn("BGG returned an empty stats body for {} games", batch.size());
            stampAndSave(batch);
            return;
        }

        try {
            applyStats(batch, response);
        } catch (Exception e) {
            log.error("Failed to apply BGG stats: {}", e.getMessage(), e);
            stampAndSave(batch);
        }
    }

    private void applyStats(List<Boardgame> batch, String response) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(response)));

        Map<Integer, Element> itemsByBggId = new HashMap<>();
        NodeList items = document.getElementsByTagName("item");
        for (int i = 0; i < items.getLength(); i++) {
            Node node = items.item(i);
            if (node == null)
                continue;

            Node idAttr = node.getAttributes().getNamedItem("id");
            if (idAttr == null)
                continue;

            try {
                itemsByBggId.put(Integer.valueOf(idAttr.getNodeValue()), (Element) node);
            } catch (NumberFormatException e) {
                log.warn("BGG returned an unparseable item id: {}", idAttr.getNodeValue());
            }
        }

        Instant now = Instant.now();
        List<BoardgameStatsSnapshot> snapshots = new ArrayList<>();

        for (Boardgame game : batch) {
            Element item = itemsByBggId.get(game.getBggId());

            // still stamp games bgg didn't return, otherwise the cursor keeps handing
            // back the same game every tick and the job stops moving
            if (item == null) {
                log.debug("BGG returned no item for bggId {}", game.getBggId());
                game.setLastStatsRefreshedAt(now);
                continue;
            }

            BggStats stats = BggDataParser.parseStats(item);
            Integer yearPublished = BggDataParser.parseYearPublished(item);

            if (yearPublished != null)
                game.setYearPublished(yearPublished);

            if (stats != null) {
                game.setStats(stats);
                game.setPopularityScore(scorer.score(game));

                snapshots.add(BoardgameStatsSnapshot.builder()
                        .bggId(game.getBggId())
                        .capturedAt(now)
                        .owned(stats.getOwned())
                        .usersRated(stats.getUsersRated())
                        .bayesAverage(stats.getBayesAverage())
                        .popularityScore(game.getPopularityScore())
                        .build());
            }

            game.setLastStatsRefreshedAt(now);
        }

        if (!snapshots.isEmpty())
            snapshotRepo.saveAll(snapshots);

        gameRepo.saveAll(batch);
        log.info("Refreshed stats for {} games, captured {} snapshots", batch.size(), snapshots.size());
    }

    // moves the cursor past a batch we couldn't score, so the job keeps going
    private void stampAndSave(List<Boardgame> batch) {
        Instant now = Instant.now();
        batch.forEach(game -> game.setLastStatsRefreshedAt(now));
        gameRepo.saveAll(batch);
    }
}
package com.boardwise.backend.shared.services;

import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boardwise.backend.marketplace.enums.Genres;
import com.boardwise.backend.shared.repository.BoardGameRepository;
import com.boardwise.backend.shared.services.scoring.PopularityScorer;
import com.boardwise.backend.shared.dtos.*;
import com.boardwise.backend.shared.model.*;
import com.boardwise.backend.user_service.services.AuthService;
import com.boardwise.backend.user_service.services.R2StorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardGameService {

    private final BoardGameRepository gameRepo;
    private final R2StorageService bucket;
    private final @Qualifier("bggRestClient") RestClient client;
    private final MongoTemplate db;
    private final PopularityScorer scorer;

    private static final Logger log = LoggerFactory.getLogger(BoardGameService.class);
    private final String defaultImageKey = "/rulebooks/default_cover.png"; 
    @Value("${r2.rulebooks.public-url}")
    private String r2BaseUrl;

    @Scheduled(fixedDelay = 6 * 1000)
    public void populateDatabase(){
        int nextBggId = gameRepo.findTopByBggIdNotNullOrderByBggIdDesc()
                        .map(game -> game.getBggId() + 1)
                        .orElse(1);
        
        String ids = IntStream.range(nextBggId, nextBggId + 20)
                        .mapToObj(Integer::toString)
                        .collect(Collectors.joining(","));

        String requestUrl = "/thing?id=" + ids + "&subtype=boardgame&stats=1";
        String response = client.get()
                            .uri(requestUrl)
                            .retrieve()
                            .body(String.class);

        try{
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(response)));

            List<Boardgame> boardgames = new ArrayList<>();
            NodeList nodeList = document.getElementsByTagName("item");
            List<Boardgame> nullGames = gameRepo.findAllByBggIdNull(); // user provided games
            
            for(int i = 0; i < nodeList.getLength(); i++){
                boolean updateEntry = false;
                Node node = nodeList.item(i);

                if(node == null) continue;

                String preBggId = node.getAttributes()
                                    .getNamedItem("id")
                                    .getNodeValue();

                int bggId = Integer.parseInt(preBggId);
                
                Element element = ((Element) node);
                Boardgame game = BggDataParser.parseGame(element);
                String imageURL = game.getImageURL() == null ? (r2BaseUrl + defaultImageKey) : game.getImageURL();
                game.setImageURL(imageURL);
                game.setBggId(bggId);

                BggStats stats = BggDataParser.parseStats(element);
                game.setYearPublished(BggDataParser.parseYearPublished(element));
                if(stats != null){
                    game.setStats(stats);
                    game.setPopularityScore(scorer.score(game));
                    game.setLastStatsRefreshedAt(Instant.now());
                }
                

                for(Boardgame nullGame : nullGames){
                    String nullTitle = nullGame.getTitle().trim().toLowerCase();
                    String newGameTitle = game.getTitle().trim().toLowerCase();
                    if(nullTitle.equals(newGameTitle)){
                        // update nullGame
                        updateEntry = true;
                        
                        nullGame.setBggId(game.getBggId());
                        nullGame.setTitle(game.getTitle());
                        nullGame.setDescription(game.getDescription());
                        nullGame.setImageURL(game.getImageURL());
                        nullGame.setMinPlayers(game.getMinPlayers());
                        nullGame.setMaxPlayers(game.getMaxPlayers());
                        nullGame.setMinAge(game.getMinAge());
                        nullGame.setDuration(game.getDuration());
                        nullGame.setGenres(game.getGenres());

                        gameRepo.save(nullGame);
                        break;
                    }
                }

                if(!updateEntry)
                    boardgames.add(game);
            }

            if(boardgames.size() > 0)
                gameRepo.saveAll(boardgames);
        }
        catch(Exception e){
            log.error("Failed to populate database: {}", e.getMessage(), e);
        }
    }

    public Map<String, Object> getBoardgames(String query, Integer top){
        Map<String, Object> result = new HashMap<>();
        List<Boardgame> dbGames;
        int resultLimit = 12;

        if(query == null && top == null){
            Limit maxRecords = Limit.of(resultLimit);
            dbGames = gameRepo.findAllBy(maxRecords);
        }
        else if(query == null && top != null){
            Query topQuery = new Query();
            topQuery.limit(top);
            topQuery.with(Sort.by(Sort.Direction.DESC, "popularityScore"));
            dbGames = db.find(topQuery, Boardgame.class);
        } 
        else{
            Pattern pattern = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE);
            Criteria searchCrit = Criteria.where("title").regex(pattern);
            Query searchQuery = new Query(searchCrit);
            searchQuery.limit(resultLimit);
            dbGames = db.find(searchQuery, Boardgame.class);

            if(dbGames.size() < resultLimit){
                int remainder = resultLimit - dbGames.size();
                searchBggForGames(query, remainder).forEach((game) -> dbGames.add(game));
            }
            Set<String> seen = new HashSet<>();
            dbGames.removeIf(g -> !seen.add(g.getId()));
        }

        List<GameListDTO> games = new ArrayList<>();
        for(Boardgame game : dbGames){
            GameListDTO dto = new GameListDTO(
                game.getId(),
                game.getTitle(),
                game.getImageURL()
            );
            games.add(dto);
        }
        
        result.put("message", "Boardgames successfully fetched.");
        result.put("boardGames", games);
        
        
        return result; 
    }

    private List<Boardgame> searchBggForGames(String query, int resultSize){
        List<Boardgame> results = new ArrayList<>();
        String requestUrl = "/search?query=" + query + "&type=boardgame";
        String response = client.get()
                            .uri(requestUrl)
                            .retrieve()
                            .body(String.class);

        try{
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(response)));
            NodeList searchNodeList = document.getElementsByTagName("item");

            Set<String> bestResults = new LinkedHashSet<>();
            
            for(int i = 0; bestResults.size() < resultSize && i < searchNodeList.getLength(); i++){
                Element item = (Element) searchNodeList.item(i);
    
                Element gameTitleEL = (Element) item.getElementsByTagName("name").item(0);

                if(gameTitleEL != null && gameTitleEL.getAttribute("value").trim().equalsIgnoreCase(query)){
                    bestResults.add(item.getAttribute("id"));
                }
            }

            for(int j = 0; bestResults.size() < resultSize && j < searchNodeList.getLength(); j++){
                Element item = (Element) searchNodeList.item(j);
                String bggId = item.getAttribute("id");
                bestResults.add(bggId);
            }

            if(bestResults.isEmpty()) 
                return results;

            String ids = String.join(",", bestResults);
            requestUrl = "/thing?id=" + ids + "&subtype=boardgame&stats=1";
            response = client.get()
                            .uri(requestUrl)
                            .retrieve()
                            .body(String.class);
            
            document = builder.parse(new InputSource(new StringReader(response)));
            NodeList nodeList = document.getElementsByTagName("item");
            for(int k = 0; k < nodeList.getLength(); k++){
                Element element = ((Element) nodeList.item(k));
                Boardgame game = BggDataParser.parseGame(element);
                String imageURL = game.getImageURL() == null ? (r2BaseUrl + defaultImageKey) : game.getImageURL();
                game.setImageURL(imageURL);

                BggStats stats = BggDataParser.parseStats(element);
                game.setYearPublished(BggDataParser.parseYearPublished(element));
                if(stats != null){
                    game.setStats(stats);
                    game.setPopularityScore(scorer.score(game));
                    game.setLastStatsRefreshedAt(Instant.now());
                }
                results.add(game);
            }
            // send to async task
            results = saveSearchedBoardgames(results);

        }
        catch(Exception e){
            log.error("Failed to populate database: {}", e.getMessage(), e);
        }

        return results;
    }

    public Map<String, Object> addBoardgame(OtherGameDTO gameInfo, MultipartFile image) throws IOException {
        Map<String, Object> result = new HashMap<>();
        
        // add the user provided game
        String gameTitle = AuthService.sanitize(gameInfo.title());
        String gameDesc = AuthService.sanitize(gameInfo.description());
        List<String> gameGenres = new ArrayList<>();
        for(String genre : gameInfo.genres()){
            String cleanGenre = AuthService.sanitize(genre);
            gameGenres.add(cleanGenre);
        }

        String fileName = bucket.uploadFile(image, gameTitle);
        String imageUrl = bucket.getFileUrl(fileName);

        Boardgame newGame = new Boardgame(
            null,
            null,
            gameTitle,
            gameDesc,
            imageUrl,
            gameInfo.minPlayers(),
            gameInfo.maxPlayers(),
            gameInfo.minAge(),
            gameInfo.duration(),
            gameGenres
        );

        newGame = gameRepo.save(newGame);

        result.put("message", "New game successfully added to database.");

        return result;
    }

    public Map<String, Object> getBoardgameGenres(String query){
        Map<String, Object> result = new HashMap<>();
        List<Genres> genres = new ArrayList<>();
        
        if(query == null){
            for(int i = 0; i < 10; i++){
                genres.add(Genres.values()[i]);
            }
        }
        else{
            int count = 10;
            for(Genres genre : Genres.values()){
                if(genre.getValue().contains(query.toLowerCase())){
                    genres.add(genre);
                    count--;
                }
                if(count < 1)
                    break;
            }
        }
        

        result.put("message", "Genres successfully retrieved.");
        result.put("genres", genres);
        return result;
    }

    private List<Boardgame> saveSearchedBoardgames(List<Boardgame> candidates){
        List<Boardgame> existing = new ArrayList<>();
        List<Boardgame> toSave = new ArrayList<>();

        for(Boardgame candidate : candidates){
            Optional<Boardgame> eGame = gameRepo.findFirstByTitle(candidate.getTitle());
            if(eGame.isPresent())
                existing.add(eGame.get());
            else
                toSave.add(candidate);
        }

        existing.addAll(gameRepo.saveAll(toSave));
        return existing;
    } 
}

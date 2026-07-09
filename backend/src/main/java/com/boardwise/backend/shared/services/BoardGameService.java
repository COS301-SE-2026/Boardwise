package com.boardwise.backend.shared.services;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
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
import com.boardwise.backend.user_service.dtos.GameListDTO;
import com.boardwise.backend.user_service.dtos.OtherGameDTO;
import com.boardwise.backend.user_service.models.Boardgame;
import com.boardwise.backend.user_service.repos.BoardGameRepository;
import com.boardwise.backend.user_service.services.AuthService;
import com.boardwise.backend.user_service.services.R2StorageService;

@Service
public class BoardGameService {

    private final BoardGameRepository gameRepo;
    private final R2StorageService bucket;
    private final RestClient client;
    private static final Logger log = LoggerFactory.getLogger(BoardGameService.class);

    public BoardGameService(
        BoardGameRepository gameRepo,
        R2StorageService bucket, 
        RestClient bggRestClient
    ){
        this.gameRepo = gameRepo;
        this.bucket = bucket;
        this.client = bggRestClient;
    }

    public void populateDatabase(){
        int nextBggId = gameRepo.findTopByBggIdNotNullOrderByBggIdDesc()
                        .map(game -> game.getBggId() + 1)
                        .orElse(1);
        
        String ids = IntStream.range(nextBggId, nextBggId + 20)
                        .mapToObj(Integer::toString)
                        .collect(Collectors.joining(","));

        String requestUrl = "/thing?id=" + ids + "&subtype=boardgame";
        String response = client.get()
                            .uri(requestUrl)
                            .retrieve()
                            .body(String.class);

        try{
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(response)));

            List<Boardgame> boardgames = new ArrayList<>();
            NodeList nodeList = document.getElementsByTagName("item");
            boolean updateEntry = false;
            for(int i = 0; i < nodeList.getLength(); i++){
                Node node = nodeList.item(i);

                String preBggId = node.getAttributes()
                                    .getNamedItem("id")
                                    .getNodeValue();

                int bggId = Integer.parseInt(preBggId);
                
                Element element = ((Element) node);
                String gameTitle = element.getElementsByTagName("name")
                                                    .item(0)
                                                    .getAttributes()
                                                    .getNamedItem("value")
                                                    .getNodeValue();

                String gameDesc = element.getElementsByTagName("description")
                                                    .item(0)
                                                    .getTextContent();

                String gameImage = element.getElementsByTagName("image")
                                                    .item(0)
                                                    .getTextContent();

                String preMin = element.getElementsByTagName("minplayers")
                                            .item(0)
                                            .getAttributes()
                                            .getNamedItem("value")
                                            .getNodeValue();

                int minPlayers = Integer.parseInt(preMin);
                
                String preMax = element.getElementsByTagName("maxplayers")
                                            .item(0)
                                            .getAttributes()
                                            .getNamedItem("value")
                                            .getNodeValue();
                int maxPlayers = Integer.parseInt(preMax);       

                String preDuration = element.getElementsByTagName("playingtime")
                                            .item(0)
                                            .getAttributes()
                                            .getNamedItem("value")
                                            .getNodeValue();
                int duration = Integer.parseInt(preDuration);

                String preMinAge = element.getElementsByTagName("minage")
                                            .item(0)
                                            .getAttributes()
                                            .getNamedItem("value")
                                            .getNodeValue();
                int minAge = Integer.parseInt(preMinAge);

                NodeList gameGenres = element.getElementsByTagName("link");
                List<String> genres = new ArrayList<>();
                for(int j = 0; j < gameGenres.getLength(); j++){
                    Node genreNode = gameGenres.item(j);
                    Node type = genreNode.getAttributes()
                                    .getNamedItem("type");
                    
                    if(type != null && type.getNodeValue().equals("boardgamecategory")){
                        String genre = genreNode.getAttributes().getNamedItem("value").getNodeValue();
                        genres.add(genre);
                    }
                }
                // API game data object
                Boardgame game = new Boardgame(
                    null,
                    bggId,
                    gameTitle,
                    gameDesc,
                    gameImage,
                    minPlayers,
                    maxPlayers,
                    minAge,
                    duration,
                    genres
                );

                List<Boardgame> nullGames = gameRepo.findAllByBggIdNull(); // user provided games
                for(Boardgame nullGame : nullGames){
                    if(game.getTitle().contains(nullGame.getTitle()) || nullGame.getTitle().contains(game.getTitle())){
                        // update nullGame
                        updateEntry = true;
                        
                        nullGame.setBggId(bggId);
                        nullGame.setTitle(gameTitle);
                        nullGame.setDescription(gameDesc);
                        nullGame.setImageURL(gameImage);
                        nullGame.setMinPlayers(minPlayers);
                        nullGame.setMaxPlayers(maxPlayers);
                        nullGame.setMinAge(minAge);
                        nullGame.setDuration(duration);
                        nullGame.setGenres(genres);

                        gameRepo.save(nullGame);
                    }
                }

                if(!updateEntry)
                    boardgames.add(game);
            }

            gameRepo.saveAll(boardgames);
        }
        catch(Exception e){
            log.error("Failed to populate database: {}", e.getMessage(), e);
        }
    }

    public Map<String, Object> getBoardgames(String query){
        Map<String, Object> result = new HashMap<>();
        List<Boardgame> dbGames;

        if(query == null){
            Limit maxRecords = Limit.of(10);
            dbGames = gameRepo.findAllBy(maxRecords);
        }
        else{
            Pageable limit = PageRequest.of(0, 10);
            TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingAny(query);
            dbGames = gameRepo.findAllBy(criteria, limit);
        }

        List<GameListDTO> games = new ArrayList<>();
        for(Boardgame game : dbGames){
            GameListDTO dto = new GameListDTO(
                game.getId(),
                game.getTitle()
            );
            games.add(dto);
        }
        
        result.put("message", "Boardgames successfully fetched.");
        result.put("boardGames", games);
        
        
        return result; 
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
                if(genre.getValue().contains(query)){
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
}

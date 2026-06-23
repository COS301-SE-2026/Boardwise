package com.boardwise.backend.user_service.services;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.boardwise.backend.user_service.models.Boardgame;
import com.boardwise.backend.user_service.repos.BoardGameRepository;

@Service
public class BoardGameService {

    @Value("${bgg.url}")
    private String baseUrl;

    @Value("${bgg.token}")
    private String token;

    private final BoardGameRepository gameRepo;
    private final RestClient client;
    private static final Logger log = LoggerFactory.getLogger(BoardGameService.class);

    BoardGameService(BoardGameRepository gameRepo, RestClient client){
        this.gameRepo = gameRepo;
        this.client = client;
    }

    public void populateDatabase(){
        int nextBggId = gameRepo.findTopByBggIdNotNullOrderByBggIdDesc()
                        .map(game -> game.getBggId() + 1)
                        .orElse(1);
        
        String ids = IntStream.range(nextBggId, nextBggId + 20)
                        .mapToObj(Integer::toString)
                        .collect(Collectors.joining(","));

        String requestUrl = baseUrl + "/thing?id=" + ids + "&subtype=boardgame";
        String response = client.get()
                            .uri(requestUrl)
                            .retrieve()
                            .body(String.class);

        try{
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(response)));

            List<Boardgame> boardgames = new ArrayList<>();
            NodeList nodeList = document.getElementsByTagName("item");
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
                Boardgame game = new Boardgame(preBggId, bggId, gameTitle, gameDesc, gameImage, genres);
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
        List<Boardgame> games;

        if(query == null){
            Limit maxRecords = Limit.of(10);
            games = gameRepo.findAll(maxRecords);
        }
        else{
            Pageable limit = PageRequest.of(0, 10);
            TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingAny(query);
            games = gameRepo.findAllBy(criteria, limit);
        }
        
        result.put("message", "Boardgames successfully fetched.");
        result.put("boardGames", games);
        
        
        return result; 
    }
}

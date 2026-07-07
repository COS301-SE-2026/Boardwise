package com.boardwise.backend.user_service.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.boardwise.backend.user_service.dtos.OtherGameDTO;
import com.boardwise.backend.user_service.services.BoardGameService;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/boardgames")
public class BoardGameController {

    private final BoardGameService service;

    BoardGameController(BoardGameService service){
        this.service = service;
    }

    @GetMapping("/")
    public ResponseEntity<?> getGamesList(
        @RequestParam(required = false) String query
    ){
        Map<String, Object> res = service.getBoardgames(query);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<?> addBoardgame(
        @RequestPart("gameInfo") OtherGameDTO gameInfo,
        @RequestPart("gameImage") MultipartFile image
    ) {
        Map<String, Object> res;
        try{
            res = service.addBoardgame(gameInfo, image);
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        }
        catch(IOException e){
            res = new HashMap<>();
            res.put("error", "Something went wrong while adding a boardgame to the database.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}

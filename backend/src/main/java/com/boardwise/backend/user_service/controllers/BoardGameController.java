package com.boardwise.backend.user_service.controllers;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boardwise.backend.user_service.services.BoardGameService;

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
}

package com.boardwise.scrapers.exceptions;

public class FailedToScrape extends RuntimeException{
    public FailedToScrape(String msg){
        super(msg);
    }
}

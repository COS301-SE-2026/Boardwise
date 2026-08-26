package com.boardwise.backend.user_service.dtos;


import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.boardwise.backend.user_service.enums.Visibility;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

public record EventUpdateDTO(
    @Size(min = 3, message = "Name of an event must be at least 3 characters.")
    String name,
    String description,
    String location,
    @FutureOrPresent(message = "Event date cannot be in the past.")
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    Visibility visibility,
    @Size(min = 1, message = "Event must have at least one game to play.")
    List<String> games
) {

    public EventUpdateDTO{
        if(date != null && startTime != null && endTime != null){
            LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
            LocalDateTime endDateTime = endTime.isBefore(startTime) ?
                                        LocalDateTime.of(date.plusDays(1L), endTime) :
                                        LocalDateTime.of(date, endTime);
            
            if(startDateTime.equals(endDateTime))
                throw new IllegalArgumentException("An event cannot start and end at the same time.");
            else if (Duration.between(startDateTime, endDateTime).toHours() >= 24)
                throw new IllegalArgumentException("An event cannot be 24 hours or longer.");
        }
    }
}

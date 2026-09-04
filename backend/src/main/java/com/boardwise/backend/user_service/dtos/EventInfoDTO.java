package com.boardwise.backend.user_service.dtos;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.boardwise.backend.user_service.enums.Visibility;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EventInfoDTO(
    @NotNull(message = "Name is a required field.")
    @Size(min = 3, message = "Name of an event must be at least 3 characters.")
    String name,

    @NotNull(message = "Description is a required field.")
    @NotBlank(message = "Description cannot be blank.")
    String description,

    @NotNull(message = "Date is required field.")
    @FutureOrPresent(message = "Date field must be a date in the future.")
    LocalDate date,

    @NotNull(message = "Start time is a required field.")
    LocalTime startTime,

    @NotNull(message = "End time is a required field.")
    LocalTime endTime,

    @NotNull(message = "Location is a required field.")
    @Size(min = 3, message = "Location must be at least 3 characters long.")
    String location,

    Visibility visibility,
    
    @NotNull(message = "Games are a required field.")
    @Size(min = 1, message = "There must be at least one game for an event.")
    List<String> games // list of ids, must use our getBoardgames() list
) {
    public EventInfoDTO{
        
        if(visibility == null)
            visibility = Visibility.PUBLIC;
        
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

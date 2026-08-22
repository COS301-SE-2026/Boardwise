package com.boardwise.backend.user_service.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.boardwise.backend.user_service.enums.EventStatus;
import com.boardwise.backend.user_service.enums.RSVPStatus;
import com.boardwise.backend.user_service.enums.Visibility;
import com.boardwise.backend.user_service.models.Event;
import com.fasterxml.jackson.annotation.JsonFormat;

public record EventDTO(
    String id,
    String name,
    String description,
    String imageUrl,
    LocalDate date,

    @JsonFormat(pattern = "HH:mm")
    LocalTime startTime,

    @JsonFormat(pattern = "HH:mm")
    LocalTime endTime,
    int attendeeCount,
    String location,
    Visibility visibility,
    EventStatus eventStatus,
    RSVPStatus rsvpStatus,
    EventHostInfo host,
    boolean isHost,
    List<GameInventoryDTO> games
) {
    public static EventDTO fromEntity(
        Event event,
        int attendeeCount,
        RSVPStatus status,
        EventHostInfo hostInfo,
        boolean isHost,
        List<GameInventoryDTO> games
    ){
        return new EventDTO(
            event.getId(),
            event.getName(),
            event.getDescription(),
            event.getEventImg(),
            event.getStartDateTime().toLocalDate(),
            event.getStartDateTime().toLocalTime(),
            event.getEndDateTime().toLocalTime(),
            attendeeCount,
            event.getLocationText(),
            event.getVisibility(),
            event.getStatus(),
            status,
            hostInfo,
            isHost,
            games
        );
    }
}

package com.boardwise.backend.user_service.services;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@Service 
@RequiredArgsConstructor 
public class GeocodingService {

    private final @Qualifier("osmRestClient") RestClient client;

    public GeoJsonPoint getLocationCoordinates(String locationText){
        try{
            JsonNode res = client.get()
                            .uri(uri -> uri
                                .path("/search")
                                .queryParam("q", locationText)
                                .queryParam("format", "json")
                                .queryParam("limit", 1)
                                .build()
                            )
                            .retrieve()
                            .body(JsonNode.class);

            if(res != null && res.isArray() && res.size() > 0){
                JsonNode match = res.get(0);

                double lat = match.get("lat").asDouble();
                double lng = match.get("lon").asDouble();

                return new GeoJsonPoint(lng, lat);
            }
            else{
                throw new NoSuchElementException("Could not find coordinates for location: " + locationText);
            }
        }
        catch(NoSuchElementException e){
            throw e;
        }
    }
}

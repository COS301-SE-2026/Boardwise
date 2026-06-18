package com.boardwise.backend.marketplace;

import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties.Json;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.boardwise.backend.marketplace.enums.*;
import com.boardwise.backend.marketplace.controller.ListingController;
import com.boardwise.backend.marketplace.dtos.listing.ListingResponse;
import com.boardwise.backend.marketplace.enums.Genres;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.service.ListingService;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.repos.TokenBlackListRepository;
import com.boardwise.backend.user_service.repos.UserRepository;

@WebMvcTest(ListingController.class)
public class ListingControllerTest{

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private ListingService listingService;


    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private TokenBlackListRepository tokenBlackListRepository;

    private ListingResponse buildDefaultResponse(){
        return new ListingResponse("someListingId",
        "some listingTitle",
        "testBuddy",
        new ObjectId(),
        "Monopoly",
        "partial boardgame",
        "sale", 
        24673.0, 
        "Some monoply assets ",
        null,
        "Pretoria",
        true,
        "fair",
        "original",
        List.of(Genres.DICE.getValue()),
        null,
        LocalDateTime.now(),
        LocalDateTime.now(),
        ListingStatus.AVAILABLE
        );
    }

    @Test
    @DisplayName("GET /listings returns 200 with listings")
    @WithMockUser
    public void getAllActiveListingsReturns_200() throws Exception{
        //ARRANGE
        when(listingService.getAllActiveListings()).thenReturn(List.of(buildDefaultResponse()));
        //ACT & ASSERT
         mockMvc.perform(get("/api/marketplace/listings"))
               .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /listings returns 204 with no listings")
    @WithMockUser
    public void getAllActiveListingsReturns_204() throws Exception{
        //ARRANGE
        when(listingService.getAllActiveListings()).thenReturn(List.of());
        //ACT & ASSERT
         mockMvc.perform(get("/api/marketplace/listings"))
               .andExpect(status().isAccepted());
    }

    @Test
    @DisplayName("GET /listings returns 500 with error")
    @WithMockUser
    public void getAllActiveListingsReturns_500() throws Exception{
        //ARRANGE
        when(listingService.getAllActiveListings()).thenThrow(new RuntimeException("boom"));
        //ACT & ASSERT
         mockMvc.perform(get("/api/marketplace/listings"))
               .andExpect(status().is5xxServerError());
    }


    @Test
    @WithMockUser
    @DisplayName("GET /listing returns 200 with a Listing")
    public void getSpecificListingReturns_200() throws Exception{
        // ARRANGE
        String fakeListingId = "someListingId";
        when(listingService.getListingById(fakeListingId)).thenReturn(buildDefaultResponse());

        //ACT & ASSERT
        mockMvc.perform(get("/api/marketplace/listing/someListingId")).andExpect(status().isOk());
    }
}
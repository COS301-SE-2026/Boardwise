package com.boardwise.backend.marketplace;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import com.boardwise.backend.marketplace.controller.ListingController;
import com.boardwise.backend.marketplace.dtos.listing.ListingRequest;
import com.boardwise.backend.marketplace.dtos.listing.ListingResponse;
import com.boardwise.backend.marketplace.enums.Genres;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.exceptions.ForbiddenException;
import com.boardwise.backend.marketplace.service.ListingService;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.repos.TokenBlackListRepository;
import com.boardwise.backend.user_service.repos.UserRepository;

import lombok.With;

@WebMvcTest(ListingController.class)
public class ListingControllerTest{

    private final String defaultIMG = "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/default-listing-images/default.png";

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
        "Some monopoly assets ",
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

    private ListingRequest buildDefaultRequest(){
        return new ListingRequest(
            "full boardGame",
            "sale",
            "some listingTitle",
            2468.2,
            "Monopoly",
            "Boksburg", 
            true, 
            defaultIMG,
            "original",
            "like new", 
            "some description",
            List.of(Genres.DICE.getValue()), 
            null);
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

    @Test
    @WithMockUser
    @DisplayName("GET /listing returns 200")
    public void getSpecificListingReturns_404() throws Exception{
        // ARRANGE
        String fakeListingId = "someListingId";
        when(listingService.getListingById(fakeListingId)).thenThrow(IllegalArgumentException.class);

        //ACT & ASSERT
        mockMvc.perform(get("/api/marketplace/listing/someListingId")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /listing returns 500")
    public void getSpecificListingReturns_500() throws Exception{
        // ARRANGE
        String fakeListingId = "someListingId";
        when(listingService.getListingById(fakeListingId)).thenThrow(new RuntimeException());

        //ACT & ASSERT
        mockMvc.perform(get("/api/marketplace/listing/someListingId")).andExpect(status().is5xxServerError());
    }


    @Test
    @WithMockUser
    @DisplayName("POST /listings should return 200")
    public void postCreateListingReturns_200() throws Exception{
        //ARRANGE
        MockMultipartFile image = new MockMultipartFile("image", "picture.png", "image/png", new byte[]{1,2,3});
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
        "{\"itemType\":\"full boardGame\",\"listingType\":\"sale\",\"listingTitle\":\"some listingTitle\",\"price\":2468.2,\"gameTitle\":\"Monopoly\",\"location\":\"Boksburg\",\"isNegotiable\":true,\"imageUrl\":\"fake\",\"version\":\"original\",\"condition\":\"like new\",\"description\":\"some description\",\"genres\":[\"dice\"],\"rentalPeriod\":null}".getBytes());

        when(listingService.createListing(any(), any(), any())).thenReturn(buildDefaultResponse());

        //ACT & ASSERT
        mockMvc.perform(multipart("/api/marketplace/listings").file(image).file(data).header("Authorization", "Bearer fake").with(csrf())).andExpect(status().isOk());        
    }

    @Test
    @WithMockUser
    @DisplayName("POST /listings should return 403 BAD REQUEST")
    public void postCreateListingReturns_403() throws Exception{
        //ARRANGE
        MockMultipartFile image = new MockMultipartFile("image", "picture.png", "image/png", new byte[]{1,2,3});
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
        "{\"itemType\":\"full boardGame\",\"listingType\":\"sale\",\"listingTitle\":\"some listingTitle\",\"price\":2468.2,\"gameTitle\":\"Monopoly\",\"location\":\"Boksburg\",\"isNegotiable\":true,\"imageUrl\":\"fake\",\"version\":\"original\",\"condition\":\"like new\",\"description\":\"some description\",\"genres\":[\"dice\"],\"rentalPeriod\":null}".getBytes());

        when(listingService.createListing(any(), any(), any())).thenThrow(IllegalArgumentException.class);

        //ACT & ASSERT
        mockMvc.perform(multipart("/api/marketplace/listings").file(image).file(data).header("Authorization", "Bearer fake").with(csrf())).andExpect(status().isBadRequest());        
    }


    @Test
    @WithMockUser
    @DisplayName("POST /listings should return 500 INTERNAL SERVER ERROR")
    public void postCreateListingReturns_500() throws Exception{
        //ARRANGE
        MockMultipartFile image = new MockMultipartFile("image", "picture.png", "image/png", new byte[]{1,2,3});
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
        "{\"itemType\":\"full boardGame\",\"listingType\":\"sale\",\"listingTitle\":\"some listingTitle\",\"price\":2468.2,\"gameTitle\":\"Monopoly\",\"location\":\"Boksburg\",\"isNegotiable\":true,\"imageUrl\":\"fake\",\"version\":\"original\",\"condition\":\"like new\",\"description\":\"some description\",\"genres\":[\"dice\"],\"rentalPeriod\":null}".getBytes());

        when(listingService.createListing(any(), any(), any())).thenThrow(RuntimeException.class);

        //ACT & ASSERT
        mockMvc.perform(multipart("/api/marketplace/listings")
        .file(image)
        .file(data)
        .with(csrf())
        .with(request->{
            request.setMethod("PATCH"); 
            return request;})
        .header("Authorization", "Bearer fake"))
        .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser 
    @DisplayName("PATCH /listing/{id} should return 200 OK")
    public void patchUpdateListingReturns_200() throws Exception{
        //ARRANGE
        MockMultipartFile image = new MockMultipartFile("image", "picture.png", "image/png", new byte[]{1,2,3});
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
        "{\"itemType\":\"full boardGame\",\"listingType\":\"sale\",\"listingTitle\":\"some listingTitle\",\"price\":2468.2,\"gameTitle\":\"Monopoly\",\"location\":\"Boksburg\",\"isNegotiable\":true,\"imageUrl\":\"fake\",\"version\":\"original\",\"condition\":\"like new\",\"description\":\"some description\",\"genres\":[\"dice\"],\"rentalPeriod\":null}".getBytes());

        when(listingService.updateListing(any(),any(), any(), any())).thenReturn(buildDefaultResponse());

        //ACT & ASSERT
        mockMvc.perform(multipart("/api/marketplace/listing/someListingId")
        .file(image)
        .file(data)
        .with(csrf())
        .with(request->{
            request.setMethod("PATCH"); 
            return request;})
        .header("Authorization", "Bearer fake")).andExpect(status().isOk());        
    }
    
    @Test
    @WithMockUser 
    @DisplayName("PATCH /listing/{id} should return 404 NOT FOUND")
    public void patchUpdateListingReturns_404() throws Exception{
        //ARRANGE
        MockMultipartFile image = new MockMultipartFile("image", "picture.png", "image/png", new byte[]{1,2,3});
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
        "{\"itemType\":\"full boardGame\",\"listingType\":\"sale\",\"listingTitle\":\"some listingTitle\",\"price\":2468.2,\"gameTitle\":\"Monopoly\",\"location\":\"Boksburg\",\"isNegotiable\":true,\"imageUrl\":\"fake\",\"version\":\"original\",\"condition\":\"like new\",\"description\":\"some description\",\"genres\":[\"dice\"],\"rentalPeriod\":null}".getBytes());

        when(listingService.updateListing(any(),any(), any(), any())).thenThrow(new IllegalArgumentException());

        //ACT & ASSERT
        mockMvc.perform(multipart("/api/marketplace/listing/someListingId")
        .file(image)
        .file(data)
        .header("Authorization", "Bearer fake")
        .with(csrf()) 
        .with(request->{
            request.setMethod("PATCH"); 
            return request;})
        ).andExpect(status().isNotFound());        
    }

    @Test
    @WithMockUser 
    @DisplayName("PATCH /listing/{id} should return 403 FORBIDDEN")
    public void patchUpdateListingReturns_403() throws Exception{
        //ARRANGE
        MockMultipartFile image = new MockMultipartFile("image", "picture.png", "image/png", new byte[]{1,2,3});
        MockMultipartFile data = new MockMultipartFile("data", "", "application/json",
        "{\"itemType\":\"full boardGame\",\"listingType\":\"sale\",\"listingTitle\":\"some listingTitle\",\"price\":2468.2,\"gameTitle\":\"Monopoly\",\"location\":\"Boksburg\",\"isNegotiable\":true,\"imageUrl\":\"fake\",\"version\":\"original\",\"condition\":\"like new\",\"description\":\"some description\",\"genres\":[\"dice\"],\"rentalPeriod\":null}".getBytes());

        when(listingService.updateListing(any(),any(), any(), any())).thenThrow(new ForbiddenException("err"));

        //ACT & ASSERT
        mockMvc.perform(multipart("/api/marketplace/listing/someListingId")
        .file(image)
        .file(data)
        .header("Authorization", "Bearer fake")
        .with(csrf())
        .with(request->{
            request.setMethod("PATCH"); 
            return request;}))
        .andExpect(status().isForbidden());        
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE returns 204 No Content")
    public void deleteListingReturns_204() throws Exception{
        //ARRANGE
        doNothing().when(listingService).deleteListing(any(), any());
        //ACT & ASSERT
        mockMvc.perform(delete("/api/marketplace/listing/someId")
        .header("Authorization", "Bearer fake")
        .with(csrf()))
        .andExpect(status().isNoContent());        
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE returns 404 NOT FOUND")
    public void deleteListingReturns_404() throws Exception{
        //ARRANGE
        doThrow(new IllegalArgumentException()).when(listingService).deleteListing(any(),any());
        //ACT & ASSERT
        mockMvc.perform(delete("/api/marketplace/listing/someId")
        .header("Authorization", "Bearer fake")
        .with(csrf()))
        .andExpect(status().isNotFound());    
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE returns 403 FORBIDDEN")
    public void deleteListingReturns_403() throws Exception{
        //ARRANGE
        doThrow(new ForbiddenException("err")).when(listingService).deleteListing(any(),any());
        //ACT & ASSERT
        mockMvc.perform(delete("/api/marketplace/listing/someId")
        .header("Authorization", "Bearer fake")
        .with(csrf()))
        .andExpect(status().isForbidden());    
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE returns 500 Internal Server Error")
    public void deleteListingReturns_500() throws Exception{
        //ARRANGE
        doThrow(new RuntimeException()).when(listingService).deleteListing(any(),any());
        //ACT & ASSERT
        mockMvc.perform(delete("/api/marketplace/listing/someId")
        .header("Authorization", "Bearer fake")
        .with(csrf()))
        .andExpect(status().isInternalServerError());    
    }

    @Test
    @WithMockUser 
    @DisplayName("GET user listings returns 200 OK")
    public void getUserListingsReturns_200() throws Exception{
        //ARRANGE
        when(listingService.getUserListings(any())).thenReturn(List.of(buildDefaultResponse()));

        //ACT & ASSERT
        mockMvc.perform(get("/api/marketplace/listings/user").header("Authorization", "Bearer fake")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("GET user listings returns 204 No Content")
    public void getUserListingsReturns_204() throws Exception{
        //ARRANGE 
        when(listingService.getUserListings(any())).thenReturn(List.of());
        //ACT & ASSERT
        mockMvc.perform(get("/api/marketplace/listings/user").header("Authorization", "Bearer fake")).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    @DisplayName("GET user listings returns 500 Internal Server Error")
    public void getUserListingsReturns_500() throws Exception{
        //ARRANGE 
        when(listingService.getUserListings(any())).thenThrow(new RuntimeException());
        //ACT & ASSERT
        mockMvc.perform(get("/api/marketplace/listings/user")
        .header("Authorization", "Bearer fake"))
        .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    @DisplayName("GET filtered listings 200 OK")
    public void getFilteredListingsReturns_200() throws Exception{
        //ARRANGE   
        Page<ListingResponse> page = new PageImpl(List.of(buildDefaultResponse()));

        when(listingService.getByFilter(any(), any(), any(), any(), any(),
            any(),any(), any(),any(), any())).thenReturn(page);
        
        //ACT & ASSERT

        mockMvc.perform(get("/api/marketplace/listings/search")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("GET filtered listings 204 No Content")
    public void getFilteredListingsReturns_204() throws Exception{
        //ARRANGE   
        Page<ListingResponse> page = new PageImpl(List.of());

        when(listingService.getByFilter(any(), any(), any(), any(), any(),
            any(),any(), any(),any(), any())).thenReturn(page);
        
        //ACT & ASSERT

        mockMvc.perform(get("/api/marketplace/listings/search")).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    @DisplayName("GET filtered listings 500 Internal Server Error")
    public void getFilteredListingsReturns_500() throws Exception{
        //ARRANGE   
        Page<ListingResponse> page = new PageImpl(List.of());

        when(listingService.getByFilter(any(), any(), any(), any(), any(),
            any(),any(), any(),any(), any())).thenThrow(new RuntimeException());
        
        //ACT & ASSERT
        mockMvc.perform(get("/api/marketplace/listings/search")).andExpect(status().isInternalServerError());
    }
    


}

package com.boardwise.backend.marketplace.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import com.boardwise.backend.marketplace.dtos.listing.ListingResponse;
import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;
import com.boardwise.backend.marketplace.enums.Genres;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.exceptions.ForbiddenException;
import com.boardwise.backend.marketplace.service.ListingService;
import com.boardwise.backend.marketplace.service.RetailService;
import com.boardwise.backend.shared.config.SecurityConfig;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.shared.security.JwtFilter;
import com.boardwise.backend.user_service.repos.TokenBlackListRepository;
import com.boardwise.backend.user_service.repos.UserRepository;
import com.boardwise.backend.user_service.services.MyUserDetailsService;


@WebMvcTest(ListingController.class)
@Import({SecurityConfig.class, JwtFilter.class})
public class ListingControllerTest{
    @MockitoBean
    private RetailService retailService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private ListingService listingService;

    @MockitoBean
    MyUserDetailsService userDetailsService;

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
        ListingStatus.AVAILABLE
        );
    }

    @BeforeEach
    void setup() throws Exception{
        final String validToken = "valid-test-token";
        final ObjectId userId = new ObjectId();

        when(jwtService.extractUserId(validToken)).thenReturn(userId);
        
        UserDetails mockUser = org.springframework.security.core.userdetails.User
            .withUsername(userId.toHexString())
            .password("notUsed")
            .roles("USER")
            .build();
        when(userDetailsService.loadUserByUserId(userId.toHexString())).thenReturn(mockUser);
        when(jwtService.validateToken(validToken, mockUser)).thenReturn(true);
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
               .andExpect(status().isNoContent());
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
        mockMvc.perform(multipart("/api/marketplace/listings").file(image).file(data).header("Authorization", "Bearer valid-test-token").with(csrf())).andExpect(status().isOk());        
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
        mockMvc.perform(multipart("/api/marketplace/listings").file(image).file(data).header("Authorization", "Bearer valid-test-token").with(csrf())).andExpect(status().isBadRequest());        
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
        .header("Authorization", "Bearer valid-test-token"))
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
        .header("Authorization", "Bearer valid-test-token")).andExpect(status().isOk());        
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
        .header("Authorization", "Bearer valid-test-token")
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
        .header("Authorization", "Bearer valid-test-token")
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
        .header("Authorization", "Bearer valid-test-token")
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
        .header("Authorization", "Bearer valid-test-token")
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
        .header("Authorization", "Bearer valid-test-token")
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
        .header("Authorization", "Bearer valid-test-token")
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
        mockMvc.perform(get("/api/marketplace/listings/user").header("Authorization", "Bearer valid-test-token")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("GET user listings returns 204 No Content")
    public void getUserListingsReturns_204() throws Exception{
        //ARRANGE 
        when(listingService.getUserListings(any())).thenReturn(List.of());
        //ACT & ASSERT
        mockMvc.perform(get("/api/marketplace/listings/user").header("Authorization", "Bearer valid-test-token")).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    @DisplayName("GET user listings returns 500 Internal Server Error")
    public void getUserListingsReturns_500() throws Exception{
        //ARRANGE 
        when(listingService.getUserListings(any())).thenThrow(new RuntimeException());
        //ACT & ASSERT
        mockMvc.perform(get("/api/marketplace/listings/user")
        .header("Authorization", "Bearer valid-test-token"))
        .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    @DisplayName("GET filtered listings 200 OK")
    public void getFilteredListingsReturns_200() throws Exception{
        //ARRANGE   
        Page<ListingResponse> page = new PageImpl<>(List.of(buildDefaultResponse()));

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
        Page<ListingResponse> page = new PageImpl<>(List.of());

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
        when(listingService.getByFilter(any(), any(), any(), any(), any(),
            any(),any(), any(),any(), any())).thenThrow(new RuntimeException());
        
        //ACT & ASSERT
        mockMvc.perform(get("/api/marketplace/listings/search")).andExpect(status().isInternalServerError());
    }

    //RETAILER TESTS
@Test
@WithMockUser
@DisplayName("GET Personalized Retail 200 OK with valid token")
public void getPersonalizedRetailListings_200() throws Exception{
    //ARRANGE
    RetailSourceItemDTO a = new RetailSourceItemDTO("Something cool, i guess","Takealot","somevalidurl",0.00,"imagineanimageHere.someformat",0.0f);
    Page<RetailSourceItemDTO> fakeObjs = new PageImpl<>(List.of(a,a,a,a,a));

    when(retailService.getPersonalizedRetailListings("valid-test-token", 0)).thenReturn(fakeObjs);

    //ACT & ASSERT
    mockMvc.perform(get("/api/marketplace/listings/retail/personalized")
            .header("Authorization", "Bearer valid-test-token"))
        .andExpect(status().isOk());
}

@Test
@WithMockUser
@DisplayName("GET Personalized Retail 500 Internal Server Error")
public void getPersonalizedRetailListings_500() throws Exception{
    //ARRANGE
    when(retailService.getPersonalizedRetailListings("valid-test-token", 0))
        .thenThrow(new RuntimeException("boom"));

    //ACT & ASSERT
    mockMvc.perform(get("/api/marketplace/listings/retail/personalized")
            .header("Authorization", "Bearer valid-test-token"))
        .andExpect(status().isInternalServerError());
}

}

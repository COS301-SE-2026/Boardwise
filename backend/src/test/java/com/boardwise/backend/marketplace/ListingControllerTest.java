package com.boardwise.backend.marketplace;

import com.boardwise.backend.marketplace.controller.ListingController;
import com.boardwise.backend.marketplace.dtos.listing.ListingRequest;
import com.boardwise.backend.marketplace.dtos.listing.ListingResponse;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.exceptions.ForbiddenException;
import com.boardwise.backend.marketplace.repository.ListingRepository;
import com.boardwise.backend.marketplace.service.ListingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(MockitoExtension.class)
public class ListingControllerTest {

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        @Mock
        private ListingService listingService;

        @Mock
        private ListingRepository listingRepository;

        @InjectMocks
        private ListingController listingController;

        private ListingResponse mockResponse;
        private final String token = "Bearer test.jwt.token";

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.standaloneSetup(listingController).build();
                objectMapper = new ObjectMapper();

                mockResponse = new ListingResponse(
                                "listing123", "testuser", "Catan", "boardgame", "sale",
                                250.00, "Barely used copy of Catan", null,
                                List.of("economic"), null,
                                LocalDateTime.now(), LocalDateTime.now(), ListingStatus.AVAILABLE);
        }

        // AC-MKT-01: GET ALL LISTINGS

        @Test
        void getAllListings_returns_200_with_listings() throws Exception {
                when(listingService.getAllActiveListings()).thenReturn(List.of(mockResponse));

                mockMvc.perform(get("/api/marketplace/listings"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].gameTitle").value("Catan"));
        }

        @Test
        void getAllListings_returns_204_when_empty() throws Exception {
                when(listingService.getAllActiveListings()).thenReturn(List.of());

                mockMvc.perform(get("/api/marketplace/listings"))
                                .andExpect(status().isNoContent());
        }

        // AC-MKT-03: CREATE LISTING

        @Test
        void createListing_returns_201_on_success() throws Exception {
                when(listingService.createListing(any(), any(), any())).thenReturn(mockResponse);

                String json = objectMapper.writeValueAsString(
                                new ListingRequest("boardgame", "sale", 250.00, "Catan",
                                                "Barely used copy of Catan", List.of("economic"), List.of()));

                MockMultipartFile data = new MockMultipartFile(
                                "data", "", "application/json", json.getBytes());
                MockMultipartFile image = new MockMultipartFile(
                                "image", "test.jpg", "image/jpeg", "fake".getBytes());

                mockMvc.perform(multipart("/api/marketplace/listings")
                                .file(data)
                                .file(image)
                                .header("Authorization", token))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.gameTitle").value("Catan"));
        }

        @Test
        void createListing_returns_422_on_invalid_data() throws Exception {
                when(listingService.createListing(any(), any(), any()))
                                .thenThrow(new IllegalArgumentException("Invalid item type"));

                String json = objectMapper.writeValueAsString(
                                new ListingRequest("boardgame", "sale", 250.00, "Catan",
                                                "Barely used copy of Catan", List.of("economic"), List.of()));

                MockMultipartFile data = new MockMultipartFile(
                                "data", "", "application/json", json.getBytes());
                MockMultipartFile image = new MockMultipartFile(
                                "image", "test.jpg", "image/jpeg", "fake".getBytes());

                mockMvc.perform(multipart("/api/marketplace/listings")
                                .file(data)
                                .file(image)
                                .header("Authorization", token))
                                .andExpect(status().isUnprocessableEntity());
        }

        // AC-MKT-04: UPDATE LISTING

        @Test
        void updateListing_returns_200_on_success() throws Exception {
                when(listingService.updateListing(any(), any(), any())).thenReturn(mockResponse);

                String json = objectMapper.writeValueAsString(
                                new ListingRequest("boardgame", "sale", 250.00, "Catan",
                                                "Updated description", List.of("economic"), null)); // ✅ null not
                                                                                                    // List.of()

                mockMvc.perform(patch("/api/marketplace/listings/listing123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                                .header("Authorization", token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.gameTitle").value("Catan"));
        }

        @Test
        void updateListing_returns_404_when_not_found() throws Exception {
                when(listingService.updateListing(any(), any(), any()))
                                .thenThrow(new IllegalArgumentException("Listing not found"));

                String json = objectMapper.writeValueAsString(
                                new ListingRequest("boardgame", "sale", 250.00, "Catan",
                                                "Updated description", List.of("economic"), List.of()));

                mockMvc.perform(patch("/api/marketplace/listings/listing123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                                .header("Authorization", token))
                                .andExpect(status().isNotFound());
        }

        @Test
        void updateListing_returns_403_when_not_owner() throws Exception {
                when(listingService.updateListing(any(), any(), any()))
                                .thenThrow(new ForbiddenException("You do not own this listing"));

                String json = objectMapper.writeValueAsString(
                                new ListingRequest("boardgame", "sale", 250.00, "Catan",
                                                "Updated description", List.of("economic"), List.of()));

                mockMvc.perform(patch("/api/marketplace/listings/listing123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                                .header("Authorization", token))
                                .andExpect(status().isForbidden());
        }

        // AC-MKT-05: DELETE LISTING

        @Test
        void deleteListing_returns_204_on_success() throws Exception {
                doNothing().when(listingService).deleteListing(any(), any());

                mockMvc.perform(delete("/api/marketplace/listings/listing123")
                                .header("Authorization", token))
                                .andExpect(status().isNoContent());
        }

        @Test
        void deleteListing_returns_404_when_not_found() throws Exception {
                doThrow(new IllegalArgumentException("Listing not found"))
                                .when(listingService).deleteListing(any(), any());

                mockMvc.perform(delete("/api/marketplace/listings/listing123")
                                .header("Authorization", token))
                                .andExpect(status().isNotFound());
        }

        @Test
        void deleteListing_returns_403_when_not_owner() throws Exception {
                doThrow(new ForbiddenException("You do not own this listing"))
                                .when(listingService).deleteListing(any(), any());

                mockMvc.perform(delete("/api/marketplace/listings/listing123")
                                .header("Authorization", token))
                                .andExpect(status().isForbidden());
        }

        // AC-MKT-06: GET USER LISTINGS

        // TODO: fix MissingPathVariableException mismatch
        // @Test
        // void getUserListings_returns_200_with_listings() throws Exception {
        //         when(listingService.getUserListings(any())).thenReturn(List.of(mockResponse));

        //        MvcResult result = mockMvc.perform(get("/api/marketplace/listings/testuser")
        //                         .header("Authorization", token)).andReturn();
        //                         // .andExpect(status().isOk())
        //                         // .andExpect(jsonPath("$[0].username").value("testuser"));
        //         Exception crash = result.getResolvedException();
        //         if (crash != null) {
        //                 System.out.println("============== THE REAL ERROR ==============");
        //                 crash.printStackTrace();
        //                 System.out.println("============================================");
        //         } else {
        //                 System.out.println("No exception was thrown, but the status was: " + result.getResponse().getStatus());
        //                 System.out.println("Response body: " + result.getResponse().getContentAsString());
        //         }
        
        // }

        // TODO: fix it
        // @Test
        // void getUserListings_returns_204_when_empty() throws Exception {
        //         when(listingService.getUserListings(any())).thenReturn(List.of());

        //         mockMvc.perform(get("/api/marketplace/listings/testuser")
        //                         .header("Authorization", token))
        //                         .andExpect(status().isNoContent());
        // }
}
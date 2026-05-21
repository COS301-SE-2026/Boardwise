package com.boardwise.backend.marketplace;

import com.boardwise.backend.marketplace.controller.ListingController;
import com.boardwise.backend.marketplace.dtos.listing.ListingRequest;
import com.boardwise.backend.marketplace.dtos.listing.ListingResponse;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.exceptions.ForbiddenException;
import com.boardwise.backend.marketplace.service.ListingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ListingControllerTest {

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        @Mock
        private ListingService listingService;

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

        // --- AC-MKT-01: GET ALL LISTINGS ---------------------------------------------

        @Test
        void getAllListings_returns_200_with_listings() throws Exception {
                when(listingService.getAllActiveListings()).thenReturn(List.of(mockResponse));

                mockMvc.perform(get("/api/marketplace/listings"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].gameTitle").value("Catan"));
        }

        @Test
        void getAllListings_returns_202_when_empty() throws Exception {
                // Controller returns 202 Accepted (not 204) when the list is empty —
                // ResponseEntity.accepted().body(null). This is a quirk of the current
                // implementation; the test mirrors it faithfully.
                when(listingService.getAllActiveListings()).thenReturn(List.of());

                mockMvc.perform(get("/api/marketplace/listings"))
                                .andExpect(status().isAccepted());
        }

        // --- AC-MKT-02: GET LISTING BY ID --------------------------------------------

        @Test
        void getListingById_returns_200_when_found() throws Exception {
                // Actual mapping: GET /api/marketplace/listing/{listingId} (singular)
                when(listingService.getListingById("listing123")).thenReturn(mockResponse);

                mockMvc.perform(get("/api/marketplace/listing/listing123"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.gameTitle").value("Catan"));
        }

        @Test
        void getListingById_returns_404_when_not_found() throws Exception {
                when(listingService.getListingById("bad-id"))
                                .thenThrow(new IllegalArgumentException("Listing not found"));

                mockMvc.perform(get("/api/marketplace/listing/bad-id"))
                                .andExpect(status().isNotFound());
        }

        // --- AC-MKT-03: CREATE LISTING -----------------------------------------------

        @Test
        void createListing_returns_200_on_success() throws Exception {
                // Controller returns 200 OK on success (ResponseEntity.ok(response))
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
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.gameTitle").value("Catan"));
        }

        @Test
        void createListing_returns_400_on_invalid_data() throws Exception {
                // Controller catches IllegalArgumentException and returns 400 Bad Request
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
                                .andExpect(status().isBadRequest());
        }

        // --- AC-MKT-04: UPDATE LISTING -----------------------------------------------

        // The update endpoint consumes multipart/form-data just like create.
        // Mapping: PATCH /api/marketplace/update/listing/{listingId}

        @Test
        void updateListing_returns_200_on_success() throws Exception {
                when(listingService.updateListing(any(), any(), any(), any())).thenReturn(mockResponse);

                String json = objectMapper.writeValueAsString(
                                new ListingRequest("boardgame", "sale", 250.00, "Catan",
                                                "Updated description", List.of("economic"), null));

                MockMultipartFile data = new MockMultipartFile(
                                "data", "", "application/json", json.getBytes());

                mockMvc.perform(multipart("/api/marketplace/update/listing/listing123")
                                .file(data)
                                // image is optional — omitting it exercises the required=false branch
                                .header("Authorization", token)
                                .with(req -> {
                                        req.setMethod("PATCH");
                                        return req;
                                }))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.gameTitle").value("Catan"));
        }

        @Test
        void updateListing_returns_404_when_not_found() throws Exception {
                when(listingService.updateListing(any(), any(), any(), any()))
                                .thenThrow(new IllegalArgumentException("Listing not found"));

                String json = objectMapper.writeValueAsString(
                                new ListingRequest("boardgame", "sale", 250.00, "Catan",
                                                "Updated description", List.of("economic"), List.of()));

                MockMultipartFile data = new MockMultipartFile(
                                "data", "", "application/json", json.getBytes());

                mockMvc.perform(multipart("/api/marketplace/update/listing/listing123")
                                .file(data)
                                .header("Authorization", token)
                                .with(req -> {
                                        req.setMethod("PATCH");
                                        return req;
                                }))
                                .andExpect(status().isNotFound());
        }

        @Test
        void updateListing_returns_403_when_not_owner() throws Exception {
                when(listingService.updateListing(any(), any(), any(), any()))
                                .thenThrow(new ForbiddenException("You do not own this listing"));

                String json = objectMapper.writeValueAsString(
                                new ListingRequest("boardgame", "sale", 250.00, "Catan",
                                                "Updated description", List.of("economic"), List.of()));

                MockMultipartFile data = new MockMultipartFile(
                                "data", "", "application/json", json.getBytes());

                mockMvc.perform(multipart("/api/marketplace/update/listing/listing123")
                                .file(data)
                                .header("Authorization", token)
                                .with(req -> {
                                        req.setMethod("PATCH");
                                        return req;
                                }))
                                .andExpect(status().isForbidden());
        }

        // --- AC-MKT-05: DELETE LISTING -----------------------------------------------

        // Actual mapping: DELETE /api/marketplace/delete/listing/{listingId}

        @Test
        void deleteListing_returns_204_on_success() throws Exception {
                doNothing().when(listingService).deleteListing(any(), any());

                mockMvc.perform(delete("/api/marketplace/delete/listing/listing123")
                                .header("Authorization", token))
                                .andExpect(status().isNoContent());
        }

        @Test
        void deleteListing_returns_404_when_not_found() throws Exception {
                doThrow(new IllegalArgumentException("Listing not found"))
                                .when(listingService).deleteListing(any(), any());

                mockMvc.perform(delete("/api/marketplace/delete/listing/listing123")
                                .header("Authorization", token))
                                .andExpect(status().isNotFound());
        }

        @Test
        void deleteListing_returns_403_when_not_owner() throws Exception {
                doThrow(new ForbiddenException("You do not own this listing"))
                                .when(listingService).deleteListing(any(), any());

                mockMvc.perform(delete("/api/marketplace/delete/listing/listing123")
                                .header("Authorization", token))
                                .andExpect(status().isForbidden());
        }

        // --- AC-MKT-06: GET USER LISTINGS --------------------------------------------

        // Actual mapping: GET /api/marketplace/listings/user (username comes from
        // token,
        // not a path variable — there is no {username} param in the controller).

        @Test
        void getUserListings_returns_200_with_listings() throws Exception {
                when(listingService.getUserListings(any())).thenReturn(List.of(mockResponse));

                mockMvc.perform(get("/api/marketplace/listings/user")
                                .header("Authorization", token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].username").value("testuser"));
        }

        @Test
        void getUserListings_returns_204_when_empty() throws Exception {
                when(listingService.getUserListings(any())).thenReturn(List.of());

                mockMvc.perform(get("/api/marketplace/listings/user")
                                .header("Authorization", token))
                                .andExpect(status().isNoContent());
        }

        // --- FILTER ------------------------------------------------------------------

        @Test
        void getFilteredListings_returns_200_with_results() throws Exception {
                when(listingService.getByFilter(any(), any(), any(), any(), any()))
                                .thenReturn(List.of(mockResponse));

                mockMvc.perform(get("/api/marketplace/listings/search")
                                .param("listingType", "sale")
                                .param("itemType", "boardgame"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].listingType").value("sale"));
        }

        @Test
        void getFilteredListings_returns_204_when_no_results() throws Exception {
                when(listingService.getByFilter(any(), any(), any(), any(), any()))
                                .thenReturn(List.of());

                mockMvc.perform(get("/api/marketplace/listings/search"))
                                .andExpect(status().isNoContent());
        }
}
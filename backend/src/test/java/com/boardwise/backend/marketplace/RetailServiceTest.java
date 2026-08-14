package com.boardwise.backend.marketplace;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import com.boardwise.backend.marketplace.dtos.retailsource.RetailSourceItemDTO;
import com.boardwise.backend.marketplace.service.RetailService;
import com.boardwise.backend.marketplace.service.webscrapers.BobShopScraper;
import com.boardwise.backend.marketplace.service.webscrapers.TakealotScraper;
import com.boardwise.backend.marketplace.service.webscrapers.ToysRUsScraper;


@DisplayName("Retail Service Tests")
@ExtendWith(MockitoExtension.class) // auto create/inject mocks
public class RetailServiceTest {

    
    @InjectMocks
    private RetailService retailService;
    @Mock 
    TakealotScraper ts;
    
    @Mock
    BobShopScraper bss;

    @Mock
    ToysRUsScraper trus;

    //helper: Generate random RetailSericeItemDTO

    RetailSourceItemDTO genValidRetailSourceItemDTO(String retailerName, String toSearch, Double price, float JWValue ){
        if(retailerName.isBlank() || !retailerName.equalsIgnoreCase("Takealot") || !retailerName.equalsIgnoreCase("BobShop") || !retailerName.equalsIgnoreCase("ToysRUs")){
            throw new IllegalArgumentException("Invalid Retailer Name");
        }
        String url = "http://validurl.com";
        String imageUrl =  "http://validimageurl.com";
        return new RetailSourceItemDTO(toSearch, retailerName, url, price, imageUrl, JWValue);   
    }

    @DisplayName("Should return a list of retail source item DTOS on success")
    @Test
    public void shouldReturnAListOfRetailSourceItemDTOs(){
        //ARRANGE 
        String toSearch = "ExampleString";

        List <RetailSourceItemDTO> tsResults = new ArrayList<>();
        tsResults.add(genValidRetailSourceItemDTO("Takealot","Monopoly board game Test 1 ", 500.00, 0.31f));
        tsResults.add(genValidRetailSourceItemDTO("Takealot","Monopoly board game Test 2 ", 247.24, 0.32f));

        List <RetailSourceItemDTO> bssResults = new ArrayList<>();
        bssResults.add(genValidRetailSourceItemDTO("BobShop","Monopoly board game Test 3 ", 123.53, 0.57f));
        bssResults.add(genValidRetailSourceItemDTO("BobShop","Monopoly board game Test 4 ", 347.24, 0.42f));

        List <RetailSourceItemDTO> trusResults = new ArrayList<>();
        trusResults.add(genValidRetailSourceItemDTO("ToysRus","Monopoly board game Test 5 ", 543.21, 0.81f));
        trusResults.add(genValidRetailSourceItemDTO("ToysRus","Monopoly board game Test 6 ", 299.24, 0.02f));

        when(ts.scrape(toSearch)).thenReturn(tsResults);
        when(trus.scrape(toSearch)).thenReturn(trusResults);
        when(bss.scrape(toSearch)).thenReturn(bssResults);

        //ACT
        List <RetailSourceItemDTO> finalList = retailService.findWebListings(toSearch);

        //ASSERT 
        assertNotNull(finalList);
        assertTrue(!finalList.isEmpty());
        assertTrue(finalList.size() == 6);
    }
    public void shouldReturnAnEmptyList(){
        //ARRANGE 
        String toSearch = "ExampleString";
        when(ts.scrape(toSearch)).thenReturn(null);
        when(trus.scrape(toSearch)).thenReturn(null);
        when(bss.scrape(toSearch)).thenReturn(null);

        //ACT
        List <RetailSourceItemDTO> finalList = retailService.findWebListings(toSearch);

        //ASSERT 
        assertNotNull(finalList);
        assertTrue(!finalList.isEmpty());
    }
    

}


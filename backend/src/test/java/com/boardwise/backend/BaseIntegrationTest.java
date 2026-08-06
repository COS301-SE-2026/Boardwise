package com.boardwise.backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.boardwise.backend.user_service.services.MyUserDetailsService;

@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseIntegrationTest extends SharedMongoContainer {
    @MockitoBean
    protected MyUserDetailsService userDetailsService;

    @MockitoBean
    protected SearchIndexInitializer searchIndexInitializer;
}
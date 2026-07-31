package com.boardwise.backend.user_service.models;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@Document(collection = "TOKEN_BLACKLIST")
@Getter
@Setter
@EqualsAndHashCode
public class TokenBlackList {

    @Id
    private String id;
    @Indexed(unique = true)
    private String jti;
    private Instant createdAt;

    @Indexed(expireAfter = "1s")
    private Instant expiresAt;

    public TokenBlackList(String jti, Instant expiresAt){
        this.jti = jti;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }
}

package com.boardwise.backend.user_service.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@Document(collection = "Token_Blacklist")
@Getter
@Setter
@EqualsAndHashCode
public class TokenBlackList {

    @Id
    private String id;
    @Indexed(unique = true)
    private String jti;
    private LocalDateTime createdAt;
    @Indexed(expireAfter = "1s")
    private LocalDateTime expiresAt;

    public TokenBlackList(String jti, LocalDateTime expiresAt){
        this.jti = jti;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }
}

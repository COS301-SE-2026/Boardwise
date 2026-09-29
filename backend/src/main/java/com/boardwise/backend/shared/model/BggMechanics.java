package com.boardwise.backend.shared.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data 
@Builder 
@AllArgsConstructor 
public class BggMechanics {
    private Integer bggId;
    private String name;
}

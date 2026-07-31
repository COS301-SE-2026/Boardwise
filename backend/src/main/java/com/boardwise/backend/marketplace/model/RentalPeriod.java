package com.boardwise.backend.marketplace.model;

import java.time.LocalDate;

import lombok.Data;

@Data
public class RentalPeriod {
    private LocalDate startDate;
    private LocalDate endDate;
}

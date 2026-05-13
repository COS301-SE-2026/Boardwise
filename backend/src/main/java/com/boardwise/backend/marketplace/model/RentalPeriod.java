package com.boardwise.backend.marketplace.model;

import java.time.LocalDate;

public class RentalPeriod {
    private LocalDate startDate;
    private LocalDate endDate;

    public void setStartDate(LocalDate x) {
        startDate = x;
    }

    public void setEndDate(LocalDate x) {
        endDate = x;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}

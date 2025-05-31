package com.projects.bills.Enums;

import lombok.Getter;

@Getter
public enum EntityType {
    PARTY("Entity"),
    ENTRY("Invoice"),
    PAYMENT("Payment");

    private final String value;

    EntityType(String value) {
        this.value = value;
    }

}
package com.projects.bills.Enums;

import lombok.Getter;

@Getter
public enum EntityType {
    PARTY("Party"),
    ENTRY("Entry"),
    PAYMENT("Payment");

    private final String value;

    EntityType(String value) {
        this.value = value;
    }

}
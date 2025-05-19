package com.projects.bills.Enums;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public enum FlowType {
    INCOMING("Income"),
    OUTGOING("Expense");

    private final String type;

    FlowType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static FlowType fromType(String type) {
        for (FlowType flowType : FlowType.values()) {
            if (flowType.getType().equalsIgnoreCase(type)) {
                return flowType;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid FlowType: " + type);
    }

    public static String fromName(String name) {
        for (FlowType flowType : FlowType.values()) {
            if (flowType.name().equalsIgnoreCase(name)) {
                return flowType.getType();
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid FlowType Name: " + name);
    }
}

package com.projects.bills.Enums;

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
        throw new IllegalArgumentException("Invalid FlowType: " + type);
    }
}
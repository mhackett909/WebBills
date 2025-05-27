package com.projects.bills.Enums;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

public class FlowTypeTest {
    @Test
    void testEnumValues() {
        assertEquals("Income", FlowType.INCOMING.getType());
        assertEquals("Expense", FlowType.OUTGOING.getType());
    }

    @Test
    void testFromTypeValid() {
        assertEquals(FlowType.INCOMING, FlowType.fromType("Income"));
        assertEquals(FlowType.OUTGOING, FlowType.fromType("Expense"));
        // Case-insensitive
        assertEquals(FlowType.INCOMING, FlowType.fromType("income"));
        assertEquals(FlowType.OUTGOING, FlowType.fromType("expense"));
    }

    @Test
    void testFromTypeInvalid() {
        assertThrows(ResponseStatusException.class, () -> FlowType.fromType("Invalid"));
        assertThrows(ResponseStatusException.class, () -> FlowType.fromType(""));
        assertThrows(ResponseStatusException.class, () -> FlowType.fromType(null));
    }

    @Test
    void testFromNameValid() {
        assertEquals("Income", FlowType.fromName("INCOMING"));
        assertEquals("Expense", FlowType.fromName("OUTGOING"));
        // Case-insensitive
        assertEquals("Income", FlowType.fromName("incoming"));
        assertEquals("Expense", FlowType.fromName("outgoing"));
    }

    @Test
    void testFromNameInvalid() {
        assertThrows(ResponseStatusException.class, () -> FlowType.fromName("Invalid"));
        assertThrows(ResponseStatusException.class, () -> FlowType.fromName(""));
        assertThrows(ResponseStatusException.class, () -> FlowType.fromName(null));
    }
}

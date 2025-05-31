package com.projects.bills.Enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EntityTypeTest {
    @Test
    void testEnumValues() {
        assertEquals("Entity", EntityType.PARTY.getValue());
        assertEquals("Invoice", EntityType.ENTRY.getValue());
        assertEquals("Payment", EntityType.PAYMENT.getValue());
    }
    
    @Test
    void testEnumValuesArray() {
        EntityType[] values = EntityType.values();
        assertArrayEquals(new EntityType[]{EntityType.PARTY, EntityType.ENTRY, EntityType.PAYMENT}, values);
    }
}

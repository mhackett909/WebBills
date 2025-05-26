package com.projects.bills.Enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EntityTypeTest {
    @Test
    void testEnumValues() {
        assertEquals("Party", EntityType.PARTY.getValue());
        assertEquals("Entry", EntityType.ENTRY.getValue());
        assertEquals("Payment", EntityType.PAYMENT.getValue());
    }

    @Test
    void testEnumNames() {
        assertEquals(EntityType.PARTY, EntityType.valueOf("PARTY"));
        assertEquals(EntityType.ENTRY, EntityType.valueOf("ENTRY"));
        assertEquals(EntityType.PAYMENT, EntityType.valueOf("PAYMENT"));
    }

    @Test
    void testEnumValuesArray() {
        EntityType[] values = EntityType.values();
        assertArrayEquals(new EntityType[]{EntityType.PARTY, EntityType.ENTRY, EntityType.PAYMENT}, values);
    }
}

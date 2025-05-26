package com.projects.bills.Enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UpdateTypeTest {
    @Test
    void testEnumValues() {
        assertEquals(UpdateType.EMAIL, UpdateType.valueOf("EMAIL"));
        assertEquals(UpdateType.PASSWORD, UpdateType.valueOf("PASSWORD"));
        assertEquals(UpdateType.RECYCLE, UpdateType.valueOf("RECYCLE"));
        assertEquals(UpdateType.NONE, UpdateType.valueOf("NONE"));
    }

    @Test
    void testEnumValuesArray() {
        UpdateType[] values = UpdateType.values();
        assertArrayEquals(new UpdateType[]{UpdateType.EMAIL, UpdateType.PASSWORD, UpdateType.RECYCLE, UpdateType.NONE}, values);
    }
}

package com.projects.bills.Enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LastActionTest {
    @Test
    void testFromDb() {
        assertEquals(LastAction.USER_RECYCLE, LastAction.fromDb("User_Recycle"));
        assertEquals(LastAction.SYSTEM_RECYCLE, LastAction.fromDb("System_Recycle"));
        assertEquals(LastAction.NONE, LastAction.fromDb("Other_Value"));
        assertEquals(LastAction.NONE, LastAction.fromDb(null));
    }

    @Test
    void testToDb() {
        assertEquals("User_Recycle", LastAction.USER_RECYCLE.toDb());
        assertEquals("System_Recycle", LastAction.SYSTEM_RECYCLE.toDb());
        assertNull(LastAction.NONE.toDb());
    }

    @Test
    void testEnumValues() {
        LastAction[] values = LastAction.values();
        assertArrayEquals(new LastAction[]{LastAction.USER_RECYCLE, LastAction.SYSTEM_RECYCLE, LastAction.NONE}, values);
    }
}

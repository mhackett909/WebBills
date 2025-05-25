package com.projects.bills.Mappers;

import com.projects.bills.DTOs.BillDTO;
import com.projects.bills.DTOs.BillDTOList;
import com.projects.bills.Entities.Bill;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BillMapperTest {
    private final BillMapper mapper;

    public BillMapperTest() {
        this.mapper = new BillMapper();
    }

    @Test
    void testMapToDTO() {
        // Arrange
        Long id = 1L;
        LocalDateTime recycleDate = LocalDateTime.now().minusDays(1);
        String billName = "Water";

        Bill bill = new Bill();
        bill.setBillId(id);
        bill.setName(billName);
        bill.setStatus(true);
        bill.setRecycleDate(recycleDate);

        // Act
        BillDTO dto = mapper.mapToDTO(bill);

        // Assert
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(billName, dto.getName());
        assertEquals(true, dto.getStatus());
        assertTrue(dto.getRecycle());
    }

    @Test
    void testBillDTOListConstructionAndAccess() {
        // Arrange
        long id = 1L;
        String billName = "Water";

        Bill bill = new Bill();
        bill.setBillId(id);
        bill.setName(billName);

        long id2 = 2L;
        String billName2 = "Water";

        Bill bill2 = new Bill();
        bill2.setBillId(id2);
        bill2.setName(billName2);

        // Act
        BillDTOList actual = mapper.mapToDTOList(List.of(bill, bill2));

        // Assert
        assertNotNull(actual.getBillDTOList());
        assertEquals(2, actual.getBillDTOList().size());
        assertEquals(billName, actual.getBillDTOList().get(0).getName());
        assertEquals(billName2, actual.getBillDTOList().get(1).getName());
    }
}
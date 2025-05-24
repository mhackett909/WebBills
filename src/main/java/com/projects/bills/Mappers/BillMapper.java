package com.projects.bills.Mappers;

import com.projects.bills.DTOs.BillDTO;
import com.projects.bills.DTOs.BillDTOList;
import com.projects.bills.Entities.Bill;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BillMapper {
    public BillDTOList mapToDTOList(List<Bill> bills) {
        List<BillDTO> billDtoList = bills.stream()
                .map(this::mapToDTO)
                .toList();

        return new BillDTOList(billDtoList);
    }

    public BillDTO mapToDTO(Bill bill) {
        if (bill == null) return null;
        BillDTO billDTO = new BillDTO();
        billDTO.setId(bill.getBillId());
        billDTO.setName(bill.getName());
        billDTO.setStatus(bill.getStatus());
        billDTO.setRecycle(bill.getRecycleDate() != null);
        return billDTO;
    }
}

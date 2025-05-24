package com.projects.bills.Mappers;

import com.projects.bills.DTOs.RecycleDTO;
import com.projects.bills.DTOs.RecycleDTOList;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.Payment;
import com.projects.bills.Enums.EntityType;
import com.projects.bills.Enums.FlowType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RecycleMapper {
    public RecycleDTOList buildRecycleDTOList(List<Bill> recycledBills, List<Entry> recycledEntries, List<Payment> recycledPayments) {
        List<RecycleDTO> combined = new ArrayList<>();
        if (recycledBills != null) {
            recycledBills.forEach(bill -> combined.add(mapBillToRecycleDTO(bill)));
        }
        if (recycledEntries != null) {
            recycledEntries.forEach(entry -> combined.add(mapEntryToRecycleDTO(entry)));
        }
        if (recycledPayments != null) {
            recycledPayments.forEach(payment -> combined.add(mapPaymentToRecycleDTO(payment)));
        }
        combined.sort((a, b) -> b.getRecycleDate().compareTo(a.getRecycleDate())); // Descending

        RecycleDTOList recycleDTOList = new RecycleDTOList();
        recycleDTOList.setRecycleItems(combined);
        return recycleDTOList;
    }

    private RecycleDTO mapPaymentToRecycleDTO(Payment payment) {
        Bill entryBill = payment.getEntry().getBill();

        RecycleDTO recycleDTO = new RecycleDTO();
        recycleDTO.setEntityId(payment.getPaymentId());
        recycleDTO.setEntityType(EntityType.PAYMENT.getValue());
        recycleDTO.setEntityDate(payment.getDate());
        recycleDTO.setRecycleDate(payment.getRecycleDate());
        recycleDTO.setPartyName(entryBill.getName());
        recycleDTO.setInvoiceNumber(entryBill.getBillId());
        recycleDTO.setAmount(payment.getAmount());
        recycleDTO.setType(payment.getType()+" / "+payment.getMedium());
        recycleDTO.setDetails(payment.getNotes());

        return recycleDTO;
    }

    private RecycleDTO mapEntryToRecycleDTO(Entry entry) {
        Bill entryBill = entry.getBill();

        RecycleDTO recycleDTO = new RecycleDTO();
        recycleDTO.setEntityId(entry.getId());
        recycleDTO.setEntityType(EntityType.ENTRY.getValue());
        recycleDTO.setEntityDate(entry.getDate());
        recycleDTO.setRecycleDate(entry.getRecycleDate());
        recycleDTO.setPartyName(entryBill.getName());
        recycleDTO.setInvoiceNumber(entry.getId());
        recycleDTO.setAmount(entry.getAmount());
        recycleDTO.setType(FlowType.fromName(entry.getFlow()));
        recycleDTO.setDetails(entry.getServices());

        return recycleDTO;
    }

    private RecycleDTO mapBillToRecycleDTO(Bill bill) {
        RecycleDTO recycleDTO = new RecycleDTO();
        recycleDTO.setEntityId(bill.getBillId());
        recycleDTO.setEntityType(EntityType.PARTY.getValue());
        recycleDTO.setRecycleDate(bill.getRecycleDate());
        recycleDTO.setPartyName(bill.getName());
        recycleDTO.setType(bill.getStatus() ? "Active" : "Archived");
        return recycleDTO;
    }
}

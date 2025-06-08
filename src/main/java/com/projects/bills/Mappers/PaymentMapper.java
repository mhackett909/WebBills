package com.projects.bills.Mappers;

import com.projects.bills.DTOs.PaymentDTO;
import com.projects.bills.DTOs.PaymentDTOList;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.Payment;
import com.projects.bills.Enums.LastAction;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class PaymentMapper {
    public PaymentDTO mapToPaymentDTO(Payment payment) {
        if (payment == null) {
            return null;
        }
        PaymentDTO dto = new PaymentDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setDate(payment.getDate() != null ? payment.getDate().toLocalDate() : null);
        dto.setAmount(payment.getAmount());
        dto.setType(payment.getType());
        dto.setMedium(payment.getMedium());
        dto.setAutopay(payment.getAutopay());
        dto.setNotes(payment.getNotes());
        dto.setRecycle(payment.getRecycleDate() != null);
        if (payment.getEntry() != null) {
            dto.setEntryId(payment.getEntry().getId());
        }
        return dto;
    }

    public Payment buildPaymentFromDTO(PaymentDTO paymentDTO, Payment payment, Entry entry) {
        payment.setDate(Date.valueOf(paymentDTO.getDate()));
        payment.setAmount(paymentDTO.getAmount());
        payment.setType(paymentDTO.getType());
        payment.setMedium(paymentDTO.getMedium());
        payment.setAutopay(paymentDTO.getAutopay());
        payment.setNotes(paymentDTO.getNotes());
        payment.setEntry(entry);
        payment.setRecycleDate(paymentDTO.getRecycle() ? LocalDateTime.now() : null);
        payment.setLastAction(paymentDTO.getRecycle() ? LastAction.USER_RECYCLE.toDb() : LastAction.NONE.toDb());
        return payment;
    }

    public PaymentDTOList mapToDtoList(List<Payment> payments) {
        ArrayList<PaymentDTO> paymentList = new ArrayList<>();
        for (Payment payment : payments) {
            PaymentDTO paymentDTO = mapToPaymentDTO(payment);
            paymentList.add(paymentDTO);
        }
        PaymentDTOList paymentDTOList = new PaymentDTOList();
        paymentDTOList.setPaymentDTOList(paymentList);
        return paymentDTOList;
    }
}

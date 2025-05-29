package com.projects.bills.Mappers;

import com.projects.bills.DTOs.BalanceDTO;
import com.projects.bills.DTOs.EntryDTO;
import com.projects.bills.DTOs.EntryDTOList;
import com.projects.bills.DataHelpers.EntryFilters;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.User;
import com.projects.bills.Enums.FlowType;
import com.projects.bills.Enums.LastAction;
import com.projects.bills.Constants.Strings;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class EntryMapper {
    public EntryDTO mapToDTO(Entry entry, boolean isArchived) {
        return new EntryDTO(
                entry.getId(),
                entry.getBill().getBillId(),
                entry.getInvoiceId(), // Local invoice ID
                entry.getBill().getName(), // Not using entry.getBill().getName() because it is legacy
                entry.getDate() != null ? entry.getDate().toLocalDate() : null,
                entry.getAmount(),
                mapBalance(entry.getBalance()),
                entry.getStatus(),
                entry.getRecycleDate() != null,
                entry.getServices(),
                entry.getFlow(),
                isArchived,
                entry.getOverpaid()
        );
    }

    public Entry mapToEntity(EntryDTO entryDTO, Entry entry, Bill bill, FlowType flowType, User user, Long invoiceId) {
        entry.setId(entryDTO.getEntryId());
        entry.setBill(bill);
        entry.setUser(user);
        entry.setDate(Date.valueOf(entryDTO.getDate()));
        entry.setAmount(entryDTO.getAmount());
        entry.setStatus(entryDTO.getStatus());
        entry.setServices(entryDTO.getServices());
        entry.setFlow(flowType.toString());
        entry.setOverpaid(entryDTO.getOverpaid());
        if (entry.getOverpaid() == null) {
            entry.setOverpaid(false);
        }
        entry.setInvoiceId(invoiceId);
        entry.setRecycleDate(entryDTO.getRecycle() ? LocalDateTime.now() : null);
        entry.setLastAction(entryDTO.getRecycle() ? LastAction.USER_RECYCLE.toDb() : LastAction.NONE.toDb());
        return entry;
    }

    public EntryDTOList mapEntriesToDTOList(ArrayList<EntryDTO> entryList, Long total) {
        EntryDTOList entryDtoList = new EntryDTOList();
        entryDtoList.setEntries(entryList);
        entryDtoList.setTotal(total);

        return entryDtoList;
    }

    public EntryFilters mapToEntryFilters(String userName,
                                          LocalDate startDate,
                                          LocalDate endDate,
                                          Long invoiceNum,
                                          List<String> partyList,
                                          BigDecimal min,
                                          BigDecimal max,
                                          String flow,
                                          String paid,
                                          String archives) {
        String flowType = null;
        if (flow != null && !flow.isEmpty()) {
            flowType = FlowType.fromType(flow).toString();
        }

        Boolean isPaid = null;
        Boolean isOverpaid = null;
        Boolean isPartial = null;
        if (paid != null) {
            if (paid.equalsIgnoreCase(Strings.PAID_TRUE)) {
                isPaid = true;
            } else if (paid.equalsIgnoreCase(Strings.PAID_FALSE)) {
                isPaid = false;
            } else if (paid.equalsIgnoreCase(Strings.PAID_OVERPAID)) {
                isOverpaid = true;
            }
            else if (paid.equalsIgnoreCase(Strings.PAID_PARTIAL)) {
                // Unpaid entries with balance < entry amount
                isPaid = false;
                isPartial = true;
            }
        }

        Boolean isArchived = null;
        if (archives != null) {
            if (archives.equalsIgnoreCase(Strings.ARCHIVES_TRUE)) {
                isArchived = true;
            } else if (archives.equalsIgnoreCase(Strings.ARCHIVES_FALSE)) {
                isArchived = false;
            }
        }

        EntryFilters filters = new EntryFilters();
        filters.setUserName(userName);
        filters.setStartDate(startDate);
        filters.setEndDate(endDate);
        filters.setInvoiceNum(invoiceNum);
        filters.setPartyList(partyList);
        filters.setMin(min);
        filters.setMax(max);
        filters.setFlow(flowType);
        filters.setPaid(isPaid);
        filters.setOverpaid(isOverpaid);
        filters.setArchived(isArchived);
        filters.setPartial(isPartial);
        return filters;
    }

    private BalanceDTO mapBalance(BigDecimal balance) {
        if (balance == null) {
            return new BalanceDTO();
        }
        // BalanceDTO already has default values for totalBalance and totalOverpaid
        BalanceDTO balanceDTO = new BalanceDTO();
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            balanceDTO.setTotalBalance(balance);
        } else if (balance.compareTo(BigDecimal.ZERO) < 0) {
            balanceDTO.setTotalOverpaid(balance.abs());
        }
        return balanceDTO;
    }

    public String mapSortField(String sortField) {
        return switch (sortField) {
            case "paid" -> "status";
            case "name" -> "bill.name";
            case "entryId" -> "id";
            case "description" -> "services";
            case "archived" -> "bill.status";
            default -> sortField;
        };
    }
}


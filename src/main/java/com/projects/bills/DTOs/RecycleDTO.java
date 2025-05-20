package com.projects.bills.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecycleDTO {
    private Long entityId;
    private String entityType;
    private Date entityDate;
    private LocalDateTime recycleDate;
    private String partyName;
    private Long invoiceNumber;
    private BigDecimal amount;
    private String type;
    private String details;
}

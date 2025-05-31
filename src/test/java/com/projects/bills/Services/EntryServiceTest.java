package com.projects.bills.Services;

import com.projects.bills.Constants.Exceptions;
import com.projects.bills.DTOs.EntryDTO;
import com.projects.bills.DTOs.EntryDTOList;
import com.projects.bills.DataHelpers.EntryFilters;
import com.projects.bills.DataHelpers.StatsHelper;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.User;
import com.projects.bills.Mappers.EntryMapper;
import com.projects.bills.Repositories.EntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EntryServiceTest {

    private EntryRepository entryRepository;
    private BillService billService;
    private UserService userService;
    private StatsHelper statsHelper;
    private EntryMapper entryMapper;
    private EntryService entryService;

    @BeforeEach
    void setUp() {
        entryRepository = mock(EntryRepository.class);
        billService = mock(BillService.class);
        userService = mock(UserService.class);
        statsHelper = mock(StatsHelper.class);
        entryMapper = mock(EntryMapper.class);
        entryService = new EntryService(entryRepository, billService, userService, statsHelper, entryMapper);
    }

    @Test
    void testGetEntries_UserFound_ReturnsDTOList() {
        String userName = "alice";
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        Long invoiceNum = 123L;
        List<String> partyList = Arrays.asList("A", "B");
        BigDecimal min = new BigDecimal("10.00");
        BigDecimal max = new BigDecimal("100.00");
        String flow = "IN";
        String paid = "true";
        String archives = "false";
        String sortField = "date";
        String sortOrder = "asc";

        User user = new User();
        user.setUsername(userName);

        EntryFilters filters = new EntryFilters();
        Page<Entry> entryPage = new PageImpl<>(List.of());
        EntryDTOList dtoList = new EntryDTOList(List.of(), 0L);

        when(userService.findByUsername(userName)).thenReturn(Optional.of(user));
        when(entryMapper.mapToEntryFilters(userName, startDate, endDate, invoiceNum, partyList, min, max, flow, paid, archives)).thenReturn(filters);
        when(entryMapper.mapSortField(any())).thenReturn(sortField);
        when(statsHelper.getFilteredPredicate(any(), eq(filters), any())).thenReturn(null);
        when(entryRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(entryPage);
        when(entryMapper.mapEntriesToDTOList(any(), anyLong())).thenReturn(dtoList);

        EntryDTOList result = entryService.getEntries(userName, startDate, endDate, invoiceNum, partyList, min, max, flow, paid, archives, null, null, sortField, sortOrder);

        assertEquals(dtoList, result);
    }

    @Test
    void testGetEntries_UserNotFound_Throws() {
        String userName = "bob";
        when(userService.findByUsername(userName)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                entryService.getEntries(userName, null, null, null, null, null, null, null, null, null, null, null, null, null));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testGetEntryDtoById_FoundAndAuthorized() {
        Long entryId = 1L;
        String userName = "alice";
        String filter = "any";
        User user = new User();
        user.setUsername(userName);
        Bill bill = new Bill();
        bill.setUser(user);
        bill.setStatus(true);
        Entry entry = new Entry();
        entry.setBill(bill);

        EntryDTO dto = new EntryDTO();

        when(entryRepository.findByIdAndRecycleDateIsNull(entryId)).thenReturn(entry);
        when(billService.getBillEntityById(any())).thenReturn(bill);
        when(entryMapper.mapToDTO(entry, false)).thenReturn(dto);

        Optional<EntryDTO> result = entryService.getEntryDtoById(entryId, filter, userName);

        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
    }

    @Test
    void testGetEntryDtoById_NotAuthorized_Throws() {
        Long entryId = 2L;
        String userName = "alice";
        String filter = "any";
        Entry entry = new Entry();
        Bill bill = new Bill();
        User user = new User();
        user.setUsername("bob");
        bill.setUser(user);
        entry.setBill(bill);

        when(entryRepository.findByIdAndRecycleDateIsNull(entryId)).thenReturn(entry);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                entryService.getEntryDtoById(entryId, filter, userName));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void testSaveEntry_NewEntry() {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setEntryId(0L);
        entryDTO.setBillId(10L);
        entryDTO.setFlow("Income");
        entryDTO.setInvoiceId(0L);

        String userName = "alice";
        boolean existing = false;
        String filter = "any";

        Bill bill = new Bill();
        User user = new User();
        user.setUsername(userName);
        bill.setUser(user);
        bill.setBillId(10L);
        bill.setStatus(true);

        Entry mappedEntry = new Entry();
        Entry savedEntry = new Entry();
        savedEntry.setBill(bill);
        EntryDTO mappedDTO = new EntryDTO();

        when(billService.getBillEntityById(10L)).thenReturn(bill);
        when(entryRepository.findNextInvoiceIdForUser(user)).thenReturn(5L);
        when(entryMapper.mapToEntity(any(), any(), any(), any(), any(), anyLong())).thenReturn(mappedEntry);
        when(entryRepository.save(mappedEntry)).thenReturn(savedEntry);
        when(billService.getBillEntityById(10L)).thenReturn(bill);
        when(entryMapper.mapToDTO(savedEntry, false)).thenReturn(mappedDTO);

        EntryDTO result = entryService.saveEntry(entryDTO, existing, filter, userName);

        assertEquals(mappedDTO, result);
    }

    @Test
    void testSaveEntry_ExistingEntry() {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setEntryId(1L);
        entryDTO.setBillId(10L);
        entryDTO.setFlow("Income");
        entryDTO.setInvoiceId(2L);
        entryDTO.setStatus(false);

        String userName = "alice";
        boolean existing = true;
        String filter = "any";

        Bill bill = new Bill();
        User user = new User();
        user.setUsername(userName);
        bill.setUser(user);
        bill.setBillId(10L);
        bill.setStatus(true); // Ensure the bill is not archived

        Entry entry = new Entry();
        entry.setBill(bill);

        Entry mappedEntry = new Entry();
        Entry savedEntry = new Entry();
        savedEntry.setBill(bill);
        EntryDTO mappedDTO = new EntryDTO();

        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(billService.getBillEntityById(10L)).thenReturn(bill);
        when(entryMapper.mapToEntity(any(), any(), any(), any(), any(), anyLong())).thenReturn(mappedEntry);
        when(entryRepository.save(mappedEntry)).thenReturn(savedEntry);
        when(billService.getBillEntityById(10L)).thenReturn(bill);
        when(entryMapper.mapToDTO(savedEntry, false)).thenReturn(mappedDTO);

        EntryDTO result = entryService.saveEntry(entryDTO, existing, filter, userName);

        assertEquals(mappedDTO, result);
    }

    @Test
    void testSaveEntry_ExistingEntryNotFound_Throws() {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setEntryId(1L);
        entryDTO.setBillId(10L);

        String userName = "alice";
        boolean existing = true;
        String filter = "any";

        when(entryRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                entryService.saveEntry(entryDTO, existing, filter, userName));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testSaveEntry_BillNotFound_Throws() {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setEntryId(0L);
        entryDTO.setBillId(10L);

        String userName = "alice";
        boolean existing = false;
        String filter = "any";

        when(billService.getBillEntityById(10L)).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                entryService.saveEntry(entryDTO, existing, filter, userName));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void testSaveEntry_ExistingEntry_RecycledAndNoBypass_Throws() {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setEntryId(1L);
        entryDTO.setBillId(10L);

        String userName = "alice";
        boolean existing = true;
        String filter = "notbypass";

        Bill bill = new Bill();
        User user = new User();
        user.setUsername(userName);
        bill.setUser(user);
        bill.setStatus(true);

        Entry entry = new Entry();
        entry.setBill(bill);
        entry.setRecycleDate(LocalDateTime.now());

        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                entryService.saveEntry(entryDTO, existing, filter, userName));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void testSaveEntry_ExistingEntry_BillArchived_Throws() {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setEntryId(1L);
        entryDTO.setBillId(10L);

        String userName = "alice";
        boolean existing = true;
        String filter = "any";

        Bill bill = new Bill();
        User user = new User();
        user.setUsername(userName);
        bill.setUser(user);
        bill.setStatus(false); // archived

        Entry entry = new Entry();
        entry.setBill(bill);

        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                entryService.saveEntry(entryDTO, existing, filter, userName));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void testSaveEntry_BillUserNotAuthorized_Throws() {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setEntryId(0L);
        entryDTO.setBillId(10L);

        String userName = "alice";
        boolean existing = false;
        String filter = "any";

        Bill bill = new Bill();
        User user = new User();
        user.setUsername("bob"); // not alice
        bill.setUser(user);

        when(billService.getBillEntityById(10L)).thenReturn(bill);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                entryService.saveEntry(entryDTO, existing, filter, userName));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void testSaveEntry_ExistingEntry_UserNotAuthorized_Throws() {
        EntryDTO entryDTO = new EntryDTO();
        entryDTO.setEntryId(1L);
        entryDTO.setBillId(10L);

        String userName = "alice";
        boolean existing = true;
        String filter = "any";

        Bill bill = new Bill();
        User user = new User();
        user.setUsername("bob"); // Not the same as userName
        bill.setUser(user);

        Entry entry = new Entry();
        entry.setBill(bill);

        when(entryRepository.findById(1L)).thenReturn(Optional.of(entry));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                entryService.saveEntry(entryDTO, existing, filter, userName));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals(Exceptions.NOT_AUTHORIZED_TO_ACCESS_ENTRY, ex.getReason());
    }
}


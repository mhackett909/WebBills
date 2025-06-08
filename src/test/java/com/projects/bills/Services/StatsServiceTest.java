package com.projects.bills.Services;

import com.projects.bills.Constants.Exceptions;
import com.projects.bills.DTOs.StatsDTO;
import com.projects.bills.DataHelpers.EntryFilters;
import com.projects.bills.DataHelpers.StatsHelper;
import com.projects.bills.Entities.Entry;
import com.projects.bills.Entities.User;
import com.projects.bills.Mappers.EntryMapper;
import com.projects.bills.Mappers.StatsMapper;
import com.projects.bills.Repositories.EntryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatsServiceTest {

    private UserService userService;
    private EntryRepository entryRepository;
    private StatsMapper statsMapper;
    private EntryMapper entryMapper;
    private EntityManager entityManager;
    private StatsService statsService;
    private CriteriaBuilder cb;
    private CriteriaQuery cq;
    private TypedQuery query;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        entryRepository = mock(EntryRepository.class);
        statsMapper = mock(StatsMapper.class);
        entryMapper = mock(EntryMapper.class);
        entityManager = mock(EntityManager.class);
        cb = mock(CriteriaBuilder.class);
        cq = mock(CriteriaQuery.class);
        query = mock(TypedQuery.class);
        when(entityManager.createQuery(any(CriteriaQuery.class))).thenReturn(query);
        when(entityManager.getCriteriaBuilder()).thenReturn(cb);
        when(query.getResultList()).thenReturn(Collections.emptyList());
        when(query.setMaxResults(anyInt())).thenReturn(query);

        StatsHelper statsHelper = mock(StatsHelper.class);

        statsService = new StatsService(userService, entryRepository, statsHelper, statsMapper, entryMapper, entityManager);
    }

    @Test
    void testGetStats_UserNotFound_Throws() {
        when(entryMapper.mapToEntryFilters(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new EntryFilters() {{
                    setInvoiceNum(1L);
                }});
        when(userService.findByUsername(anyString())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                statsService.getStats("alice", null, null, 1L, null, null, null, null, null, null));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals(String.format(Exceptions.USER_NOT_FOUND, "alice"), ex.getReason());
    }

    @Test
    void testGetStats_EntryNotFound_Throws() {
        EntryFilters filters = new EntryFilters();
        filters.setInvoiceNum(2L);
        when(entryMapper.mapToEntryFilters(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(filters);
        User user = new User();
        user.setUsername("alice");
        when(userService.findByUsername("alice")).thenReturn(Optional.of(user));
        when(entryRepository.findByInvoiceIdAndUserAndRecycleDateIsNull(anyLong(), any())).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                statsService.getStats("alice", null, null, 2L, null, null, null, null, null, null));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals(String.format(Exceptions.ENTRY_NOT_FOUND, 2L), ex.getReason());
    }

    @Test
    void testGetStats_Success() {
        User user = new User();
        user.setUsername("alice");

        Entry entry = new Entry();
        entry.setUser(user);

        EntryFilters filters = new EntryFilters();
        filters.setInvoiceNum(2L);

        when(entryMapper.mapToEntryFilters(any(), any(), any(), anyLong(), any(), any(), any(), any(), any(), any()))
                .thenReturn(filters);
        when(userService.findByUsername(any())).thenReturn(Optional.of(user));
        when(entryRepository.findByInvoiceIdAndUserAndRecycleDateIsNull(anyLong(), any())).thenReturn(entry);

        StatsHelper statsHelper = mock(StatsHelper.class);
        when(statsHelper.getTotalEntryAmountsByFlow(cb, filters)).thenReturn(cq);
        when(statsHelper.getMaxAvgSumQuery(cb, filters)).thenReturn(cq);
        when(statsHelper.getOverpaidEntryTotals(cb, filters)).thenReturn(cq);
        when(statsHelper.getOverpaidPaymentTotals(cb, filters)).thenReturn(cq);
        when(statsHelper.getTop5Parties(cb, filters)).thenReturn(cq);
        when(statsHelper.getTop5TypeMediumCombos(cb, filters)).thenReturn(cq);

        // Re-instantiate statsService with the new statsHelper mock
        statsService = new StatsService(userService, entryRepository, statsHelper, statsMapper, entryMapper, entityManager);

        StatsDTO statsDTO = new StatsDTO();
        when(statsMapper.buildStatsDTO(any())).thenReturn(statsDTO);

        StatsDTO result = statsService.getStats("alice", null, null, 2L, null, null, null, null, null, null);
        assertNotNull(result);
        assertSame(statsDTO, result);
    }
}


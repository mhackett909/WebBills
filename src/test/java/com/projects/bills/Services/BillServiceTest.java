package com.projects.bills.Services;

import com.projects.bills.Constants.Exceptions;
import com.projects.bills.DTOs.BillDTO;
import com.projects.bills.DTOs.BillDTOList;
import com.projects.bills.Entities.Bill;
import com.projects.bills.Entities.User;
import com.projects.bills.Mappers.BillMapper;
import com.projects.bills.Repositories.BillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillServiceTest {
    private BillRepository billRepository;
    private UserService userService;
    private BillMapper billMapper;
    private BillService billService;

    @BeforeEach
    void setUp() {
        billRepository = mock(BillRepository.class);
        userService = mock(UserService.class);
        billMapper = mock(BillMapper.class);
        billService = new BillService(billRepository, userService, billMapper);
    }

    @Test
    void testGetBillEntityById_Found() {
        Long billId = 1L;
        Bill bill = new Bill();
        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));

        Bill result = billService.getBillEntityById(billId);

        assertEquals(bill, result);
    }

    @Test
    void testGetBillEntityById_NotFound() {
        Long billId = 2L;
        when(billRepository.findById(billId)).thenReturn(Optional.empty());

        Bill result = billService.getBillEntityById(billId);

        assertNull(result);
    }

    @Test
    void testGetBillDtoList_ActiveFilter() {
        String userName = "alice";
        String filter = "active";
        User user = new User();
        user.setUsername(userName);
        List<Bill> bills = List.of(new Bill());
        BillDTOList dtoList = new BillDTOList(List.of());

        when(userService.findByUsername(userName)).thenReturn(Optional.of(user));
        when(billRepository.findAllByStatusAndUserAndRecycleDateIsNullOrderByNameAsc(true, user)).thenReturn(bills);
        when(billMapper.mapToDTOList(bills)).thenReturn(dtoList);

        BillDTOList result = billService.getBillDtoList(filter, userName);

        assertEquals(dtoList, result);
    }

    @Test
    void testGetBillDtoList_InactiveFilter() {
        String userName = "alice";
        String filter = "inactive";
        User user = new User();
        user.setUsername(userName);
        List<Bill> bills = List.of(new Bill());
        BillDTOList dtoList = new BillDTOList(List.of());

        when(userService.findByUsername(userName)).thenReturn(Optional.of(user));
        when(billRepository.findAllByStatusAndUserAndRecycleDateIsNullOrderByNameAsc(false, user)).thenReturn(bills);
        when(billMapper.mapToDTOList(bills)).thenReturn(dtoList);

        BillDTOList result = billService.getBillDtoList(filter, userName);

        assertEquals(dtoList, result);
    }

    @Test
    void testGetBillDtoList_DefaultFilter() {
        String userName = "alice";
        String filter = "other";
        User user = new User();
        user.setUsername(userName);
        List<Bill> bills = List.of(new Bill());
        BillDTOList dtoList = new BillDTOList(List.of());

        when(userService.findByUsername(userName)).thenReturn(Optional.of(user));
        when(billRepository.findAllByUserAndRecycleDateIsNullOrderByNameAsc(user)).thenReturn(bills);
        when(billMapper.mapToDTOList(bills)).thenReturn(dtoList);

        BillDTOList result = billService.getBillDtoList(filter, userName);

        assertEquals(dtoList, result);
    }

    @Test
    void testGetBillDtoList_UserNotFound_Throws() {
        String userName = "bob";
        when(userService.findByUsername(userName)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> billService.getBillDtoList("active", userName));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals(String.format(Exceptions.USER_NOT_FOUND, userName), ex.getReason());
    }

    @Test
    void testGetBill_FoundAndAuthorized() {
        Long billId = 1L;
        String userName = "alice";
        String filter = "any";
        Bill bill = new Bill();
        User user = new User();
        user.setUsername(userName);
        bill.setUser(user);
        BillDTO billDTO = new BillDTO();

        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));
        when(billMapper.mapToDTO(bill)).thenReturn(billDTO);

        BillDTO result = billService.getBill(billId, filter, userName);

        assertEquals(billDTO, result);
    }

    @Test
    void testGetBill_NotFound_Throws() {
        Long billId = 2L;
        when(billRepository.findById(billId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> billService.getBill(billId, "any", "alice"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testGetBill_NotAuthorized_Throws() {
        Long billId = 3L;
        String userName = "alice";
        Bill bill = new Bill();
        User user = new User();
        user.setUsername("bob");
        bill.setUser(user);

        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> billService.getBill(billId, "any", userName));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void testGetBill_RecycledAndNoBypass_Throws() {
        Long billId = 4L;
        String userName = "alice";
        String filter = "notbypass";
        Bill bill = new Bill();
        User user = new User();
        user.setUsername(userName);
        bill.setUser(user);
        bill.setRecycleDate(LocalDateTime.now());

        when(billRepository.findById(billId)).thenReturn(Optional.of(bill));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> billService.getBill(billId, filter, userName));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void testSaveBill_NewBill() {
        String userName = "alice";
        boolean existing = false;
        Long billId = 10L;
        String billName = "Test Bill";
        Boolean billStatus = true;
        Boolean recycle = false;

        BillDTO billDTO = new BillDTO();
        billDTO.setId(billId);
        billDTO.setName(billName);
        billDTO.setStatus(billStatus);
        billDTO.setRecycle(recycle);

        User user = new User();
        user.setUsername(userName);

        Bill savedBill = new Bill();
        savedBill.setBillId(billId);
        savedBill.setName(billName);
        savedBill.setStatus(billStatus);
        savedBill.setUser(user);

        BillDTO mappedDTO = new BillDTO();
        mappedDTO.setId(billId);

        when(userService.findByUsername(userName)).thenReturn(Optional.of(user));
        when(billRepository.save(any(Bill.class))).thenReturn(savedBill);
        when(billMapper.mapToDTO(savedBill)).thenReturn(mappedDTO);

        BillDTO result = billService.saveBill(billDTO, existing, userName);

        assertEquals(mappedDTO, result);
    }

    @Test
    void testSaveBill_ExistingBill() {
        String userName = "alice";
        boolean existing = true;
        Long billId = 20L;
        String billName = "Existing Bill";
        Boolean billStatus = false;
        Boolean recycle = true;

        BillDTO billDTO = new BillDTO();
        billDTO.setId(billId);
        billDTO.setName(billName);
        billDTO.setStatus(billStatus);
        billDTO.setRecycle(recycle);

        User user = new User();
        user.setUsername(userName);

        Bill existingBill = new Bill();
        existingBill.setBillId(billId);
        existingBill.setUser(user);

        Bill savedBill = new Bill();
        savedBill.setBillId(billId);
        savedBill.setName(billName);
        savedBill.setStatus(billStatus);
        savedBill.setUser(user);

        BillDTO mappedDTO = new BillDTO();
        mappedDTO.setId(billId);

        when(userService.findByUsername(userName)).thenReturn(Optional.of(user));
        when(billRepository.findById(billId)).thenReturn(Optional.of(existingBill));
        when(billRepository.save(any(Bill.class))).thenReturn(savedBill);
        when(billMapper.mapToDTO(savedBill)).thenReturn(mappedDTO);

        BillDTO result = billService.saveBill(billDTO, existing, userName);

        assertEquals(mappedDTO, result);
    }

    @Test
    void testSaveBill_UserNotFound_Throws() {
        String userName = "bob";
        BillDTO billDTO = new BillDTO();

        when(userService.findByUsername(userName)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> billService.saveBill(billDTO, false, userName));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testSaveBill_ExistingBillNotFound_Throws() {
        String userName = "alice";
        boolean existing = true;
        Long billId = 30L;
        BillDTO billDTO = new BillDTO();
        billDTO.setId(billId);

        User user = new User();
        user.setUsername(userName);

        when(userService.findByUsername(userName)).thenReturn(Optional.of(user));
        when(billRepository.findById(billId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> billService.saveBill(billDTO, existing, userName));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testSaveBill_ExistingBillUserNotAuthorized_Throws() {
        String userName = "alice";
        boolean existing = true;
        Long billId = 40L;
        BillDTO billDTO = new BillDTO();
        billDTO.setId(billId);

        User user = new User();
        user.setUsername(userName);

        User otherUser = new User();
        otherUser.setUsername("bob");

        Bill existingBill = new Bill();
        existingBill.setBillId(billId);
        existingBill.setUser(otherUser);

        when(userService.findByUsername(userName)).thenReturn(Optional.of(user));
        when(billRepository.findById(billId)).thenReturn(Optional.of(existingBill));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> billService.saveBill(billDTO, existing, userName));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals(Exceptions.NOT_AUTHORIZED_TO_ACCESS_BILL, ex.getReason());
    }
}


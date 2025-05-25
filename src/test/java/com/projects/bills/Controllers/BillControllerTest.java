package com.projects.bills.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.bills.DTOs.BillDTO;
import com.projects.bills.DTOs.BillDTOList;
import com.projects.bills.Services.BillService;
import com.projects.bills.Services.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BillController.class)
@AutoConfigureMockMvc(addFilters = false) // disables JWT filters
class BillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private BillService billService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void getBills_success() throws Exception {
        BillDTOList dtoList = new BillDTOList();
        Mockito.when(billService.getBillDtoList(any(), eq("alice"))).thenReturn(dtoList);

        mockMvc.perform(get("/api/v1/bills"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void getBillsById_success() throws Exception {
        BillDTO billDTO = new BillDTO();
        billDTO.setId(1L);
        Mockito.when(billService.getBill(eq(1L), any(), eq("alice"))).thenReturn(billDTO);

        mockMvc.perform(get("/api/v1/bills/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void getBillsById_notFound() throws Exception {
        Mockito.when(billService.getBill(eq(1L), any(), eq("alice"))).thenReturn(null);

        mockMvc.perform(get("/api/v1/bills/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void newBill_success() throws Exception {
        BillDTO billDTO = new BillDTO();
        billDTO.setName("Test Bill");
        billDTO.setStatus(true);

        Mockito.when(billService.saveBill(any(BillDTO.class), eq(false), eq("alice"))).thenReturn(billDTO);

        mockMvc.perform(post("/api/v1/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billDTO)))
                .andExpect(status().isCreated());
    }


    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void editBill_success() throws Exception {
        BillDTO billDTO = new BillDTO();
        billDTO.setId(1L);
        billDTO.setName("Edit Bill");
        billDTO.setStatus(true);
        billDTO.setRecycle(false);

        Mockito.when(billService.saveBill(any(BillDTO.class), eq(true), eq("alice"))).thenReturn(billDTO);

        mockMvc.perform(put("/api/v1/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void getBillsById_notFound_returns404() throws Exception {
        Mockito.when(billService.getBill(eq(99L), any(), eq("alice")))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill does not exist by id: 99"));

        mockMvc.perform(get("/api/v1/bills/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void getBillsById_forbidden_returns403() throws Exception {
        Mockito.when(billService.getBill(eq(1L), any(), eq("alice")))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "User not authorized"));

        mockMvc.perform(get("/api/v1/bills/1"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidBillDataNew")
    @WithMockUser(username = "alice", roles = "USER")
    void newBill_invalidField_returns400(BillDTO billDTO) throws Exception {
        mockMvc.perform(post("/api/v1/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billDTO)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> provideInvalidBillDataNew() {
        return Stream.of(
                Arguments.of(new BillDTO(0, "New Bill", null, false)), // Missing Status
                Arguments.of(new BillDTO(0, null, true, false)) // Missing Name
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidBillDataEdit")
    @WithMockUser(username = "alice", roles = "USER")
    void editBill_invalidField_returns400(BillDTO billDTO) throws Exception {
        mockMvc.perform(put("/api/v1/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billDTO)))
                .andExpect(status().isBadRequest());
    }
    private static Stream<Arguments> provideInvalidBillDataEdit() {
        return Stream.of(
                Arguments.of(new BillDTO(0, "Edit Bill", true, false)), // Missing ID
                Arguments.of(new BillDTO(1L, null, true, false)),          // Missing Name
                Arguments.of(new BillDTO(1L, "Edit Bill", null, false))   // Missing Status
        );
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void editBill_notFound_returns404() throws Exception {
        BillDTO billDTO = new BillDTO();
        billDTO.setId(99L);
        billDTO.setName("Edit Bill");
        billDTO.setStatus(true);
        billDTO.setRecycle(false);

        Mockito.when(billService.saveBill(any(BillDTO.class), eq(true), eq("alice")))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found with id: 99"));

        mockMvc.perform(put("/api/v1/bills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(billDTO)))
                .andExpect(status().isNotFound());
    }
}

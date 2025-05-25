package com.projects.bills.Controllers;

import com.projects.bills.DTOs.RecycleDTOList;
import com.projects.bills.Services.JwtService;
import com.projects.bills.Services.RecycleService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecycleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecycleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecycleService recycleService;

    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser(username = "alice")
    void getRecycleBin_success() throws Exception {
        RecycleDTOList recycleDTOList = new RecycleDTOList();
        Mockito.when(recycleService.getRecycleBin("alice")).thenReturn(recycleDTOList);

        mockMvc.perform(get("/api/v1/recycle"))
                .andExpect(status().isOk());
    }
}
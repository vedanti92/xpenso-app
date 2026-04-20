package com.example.xpenso.controller;

import com.example.xpenso.dto.ExpenseDTO;
import com.example.xpenso.security.JwtRequestFilter;
import com.example.xpenso.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false) // ✅ disables JWT filter for controller tests
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean  // ✅ use @MockitoBean not @MockBean (available from Spring Boot 3.4+)
    private ExpenseService expenseService;

    @MockitoBean
    private JwtRequestFilter jwtRequestFilter;

    // ✅ ObjectMapper created manually — not @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ─── POST /expenses ────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void addExpense_shouldReturn201_withSavedExpense() throws Exception {
        ExpenseDTO input = ExpenseDTO.builder()
                .name("Lunch")
                .icon("🍱")
                .amount(new BigDecimal("250.00"))
                .date(LocalDate.now())
                .categoryId(10L)
                .build();

        ExpenseDTO saved = ExpenseDTO.builder()
                .id(100L)
                .name("Lunch")
                .icon("🍱")
                .amount(new BigDecimal("250.00"))
                .date(LocalDate.now())
                .categoryId(10L)
                .categoryName("Food")
                .build();

        when(expenseService.addExpense(any(ExpenseDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/expenses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.name").value("Lunch"))
                .andExpect(jsonPath("$.categoryName").value("Food"));

        verify(expenseService, times(1)).addExpense(any(ExpenseDTO.class));
    }

    // ─── GET /expenses ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser
    void getExpenses_shouldReturn200_withList() throws Exception {
        ExpenseDTO dto = ExpenseDTO.builder()
                .id(100L)
                .name("Lunch")
                .amount(new BigDecimal("250.00"))
                .categoryName("Food")
                .build();

        when(expenseService.getCurrentMonthExpensesForCurrentUser()).thenReturn(List.of(dto));

        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Lunch"))
                .andExpect(jsonPath("$[0].categoryName").value("Food"));
    }

    @Test
    @WithMockUser
    void getExpenses_shouldReturn200_withEmptyList() throws Exception {
        when(expenseService.getCurrentMonthExpensesForCurrentUser()).thenReturn(List.of());

        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ─── DELETE /expenses/{id} ─────────────────────────────────────────────────

    @Test
    @WithMockUser
    void deleteExpense_shouldReturn204() throws Exception {
        doNothing().when(expenseService).deleteExpense(100L);

        mockMvc.perform(delete("/expenses/100").with(csrf()))
                .andExpect(status().isNoContent());

        verify(expenseService, times(1)).deleteExpense(100L);
    }
}
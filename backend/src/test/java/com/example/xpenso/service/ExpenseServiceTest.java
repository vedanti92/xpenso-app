package com.example.xpenso.service;

import com.example.xpenso.dto.ExpenseDTO;
import com.example.xpenso.entity.CategoryEntity;
import com.example.xpenso.entity.ExpenseEntity;
import com.example.xpenso.entity.ProfileEntity;
import com.example.xpenso.repository.CategoryRepository;
import com.example.xpenso.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private ExpenseService expenseService;

    private ProfileEntity mockProfile;
    private CategoryEntity mockCategory;
    private ExpenseEntity mockExpenseEntity;
    private ExpenseDTO mockExpenseDTO;

    @BeforeEach
    void setUp() {
        mockProfile = ProfileEntity.builder()
                .id(1L)
                .fullName("Vedanti Test")
                .email("vedanti@test.com")
                .build();

        mockCategory = CategoryEntity.builder()
                .id(10L)
                .name("Food")
                .type("expense")
                .profile(mockProfile)
                .build();

        mockExpenseEntity = ExpenseEntity.builder()
                .id(100L)
                .name("Lunch")
                .icon("🍱")
                .amount(new BigDecimal("250.00"))
                .date(LocalDate.now())
                .category(mockCategory)
                .profile(mockProfile)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        mockExpenseDTO = ExpenseDTO.builder()
                .name("Lunch")
                .icon("🍱")
                .amount(new BigDecimal("250.00"))
                .date(LocalDate.now())
                .categoryId(10L)
                .build();
    }

    // ─── addExpense ────────────────────────────────────────────────────────────

    @Test
    void addExpense_shouldSaveAndReturnDTO() {
        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(mockCategory));
        when(expenseRepository.save(any(ExpenseEntity.class))).thenReturn(mockExpenseEntity);

        ExpenseDTO result = expenseService.addExpense(mockExpenseDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Lunch");
        assertThat(result.getAmount()).isEqualByComparingTo("250.00");
        assertThat(result.getCategoryName()).isEqualTo("Food");
        verify(expenseRepository, times(1)).save(any(ExpenseEntity.class));
    }

    @Test
    void addExpense_shouldThrow_whenCategoryNotFound() {
        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.addExpense(mockExpenseDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }

    // ─── getCurrentMonthExpensesForCurrentUser ─────────────────────────────────

    @Test
    void getCurrentMonthExpenses_shouldReturnListOfDTOs() {
        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(expenseRepository.findByProfileIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(mockExpenseEntity));

        List<ExpenseDTO> result = expenseService.getCurrentMonthExpensesForCurrentUser();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Lunch");
    }

    @Test
    void getCurrentMonthExpenses_shouldReturnEmptyList_whenNoExpenses() {
        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(expenseRepository.findByProfileIdAndDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        List<ExpenseDTO> result = expenseService.getCurrentMonthExpensesForCurrentUser();

        assertThat(result).isEmpty();
    }

    // ─── deleteExpense ─────────────────────────────────────────────────────────

    @Test
    void deleteExpense_shouldDeleteSuccessfully() {
        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(expenseRepository.findById(100L)).thenReturn(Optional.of(mockExpenseEntity));

        expenseService.deleteExpense(100L);

        verify(expenseRepository, times(1)).delete(mockExpenseEntity);
    }

    @Test
    void deleteExpense_shouldThrow_whenExpenseNotFound() {
        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(expenseRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteExpense(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Expense not found");
    }

    @Test
    void deleteExpense_shouldThrow_whenUnauthorized() {
        ProfileEntity otherProfile = ProfileEntity.builder().id(99L).email("other@test.com").build();
        ExpenseEntity otherExpense = ExpenseEntity.builder()
                .id(200L)
                .profile(otherProfile)
                .category(mockCategory)
                .build();

        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(expenseRepository.findById(200L)).thenReturn(Optional.of(otherExpense));

        assertThatThrownBy(() -> expenseService.deleteExpense(200L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");
    }

    // ─── getTotalExpenseForCurrentUser ─────────────────────────────────────────

    @Test
    void getTotalExpense_shouldReturnTotal() {
        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(expenseRepository.findTotalExpenseByProfileId(1L)).thenReturn(new BigDecimal("1500.00"));

        BigDecimal result = expenseService.getTotalExpenseForCurrentUser();

        assertThat(result).isEqualByComparingTo("1500.00");
    }

    @Test
    void getTotalExpense_shouldReturnZero_whenNull() {
        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(expenseRepository.findTotalExpenseByProfileId(1L)).thenReturn(null);

        BigDecimal result = expenseService.getTotalExpenseForCurrentUser();

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ─── filterExpenses ────────────────────────────────────────────────────────

    @Test
    void filterExpenses_shouldReturnFilteredList() {
        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                eq(1L), any(), any(), anyString(), any(Sort.class)))
                .thenReturn(List.of(mockExpenseEntity));

        List<ExpenseDTO> result = expenseService.filterExpenses(
                LocalDate.now().minusDays(7), LocalDate.now(), "lunch", Sort.by("date"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Lunch");
    }

    // ─── getLatest5ExpensesForCurrentUser ──────────────────────────────────────

    @Test
    void getLatest5Expenses_shouldReturnList() {
        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(expenseRepository.findTop5ByProfileIdOrderByDateDesc(1L))
                .thenReturn(List.of(mockExpenseEntity));

        List<ExpenseDTO> result = expenseService.getLatest5ExpensesForCurrentUser();

        assertThat(result).hasSize(1);
    }
}
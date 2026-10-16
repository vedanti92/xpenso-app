package com.example.xpenso.service;

import com.example.xpenso.dto.IncomeDTO;
import com.example.xpenso.entity.CategoryEntity;
import com.example.xpenso.entity.IncomeEntity;
import com.example.xpenso.entity.ProfileEntity;
import com.example.xpenso.repository.CategoryRepository;
import com.example.xpenso.repository.IncomeRepository;

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
class IncomeServiceTest {

        @Mock
        private CategoryRepository categoryRepository;

        @Mock
        private IncomeRepository incomeRepository;

        @Mock
        private ProfileService profileService;

        @InjectMocks
        private IncomeService incomeService;

        private ProfileEntity mockProfile;
        private CategoryEntity mockCategory;
        private IncomeEntity mockIncomeEntity;
        private IncomeDTO mockIncomeDTO;

        @BeforeEach
        void setUp() {
                mockProfile = ProfileEntity.builder()
                                .id(1L)
                                .fullName("Vedanti Test")
                                .email("vedanti@test.com")
                                .build();

                mockCategory = CategoryEntity.builder()
                                .id(10L)
                                .name("Salary")
                                .type("income")
                                .profile(mockProfile)
                                .build();

                mockIncomeEntity = IncomeEntity.builder()
                                .id(100L)
                                .name("Monthly Salary")
                                .icon("salary-icon")
                                .amount(new BigDecimal("50000.00"))
                                .date(LocalDate.now())
                                .category(mockCategory)
                                .profile(mockProfile)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                mockIncomeDTO = IncomeDTO.builder()
                                .name("Monthly Salary")
                                .icon("salary-icon")
                                .amount(new BigDecimal("50000.00"))
                                .date(LocalDate.now())
                                .categoryId(10L)
                                .build();
        }

        // ─── addIncome ─────────────────────────────────────────────────────────

        @Test
        void addIncome_shouldSaveAndReturnDTO() {
                when(profileService.getCurrentProfile()).thenReturn(mockProfile);
                when(categoryRepository.findByIdAndProfileId(10L, 1L))
                                .thenReturn(Optional.of(mockCategory));
                when(incomeRepository.save(any(IncomeEntity.class)))
                                .thenReturn(mockIncomeEntity);

                IncomeDTO result = incomeService.addIncome(mockIncomeDTO);

                assertThat(result).isNotNull();
                assertThat(result.getName()).isEqualTo("Monthly Salary");
                assertThat(result.getAmount()).isEqualByComparingTo("50000.00");
                assertThat(result.getCategoryName()).isEqualTo("Salary");

                verify(incomeRepository, times(1)).save(any(IncomeEntity.class));
        }

        @Test
        void addIncome_shouldThrow_whenCategoryNotFound() {
                when(profileService.getCurrentProfile()).thenReturn(mockProfile);
                when(categoryRepository.findByIdAndProfileId(10L, 1L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> incomeService.addIncome(mockIncomeDTO))
                                .isInstanceOf(RuntimeException.class)
                                .hasMessageContaining("Category not found");

                verify(incomeRepository, never()).save(any(IncomeEntity.class));
        }

        // ─── getCurrentMonthIncomesForCurrentUser ───────────────────────────────

        @Test
        void getCurrentMonthIncomes_shouldReturnListOfDTOs() {
                when(profileService.getCurrentProfile()).thenReturn(mockProfile);
                when(incomeRepository.findByProfileIdAndDateBetween(
                                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(List.of(mockIncomeEntity));

                List<IncomeDTO> result = incomeService.getCurrentMonthIncomesForCurrentUser();

                assertThat(result).hasSize(1);
                assertThat(result.get(0).getName()).isEqualTo("Monthly Salary");
        }

        @Test
        void getCurrentMonthIncomes_shouldReturnEmptyList_whenNoIncomes() {
                when(profileService.getCurrentProfile()).thenReturn(mockProfile);
                when(incomeRepository.findByProfileIdAndDateBetween(
                                eq(1L), any(LocalDate.class), any(LocalDate.class)))
                                .thenReturn(List.of());

                List<IncomeDTO> result = incomeService.getCurrentMonthIncomesForCurrentUser();

                assertThat(result).isEmpty();
        }

        // ─── deleteIncome ───────────────────────────────────────────────────────

        @Test
        void deleteIncome_shouldDeleteSuccessfully() {
                when(profileService.getCurrentProfile()).thenReturn(mockProfile);
                when(incomeRepository.findById(100L))
                                .thenReturn(Optional.of(mockIncomeEntity));

                incomeService.deleteIncome(100L);

                verify(incomeRepository, times(1)).delete(mockIncomeEntity);
        }

        @Test
        void deleteIncome_shouldThrow_whenIncomeNotFound() {
                when(profileService.getCurrentProfile()).thenReturn(mockProfile);
                when(incomeRepository.findById(999L))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> incomeService.deleteIncome(999L))
                                .isInstanceOf(RuntimeException.class)
                                .hasMessageContaining("Income not found");

                verify(incomeRepository, never()).delete(any(IncomeEntity.class));
        }

        @Test
        void deleteIncome_shouldThrow_whenUnauthorized() {
                ProfileEntity otherProfile = ProfileEntity.builder()
                                .id(99L)
                                .email("other@test.com")
                                .build();

                IncomeEntity otherIncome = IncomeEntity.builder()
                                .id(200L)
                                .profile(otherProfile)
                                .category(mockCategory)
                                .build();

                when(profileService.getCurrentProfile()).thenReturn(mockProfile);
                when(incomeRepository.findById(200L))
                                .thenReturn(Optional.of(otherIncome));

                assertThatThrownBy(() -> incomeService.deleteIncome(200L))
                                .isInstanceOf(RuntimeException.class)
                                .hasMessageContaining("Unauthorized");

                verify(incomeRepository, never()).delete(any(IncomeEntity.class));
        }

        // ─── getTotalIncomeForCurrentUser ──────────────────────────────────────

        @Test
        void getTotalIncome_shouldReturnTotal() {
                when(profileService.getCurrentProfile()).thenReturn(mockProfile);
                when(incomeRepository.findTotalIncomeByProfileId(1L))
                                .thenReturn(new BigDecimal("75000.00"));

                BigDecimal result = incomeService.getTotalIncomeForCurrentUser();

                assertThat(result).isEqualByComparingTo("75000.00");
        }

        @Test
        void getTotalIncome_shouldReturnZero_whenNull() {
                when(profileService.getCurrentProfile()).thenReturn(mockProfile);
                when(incomeRepository.findTotalIncomeByProfileId(1L))
                                .thenReturn(null);

                BigDecimal result = incomeService.getTotalIncomeForCurrentUser();

                assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        // ─── filterIncomes ─────────────────────────────────────────────────────

        @Test
        void filterIncomes_shouldReturnFilteredList() {
                when(profileService.getCurrentProfile()).thenReturn(mockProfile);
                when(incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                                eq(1L), any(), any(), anyString(), any(Sort.class)))
                                .thenReturn(List.of(mockIncomeEntity));

                List<IncomeDTO> result = incomeService.filterIncomes(
                                LocalDate.now().minusDays(7),
                                LocalDate.now(),
                                "salary",
                                Sort.by("date"));

                assertThat(result).hasSize(1);
                assertThat(result.get(0).getName()).isEqualTo("Monthly Salary");
        }

        // ─── getLatest5IncomesForCurrentUser ────────────────────────────────────

        @Test
        void getLatest5Incomes_shouldReturnList() {
                when(profileService.getCurrentProfile()).thenReturn(mockProfile);
                when(incomeRepository.findTop5ByProfileIdOrderByDateDesc(1L))
                                .thenReturn(List.of(mockIncomeEntity));

                List<IncomeDTO> result = incomeService.getLatest5IncomesForCurrentUser();

                assertThat(result).hasSize(1);
                assertThat(result.get(0).getName()).isEqualTo("Monthly Salary");
        }
}

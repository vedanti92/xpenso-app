package com.example.xpenso.service;

import com.example.xpenso.dto.CategoryDTO;
import com.example.xpenso.entity.CategoryEntity;
import com.example.xpenso.entity.ProfileEntity;
import com.example.xpenso.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private ProfileEntity mockProfile;
    private CategoryEntity mockCategoryEntity;
    private CategoryDTO mockCategoryDTO;

    @BeforeEach
    void setUp() {
        mockProfile = ProfileEntity.builder()
                .id(1L)
                .fullName("Vedanti Test")
                .email("vedanti@test.com")
                .build();

        mockCategoryEntity = CategoryEntity.builder()
                .id(10L)
                .name("Food")
                .icon("🍔")
                .type("expense")
                .profile(mockProfile)
                .build();

        mockCategoryDTO = CategoryDTO.builder()
                .name("Food")
                .icon("🍔")
                .type("expense")
                .build();
    }

    // ─── saveCategory ──────────────────────────────────────────────────────────

    @Test
    void saveCategory_shouldSaveAndReturnDTO() {
        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(categoryRepository.existsByNameAndProfileId("Food", 1L)).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(mockCategoryEntity);

        CategoryDTO result = categoryService.saveCategory(mockCategoryDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Food");
        assertThat(result.getType()).isEqualTo("expense");
        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
    }

    @Test
    void saveCategory_shouldThrow_whenDuplicateName() {
        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(categoryRepository.existsByNameAndProfileId("Food", 1L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.saveCategory(mockCategoryDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already exists");
    }

    // ─── getCategoriesForCurrentUser ───────────────────────────────────────────

    @Test
    void getCategories_shouldReturnList() {
        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(categoryRepository.findByProfileId(1L)).thenReturn(List.of(mockCategoryEntity));

        List<CategoryDTO> result = categoryService.getCategoriesForCurrentUser();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Food");
    }

    @Test
    void getCategories_shouldReturnEmptyList_whenNoneExist() {
        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(categoryRepository.findByProfileId(1L)).thenReturn(List.of());

        List<CategoryDTO> result = categoryService.getCategoriesForCurrentUser();

        assertThat(result).isEmpty();
    }

    // ─── getCategoriesByTypeForCurrentUser ─────────────────────────────────────

    @Test
    void getCategoriesByType_shouldReturnFilteredList() {
        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(categoryRepository.findByTypeAndProfileId("expense", 1L))
                .thenReturn(List.of(mockCategoryEntity));

        List<CategoryDTO> result = categoryService.getCategoriesByTypeForCurrentUser("expense");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("expense");
    }

    // ─── updateCategory ────────────────────────────────────────────────────────

    @Test
    void updateCategory_shouldUpdateAndReturnDTO() {
        CategoryDTO updatedDTO = CategoryDTO.builder()
                .name("Groceries")
                .icon("🛒")
                .type("expense")
                .build();

        CategoryEntity updatedEntity = CategoryEntity.builder()
                .id(10L)
                .name("Groceries")
                .icon("🛒")
                .type("expense")
                .profile(mockProfile)
                .build();

        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(categoryRepository.findByIdAndProfileId(10L, 1L)).thenReturn(Optional.of(mockCategoryEntity));
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(updatedEntity);

        CategoryDTO result = categoryService.updateCategory(10L, updatedDTO);

        assertThat(result.getName()).isEqualTo("Groceries");
        assertThat(result.getIcon()).isEqualTo("🛒");
    }

    @Test
    void updateCategory_shouldThrow_whenNotFound() {
        when(profileService.getCurrentProfile()).thenReturn(mockProfile);
        when(categoryRepository.findByIdAndProfileId(anyLong(), anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(999L, mockCategoryDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }
}
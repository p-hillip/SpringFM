package com.phrontend.springfm.files;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoredFileSpecificationsTest {

    @Mock
    private Root<StoredFile> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Predicate predicate;

    @Test
    void matchesQuery_WithValidQuery_ReturnsSpecification() {
        // Arrange
        String searchQuery = "test document";
        
        when(criteriaBuilder.lower(any())).thenReturn(null);
        when(criteriaBuilder.like(any(), anyString())).thenReturn(predicate);
        when(criteriaBuilder.or(any(), any(), any())).thenReturn(predicate);

        // Act
        Specification<StoredFile> spec = StoredFileSpecifications.matchesQuery(searchQuery);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(result).isNotNull().isEqualTo(predicate);
        verify(criteriaBuilder).or(any(), any(), any());
        verify(criteriaBuilder, atLeast(3)).like(any(), eq("%test document%"));
    }

    @Test
    void matchesQuery_WithNullQuery_ReturnsConjunction() {
        // Arrange
        when(criteriaBuilder.conjunction()).thenReturn(predicate);

        // Act
        Specification<StoredFile> spec = StoredFileSpecifications.matchesQuery(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(result).isNotNull().isEqualTo(predicate);
        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder, never()).like(any(), anyString());
    }

    @Test
    void matchesQuery_WithBlankQuery_ReturnsConjunction() {
        // Arrange
        when(criteriaBuilder.conjunction()).thenReturn(predicate);

        // Act
        Specification<StoredFile> spec = StoredFileSpecifications.matchesQuery("   ");
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(result).isNotNull().isEqualTo(predicate);
        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder, never()).like(any(), anyString());
    }

    @Test
    void matchesQuery_WithEmptyQuery_ReturnsConjunction() {
        // Arrange
        when(criteriaBuilder.conjunction()).thenReturn(predicate);

        // Act
        Specification<StoredFile> spec = StoredFileSpecifications.matchesQuery("");
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(result).isNotNull().isEqualTo(predicate);
        verify(criteriaBuilder).conjunction();
        verify(criteriaBuilder, never()).like(any(), anyString());
    }

    @Test
    void categoryIn_WithCategories_ReturnsNonNullSpecification() {
        // Arrange
        List<FileCategory> categories = Arrays.asList(FileCategory.DOCUMENT, FileCategory.IMAGE);
        when(root.get("category")).thenReturn(null);

        // Act
        Specification<StoredFile> spec = StoredFileSpecifications.categoryIn(categories);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(result).isNotNull();
        verify(root).get("category");
    }

    @Test
    void categoryIn_WithNullCategories_ReturnsConjunction() {
        // Arrange
        when(criteriaBuilder.conjunction()).thenReturn(predicate);

        // Act
        Specification<StoredFile> spec = StoredFileSpecifications.categoryIn(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(result).isNotNull().isEqualTo(predicate);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void categoryIn_WithEmptyCategories_ReturnsConjunction() {
        // Arrange
        when(criteriaBuilder.conjunction()).thenReturn(predicate);

        // Act
        Specification<StoredFile> spec = StoredFileSpecifications.categoryIn(Collections.emptyList());
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(result).isNotNull().isEqualTo(predicate);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void categoryIn_WithSingleCategory_ReturnsNonNullSpecification() {
        // Arrange
        List<FileCategory> categories = Collections.singletonList(FileCategory.VIDEO);
        when(root.get("category")).thenReturn(null);

        // Act
        Specification<StoredFile> spec = StoredFileSpecifications.categoryIn(categories);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(result).isNotNull();
        verify(root).get("category");
    }

    @Test
    void categoryIn_WithAllCategories_ReturnsNonNullSpecification() {
        // Arrange
        List<FileCategory> allCategories = Arrays.asList(FileCategory.values());
        when(root.get("category")).thenReturn(null);

        // Act
        Specification<StoredFile> spec = StoredFileSpecifications.categoryIn(allCategories);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Assert
        assertThat(spec).isNotNull();
        assertThat(result).isNotNull();
        verify(root).get("category");
    }
}

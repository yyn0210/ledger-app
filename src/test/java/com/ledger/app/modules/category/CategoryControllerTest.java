package com.ledger.app.modules.category;

import com.ledger.app.common.result.Result;
import com.ledger.app.modules.category.controller.CategoryController;
import com.ledger.app.modules.category.dto.request.CreateCategoryRequest;
import com.ledger.app.modules.category.dto.request.UpdateCategoryRequest;
import com.ledger.app.modules.category.dto.response.CategoryResponse;
import com.ledger.app.modules.category.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 分类控制器单元测试
 */
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;
    private CategoryResponse testCategory;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();

        testCategory = new CategoryResponse();
        testCategory.setId(1L);
        testCategory.setBookId(100L);
        testCategory.setName("测试分类");
        testCategory.setIcon("🧪");
        testCategory.setType(1);
        testCategory.setSortOrder(1);
    }

    @Test
    void testGetCategories() throws Exception {
        // Arrange
        List<CategoryResponse> categories = new ArrayList<>();
        categories.add(testCategory);
        when(categoryService.getCategories(100L, 1)).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/api/categories")
                .param("bookId", "100")
                .param("type", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(categoryService).getCategories(100L, 1);
    }

    @Test
    void testGetCategory() throws Exception {
        // Arrange
        when(categoryService.getCategory(1L, 100L)).thenReturn(testCategory);

        // Act & Assert
        mockMvc.perform(get("/api/categories/1")
                .param("bookId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试分类"));

        verify(categoryService).getCategory(1L, 100L);
    }

    @Test
    void testCreateCategory() throws Exception {
        // Arrange
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setBookId(100L);
        request.setParentId(0L);
        request.setName("新分类");
        request.setType(1);

        when(categoryService.createCategory(any(CreateCategoryRequest.class))).thenReturn(1L);

        // Act & Assert
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookId\":100,\"parentId\":0,\"name\":\"新分类\",\"type\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));

        verify(categoryService).createCategory(any(CreateCategoryRequest.class));
    }

    @Test
    void testUpdateCategory() throws Exception {
        // Arrange
        doNothing().when(categoryService).updateCategory(anyLong(), any(UpdateCategoryRequest.class), anyLong());

        // Act & Assert
        mockMvc.perform(put("/api/categories/1")
                .param("bookId", "100")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"更新分类\",\"sortOrder\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(categoryService).updateCategory(anyLong(), any(UpdateCategoryRequest.class), anyLong());
    }

    @Test
    void testDeleteCategory() throws Exception {
        // Arrange
        doNothing().when(categoryService).deleteCategory(anyLong(), anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/categories/1")
                .param("bookId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(categoryService).deleteCategory(1L, 100L);
    }

    @Test
    void testGetSystemCategories() throws Exception {
        // Arrange
        List<CategoryResponse> categories = new ArrayList<>();
        categories.add(testCategory);
        when(categoryService.getSystemCategories(1)).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/api/categories/system")
                .param("type", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(categoryService).getSystemCategories(1);
    }

    @Test
    void testInitSystemCategories() throws Exception {
        // Arrange
        doNothing().when(categoryService).initSystemCategories(anyLong());

        // Act & Assert
        mockMvc.perform(post("/api/categories/system/init")
                .param("bookId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(categoryService).initSystemCategories(100L);
    }
}

package com.ledger.app.modules.book;

import com.ledger.app.modules.book.controller.BookController;
import com.ledger.app.modules.book.dto.request.CreateBookRequest;
import com.ledger.app.modules.book.dto.response.BookResponse;
import com.ledger.app.modules.book.entity.Book;
import com.ledger.app.modules.book.service.BookService;
import com.ledger.app.common.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 账本模块单元测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();
    }

    @Test
    void testCreateBook() throws Exception {
        // 准备测试数据
        CreateBookRequest request = CreateBookRequest.builder()
                .name("测试账本")
                .icon("wallet")
                .build();

        Long bookId = 1L;
        when(bookService.create(any(CreateBookRequest.class), anyLong())).thenReturn(bookId);

        // 执行测试
        mockMvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"测试账本\",\"icon\":\"wallet\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(1));

        // 验证
        verify(bookService, times(1)).create(any(CreateBookRequest.class), anyLong());
    }

    @Test
    void testGetBookById() throws Exception {
        // 准备测试数据
        BookResponse response = BookResponse.builder()
                .id(1L)
                .name("测试账本")
                .icon("wallet")
                .userId(1L)
                .build();

        when(bookService.getById(eq(1L), anyLong())).thenReturn(response);

        // 执行测试
        mockMvc.perform(get("/api/book/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("测试账本"));

        // 验证
        verify(bookService, times(1)).getById(1L, anyLong());
    }

    @Test
    void testUpdateBook() throws Exception {
        // 执行测试
        mockMvc.perform(put("/api/book/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"更新后的账本\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证
        verify(bookService, times(1)).update(eq(1L), any(), anyLong());
    }

    @Test
    void testDeleteBook() throws Exception {
        // 执行测试
        mockMvc.perform(delete("/api/book/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证
        verify(bookService, times(1)).delete(1L, anyLong());
    }

    @Test
    void testSetDefaultBook() throws Exception {
        // 执行测试
        mockMvc.perform(post("/api/book/1/set-default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证
        verify(bookService, times(1)).setDefault(1L, anyLong());
    }
}

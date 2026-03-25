package com.ledger.app.modules.book;

import com.ledger.app.modules.book.entity.Book;
import com.ledger.app.modules.book.repository.BookRepository;
import com.ledger.app.modules.book.service.BookService;
import com.ledger.app.modules.book.dto.CreateBookRequest;
import com.ledger.app.modules.book.dto.BookResponse;
import com.ledger.app.modules.book.entity.Book;
import com.ledger.app.modules.book.repository.BookRepository;
import com.ledger.app.modules.book.service.BookService;
import com.ledger.app.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 账本服务单元测试
 *
 * @author Chisong
 * @since 2026-03-24
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;
    private BookService bookService;

    private CreateBookRequest createRequest;
    private Book book;

    @BeforeEach
    void setUp() {
        createRequest = CreateBookRequest.builder()
                .name("测试账本")
                .icon("wallet")
                .build();

        book = Book.builder()
                .id(1L)
                .name("测试账本")
                .icon("wallet")
                .userId(1L)
                .isDefault(true)
                .build();
    }

    @Test
    void testCreateBook() {
        // 准备
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // 执行
        Long bookId = bookService.create(createRequest, 1L);

        // 验证
        assertNotNull(bookId);
        assertEquals(1L, bookId);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testGetBookById_Success() {
        // 准备
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        // 执行
        BookResponse response = bookService.getById(1L, 1L);

        // 验证
        assertNotNull(response);
        assertEquals("测试账本", response.getName());
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    void testGetBookById_NotFound() {
        // 准备
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        // 执行 & 验证
        assertThrows(BusinessException.class, () -> bookService.getById(1L, 1L));
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    void testGetBookById_NoPermission() {
        // 准备
        Book otherBook = Book.builder()
                .id(1L)
                .name("别人的账本")
                .userId(2L)
                .build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(otherBook));

        // 执行 & 验证
        assertThrows(BusinessException.class, () -> bookService.getById(1L, 1L));
    }

    @Test
    void testUpdateBook() {
        // 准备
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // 执行
        bookService.update(1L, createRequest, 1L);

        // 验证
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testDeleteBook() {
        // 准备
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        // 执行
        bookService.delete(1L, 1L);

        // 验证
        verify(bookRepository, times(1)).deleteById(1L);
    }

    @Test
    void testSetDefault_Success() {
        // 准备
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        // 执行
        bookService.setDefault(1L, 1L);

        // 验证
        assertTrue(book.getIsDefault());
        verify(bookRepository, times(1)).save(book);
    }
}

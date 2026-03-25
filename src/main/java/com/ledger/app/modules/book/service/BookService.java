package com.ledger.app.modules.book.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ledger.app.common.exception.BusinessException;
import com.ledger.app.modules.book.dto.BookResponse;
import com.ledger.app.modules.book.dto.CreateBookRequest;
import com.ledger.app.modules.book.dto.UpdateBookRequest;
import com.ledger.app.modules.book.entity.Book;
import com.ledger.app.modules.book.repository.BookRepository;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 账本服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookService extends ServiceImpl<BookRepository, Book> {

    private final BookRepository bookRepository;

    /**
     * 获取用户的所有账本
     */
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksByUserId(Long userId) {
        List<Book> books = bookRepository.findByUserId(userId);
        return books.stream()
                .map(BookResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 获取账本详情（权限检查）
     */
    @Transactional(readOnly = true)
    public BookResponse getBookByIdAndUserId(Long bookId, Long userId) {
        Book book = bookRepository.findByIdAndUserId(bookId, userId);
        if (book == null) {
            throw new BusinessException("账本不存在或无权访问");
        }
        return BookResponse.fromEntity(book);
    }

    /**
     * 创建新账本
     */
    @Transactional
    public BookResponse createBook(Long userId, CreateBookRequest request) {
        // 如果设为默认账本，先取消其他默认
        if (Boolean.TRUE.equals(request.getSetAsDefault())) {
            cancelDefaultBook(userId);
        }

        // 创建账本
        Book book = new Book();
        book.setUserId(userId);
        book.setName(request.getName());
        book.setIcon(request.getIcon());
        book.setColor(request.getColor());
        book.setType(request.getType() != null ? request.getType() : 1);
        book.setIsDefault(Boolean.TRUE.equals(request.getSetAsDefault()) ? 1 : 0);
        book.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        book.setDeleted(0);

        bookRepository.insert(book);
        log.info("创建账本成功：userId={}, bookId={}, name={}", userId, book.getId(), book.getName());

        return BookResponse.fromEntity(book);
    }

    /**
     * 更新账本（权限检查）
     */
    @Transactional
    public BookResponse updateBook(Long bookId, Long userId, UpdateBookRequest request) {
        Book book = bookRepository.findByIdAndUserId(bookId, userId);
        if (book == null) {
            throw new BusinessException("账本不存在或无权访问");
        }

        // 更新字段
        if (request.getName() != null) {
            book.setName(request.getName());
        }
        if (request.getIcon() != null) {
            book.setIcon(request.getIcon());
        }
        if (request.getColor() != null) {
            book.setColor(request.getColor());
        }
        if (request.getSortOrder() != null) {
            book.setSortOrder(request.getSortOrder());
        }

        // 处理默认账本设置
        if (Boolean.TRUE.equals(request.getSetAsDefault())) {
            cancelDefaultBook(userId);
            book.setIsDefault(1);
        } else if (Boolean.FALSE.equals(request.getSetAsDefault())) {
            // 检查是否是最后一个账本，最后一个不能取消默认
            int count = bookRepository.countDefaultByUserId(userId);
            if (count > 1) {
                book.setIsDefault(0);
            }
        }

        bookRepository.updateById(book);
        log.info("更新账本成功：userId={}, bookId={}", userId, bookId);

        return BookResponse.fromEntity(book);
    }

    /**
     * 删除账本（软删除，权限检查）
     */
    @Transactional
    public void deleteBook(Long bookId, Long userId) {
        Book book = bookRepository.findByIdAndUserId(bookId, userId);
        if (book == null) {
            throw new BusinessException("账本不存在或无权访问");
        }

        // 检查是否是默认账本，默认账本不能删除
        if (book.getIsDefault() == 1) {
            throw new BusinessException("不能删除默认账本");
        }

        // 软删除
        book.setDeleted(1);
        bookRepository.updateById(book);
        log.info("删除账本成功：userId={}, bookId={}", userId, bookId);
    }

    /**
     * 取消用户的默认账本
     */
    private void cancelDefaultBook(Long userId) {
        List<Book> defaultBooks = bookRepository.findByUserId(userId).stream()
                .filter(b -> b.getIsDefault() == 1)
                .collect(Collectors.toList());

        for (Book book : defaultBooks) {
            book.setIsDefault(0);
            bookRepository.updateById(book);
        }
    }

    /**
     * 获取或创建默认账本
     */
    @Transactional
    public BookResponse getOrCreateDefaultBook(Long userId) {
        Book defaultBook = bookRepository.findDefaultByUserId(userId);
        
        if (defaultBook == null) {
            // 创建默认账本
            CreateBookRequest request = new CreateBookRequest();
            request.setName("我的账本");
            request.setSetAsDefault(true);
            request.setType(1);
            return createBook(userId, request);
        }

        return BookResponse.fromEntity(defaultBook);
    }
}

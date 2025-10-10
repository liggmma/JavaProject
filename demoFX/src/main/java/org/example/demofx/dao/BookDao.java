package org.example.demofx.dao;

import org.example.demofx.model.Book;
import org.example.demofx.repository.BookRepository;

import java.util.List;

public class BookDao extends BookRepository {

    public void addBook(Book book) {
        save(book);
    }

    public void updateBook(Book book) {
        save(book);
    }

    public void deleteBook(int id) {
        delete(id);
    }

    public Book getBookById(int id) {
        return findById(id);
    }

    public List<Book> getAllBooks() {
        return findAll();
    }
}

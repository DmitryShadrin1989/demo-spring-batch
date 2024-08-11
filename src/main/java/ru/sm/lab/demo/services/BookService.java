package ru.sm.lab.demo.services;

import ru.sm.lab.demo.dto.BookDto;

import java.util.List;

public interface BookService {

    List<BookDto> findAll();
}

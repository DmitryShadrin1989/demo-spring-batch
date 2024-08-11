package ru.sm.lab.demo.services;

import ru.sm.lab.demo.models.mongo.Author;

import java.util.List;

public interface AuthorService {
    List<Author> findAll();
}

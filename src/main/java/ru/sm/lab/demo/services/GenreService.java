package ru.sm.lab.demo.services;

import ru.sm.lab.demo.models.mongo.Genre;

import java.util.List;

public interface GenreService {

    List<Genre> findAll();
}

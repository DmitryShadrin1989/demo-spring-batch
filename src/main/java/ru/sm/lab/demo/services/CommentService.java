package ru.sm.lab.demo.services;

import ru.sm.lab.demo.models.mongo.Comment;

import java.util.List;

public interface CommentService {

    List<Comment> findAll();

    List<Comment> findAllByBookId(String bookId);
}

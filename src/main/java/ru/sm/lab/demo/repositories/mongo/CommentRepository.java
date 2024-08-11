package ru.sm.lab.demo.repositories.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.sm.lab.demo.models.mongo.Comment;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {

    List<Comment> findAllByBookId(String bookId);
}

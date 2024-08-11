package ru.sm.lab.demo.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;
import ru.sm.lab.demo.models.mongo.Book;
import ru.sm.lab.demo.models.mongo.Comment;
import ru.sm.lab.demo.models.record.CommentRecord;

import java.util.Collections;

@RequiredArgsConstructor
@Component
public class CommentConverter {

    private final NamedParameterJdbcOperations namedParameterJdbcOperations;

    public String commentToString(Comment comment) {
        return "Id: %s, content: %s".formatted(
                comment.getId(),
                comment.getContent());
    }

    public Comment convertToDomain(CommentRecord commentRecord) {
        String documentId = namedParameterJdbcOperations.queryForObject(
                "select t.id_document from temp_book_ids t where t.id_table = :bookId",
                Collections.singletonMap("bookId", commentRecord.bookId()), String.class);

        return new Comment(null, commentRecord.content(), new Book(documentId, null, null, null));
    }
}

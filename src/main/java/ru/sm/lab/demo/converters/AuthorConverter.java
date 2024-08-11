package ru.sm.lab.demo.converters;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import ru.sm.lab.demo.models.jpa.AuthorJpa;
import ru.sm.lab.demo.models.mongo.Author;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthorConverter {

    private final Map<Long, Author> authorIdsMap;

    public AuthorConverter() {
        this.authorIdsMap = new ConcurrentHashMap<>();
    }

    public String authorToString(Author author) {
        return "Id: %s, FullName: %s".formatted(author.getId(), author.getFullName());
    }

    public Author getAuthor(Long authorJdbcDtoId) {
        return authorIdsMap.get(authorJdbcDtoId);
    }

    public Author convertToDomain(AuthorJpa authorJpa) {
        String authorId = new ObjectId().toString();
        Author author = new Author(authorId, authorJpa.getFullName());
        authorIdsMap.put(authorJpa.getId(), author);
        return author;
    }
}

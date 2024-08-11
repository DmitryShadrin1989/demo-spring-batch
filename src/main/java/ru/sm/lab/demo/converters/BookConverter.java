package ru.sm.lab.demo.converters;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import ru.sm.lab.demo.dto.BookDto;
import ru.sm.lab.demo.models.jpa.BookJpa;
import ru.sm.lab.demo.models.mongo.Author;
import ru.sm.lab.demo.models.mongo.Book;
import ru.sm.lab.demo.models.mongo.Genre;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookConverter {

    private final AuthorConverter authorConverter;

    private final GenreConverter genreConverter;

    public String bookToString(BookDto book) {
        var genresString = book.getGenres().stream()
                .map(genreConverter::genreToString)
                .map("{%s}"::formatted)
                .collect(Collectors.joining(", "));
        return "Id: %s, title: %s, author: {%s}, genres: [%s]".formatted(
                book.getId(),
                book.getTitle(),
                authorConverter.authorToString(book.getAuthor()),
                genresString);
    }

    public Map<String, Object> convertToDomain(BookJpa bookJpa) {
        String bookId = new ObjectId().toString();
        String title = bookJpa.getTitle();
        Author author = authorConverter.getAuthor(bookJpa.getAuthorJpa().getId());
        List<Genre> genres = bookJpa.getGenreJpas().stream()
                .map(g -> genreConverter.getGenre(g.getId()))
                .toList();
        return Map.of("book", new Book(bookId, title, author, genres),
                "relation", Map.of("jpaId", bookJpa.getId(), "mongoId", bookId));
    }
}

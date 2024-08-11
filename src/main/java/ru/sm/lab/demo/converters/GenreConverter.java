package ru.sm.lab.demo.converters;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import ru.sm.lab.demo.models.jpa.GenreJpa;
import ru.sm.lab.demo.models.mongo.Genre;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GenreConverter {

    private final Map<Long, Genre> genreIdsMap;

    public GenreConverter() {
        this.genreIdsMap = new ConcurrentHashMap<>();
    }

    public String genreToString(Genre genre) {
        return "Id: %s, Name: %s".formatted(genre.getId(), genre.getName());
    }

    public Genre getGenre(Long genreJdbcDtoId) {
        return genreIdsMap.get(genreJdbcDtoId);
    }

    public Genre convertToDomain(GenreJpa genreJpa) {
        String genreId = new ObjectId().toString();
        Genre genre = new Genre(genreId, genreJpa.getName());
        genreIdsMap.put(genreJpa.getId(), genre);
        return genre;
    }
}

package ru.sm.lab.demo.repositories.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.sm.lab.demo.models.mongo.Genre;

public interface GenreRepository extends MongoRepository<Genre, String> {

}

package ru.sm.lab.demo.step.castom.writer;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.data.MongoItemWriter;
import ru.sm.lab.demo.models.mongo.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomBookMongoItemWriter<T> extends MongoItemWriter<T> {

    @Override
    public void write(Chunk<? extends T> chunk) throws Exception {
        List<? extends T> items = chunk.getItems();
        List<Book> books = new ArrayList<>();
        for (T item : items) {
            Map<String, Object> compositeData = (Map<String, Object>) item;
            books.add((Book) compositeData.get("book"));
        }
        super.write(new Chunk(books));
    }
}

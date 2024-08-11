package ru.sm.lab.demo.step.castom.reader;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;

import java.util.List;

public class CustomCompositeItemReader<T> implements ItemStreamReader<T> {

    private final List<ItemStreamReader<T>> readers;

    private int currentReaderIndex = 0;

    public CustomCompositeItemReader(List<ItemStreamReader<T>> readers) {
        this.readers = readers;
    }

    @Override
    public T read() throws Exception {
        T item = null;
        while (item == null && currentReaderIndex < readers.size()) {
            item = readers.get(currentReaderIndex).read();
            if (item == null) {
                currentReaderIndex++;
            }
        }
        return item;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        for (ItemStream reader : readers) {
            reader.open(executionContext);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        for (ItemStream reader : readers) {
            reader.update(executionContext);
        }
    }

    @Override
    public void close() throws ItemStreamException {
        for (ItemStream reader : readers) {
            reader.close();
        }
    }
}

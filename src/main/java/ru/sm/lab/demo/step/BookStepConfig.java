package ru.sm.lab.demo.step;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;
import ru.sm.lab.demo.converters.BookConverter;
import ru.sm.lab.demo.models.jpa.BookJpa;
import ru.sm.lab.demo.repositories.jpa.BookJpaRepository;
import ru.sm.lab.demo.step.castom.writer.CustomBookMongoItemWriter;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BookStepConfig {

    private static final int CHUNK_SIZE = 3;

    private static final Map<String, Sort.Direction> SORTS = Map.of("id", Sort.Direction.ASC);

    private final JobRepository jobRepository;

    private final PlatformTransactionManager platformTransactionManager;

    private final DataSource dataSource;

    private final MongoTemplate mongoTemplate;

    private final BookJpaRepository bookJpaRepository;

    private final BookConverter bookConverter;

    @Bean
    public ItemReader<BookJpa> bookItemReader() {
        return new RepositoryItemReaderBuilder<BookJpa>()
                .name("bookItemReader")
                .sorts(SORTS)
                .repository(bookJpaRepository)
                .methodName("findAll")
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    public ItemProcessor<BookJpa, Map<String, Object>> bookProcessor() {
        return bookConverter::convertToDomain;
    }

    @Bean
    public CustomBookMongoItemWriter<Map<String, Object>> customBookMongoItemWriter() {
        CustomBookMongoItemWriter<Map<String, Object>> customBookMongoItemWriter =
                new CustomBookMongoItemWriter<>();
        customBookMongoItemWriter.setCollection("books");
        customBookMongoItemWriter.setTemplate(mongoTemplate);

        return customBookMongoItemWriter;
    }

    @Bean
    public JdbcBatchItemWriter<Map<String, Object>> tempBookIdsJdbcBatchItemWriter() {
        return new JdbcBatchItemWriterBuilder<Map<String, Object>>()
                .dataSource(dataSource)
                .sql("insert into temp_book_ids(id_table, id_document) values (?, ?)")
                .itemPreparedStatementSetter(
                        (item, ps) -> {
                            Map<String, Object> relation = (Map<String, Object>) item.get("relation");
                            ps.setLong(1, (Long) relation.get("jpaId"));
                            ps.setString(2, (String) relation.get("mongoId"));
                        }).build();
    }

    @Bean
    public CompositeItemWriter<Map<String, Object>> bookCompositeItemWriter(
            CustomBookMongoItemWriter<Map<String, Object>> bookMongoItemWriter,
            JdbcBatchItemWriter<Map<String, Object>> tempBookIdsJdbcBatchItemWriter) {
        CompositeItemWriter<Map<String, Object>> writer = new CompositeItemWriter<>();
        writer.setDelegates(List.of(bookMongoItemWriter, tempBookIdsJdbcBatchItemWriter));
        return writer;
    }

    @Bean
    public Step migrateBooksStep(ItemReader<BookJpa> reader,
                                 ItemProcessor<BookJpa, Map<String, Object>> itemProcessor,
                                 CompositeItemWriter<Map<String, Object>> writer) {
        return new StepBuilder("migrateBooksStep", jobRepository)
                .<BookJpa, Map<String, Object>>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(reader)
                .processor(itemProcessor)
                .writer(writer)
                .listener(new ItemReadListener<BookJpa>() {
                    public void beforeRead() {
                        log.info("Начало чтения");
                    }

                    public void afterRead(@NonNull BookJpa o) {
                        log.info("Конец чтения");
                    }

                    public void onReadError(@NonNull Exception e) {
                        log.info("Ошибка чтения");
                    }
                })
                .listener(new ItemWriteListener<Map<String, Object>>() {
                    public void beforeWrite(@NonNull List<Map<String, Object>> list) {
                        log.info("Начало записи");
                    }

                    public void afterWrite(@NonNull List<Map<String, Object>> list) {
                        log.info("Конец записи");
                    }

                    public void onWriteError(@NonNull Exception e, @NonNull List<Map<String, Object>> list) {
                        log.info("Ошибка записи");
                    }
                })
                .listener(new ItemProcessListener<BookJpa, Map<String, Object>>() {
                    public void beforeProcess(@NonNull BookJpa o) {
                        log.info("Начало обработки");
                    }

                    public void afterProcess(@NonNull BookJpa o, Map<String, Object> o2) {
                        log.info("Конец обработки");
                    }

                    public void onProcessError(@NonNull BookJpa o, @NonNull Exception e) {
                        log.info("Ошибка обработки");
                    }
                })
                .listener(new ChunkListener() {
                    public void beforeChunk(@NonNull ChunkContext chunkContext) {
                        log.info("Начало пачки");
                    }

                    public void afterChunk(@NonNull ChunkContext chunkContext) {
                        log.info("Конец пачки");
                    }

                    public void afterChunkError(@NonNull ChunkContext chunkContext) {
                        log.info("Ошибка пачки");
                    }
                })
                .build();
    }

    @Bean
    public TaskletStep createBookTempTable(JobRepository jobRepository) {
        return new StepBuilder("createBookTempTable", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(((contribution, chunkContext) -> {
                    new JdbcTemplate(dataSource).execute(
                            "create table temp_book_ids (id_table bigint, id_document varchar(255))");
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }

    @Bean
    public TaskletStep dropBookTempTable(JobRepository jobRepository) {
        return new StepBuilder("dropBookTempTable", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(((contribution, chunkContext) -> {
                    new JdbcTemplate(dataSource).execute(
                            "drop table temp_book_ids");
                    return RepeatStatus.FINISHED;
                }), platformTransactionManager)
                .build();
    }
}

package ru.sm.lab.demo.step;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import ru.sm.lab.demo.config.AppProperties;
import ru.sm.lab.demo.converters.CommentConverter;
import ru.sm.lab.demo.models.mongo.Comment;
import ru.sm.lab.demo.models.record.CommentRecord;
import ru.sm.lab.demo.step.castom.reader.CustomCompositeItemReader;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CommentStepConfig {

    private static final int CHUNK_SIZE = 3;

    private final AppProperties appProperties;

    private final JobRepository jobRepository;

    private final PlatformTransactionManager platformTransactionManager;

    private final DataSource dataSource;

    private final MongoTemplate mongoTemplate;

    private final CommentConverter commentConverter;

    @Bean
    public JdbcCursorItemReader<CommentRecord> jdbcItemReader() {
        JdbcCursorItemReader<CommentRecord> reader = new JdbcCursorItemReader<>();
        reader.setName("commentItemReader");
        reader.setDataSource(dataSource);
        reader.setSql("select c.id, c.content, c.book_id from comments c");
        reader.setRowMapper((rs, rowNum) -> {
            long id = rs.getLong("id");
            String content = rs.getString("content");
            long bookId = rs.getLong("book_id");
            return new CommentRecord(id, content, bookId);
        });
        return reader;
    }

    @Bean
    public FlatFileItemReader<CommentRecord> csvItemReader() {
        return new FlatFileItemReaderBuilder<CommentRecord>()
                .name("csvItemReader")
                .resource(new ClassPathResource(appProperties.getCommentInitFile()))
                .delimited()
                .names("id", "content", "bookId")
                .fieldSetMapper(fieldSet -> new CommentRecord(fieldSet.readLong("id"),
                        fieldSet.readString("content"), fieldSet.readLong("bookId"))).build();
    }

    @Bean
    public CustomCompositeItemReader<CommentRecord> compositeItemReader(JdbcCursorItemReader<CommentRecord> jdbcItemReader,
                                                         FlatFileItemReader<CommentRecord> csvItemReader) {
        return new CustomCompositeItemReader<>(List.of(jdbcItemReader, csvItemReader));
    }

    @Bean
    public ItemProcessor<CommentRecord, Comment> commentProcessor() {
        return commentConverter::convertToDomain;
    }

    @Bean
    public MongoItemWriter<Comment> commentItemWriter() {
        return new MongoItemWriterBuilder<Comment>()
                .collection("comments")
                .template(mongoTemplate)
                .build();
    }

    @Bean
    public Step migrateCommentsStep(CustomCompositeItemReader<CommentRecord> reader,
                                   ItemProcessor<CommentRecord, Comment> processor,
                                   MongoItemWriter<Comment> writer) {
        return new StepBuilder("migrateCommentsStep", jobRepository)
                .<CommentRecord, Comment>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}

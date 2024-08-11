package ru.sm.lab.demo.step;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.MongoItemWriter;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import ru.sm.lab.demo.converters.AuthorConverter;
import ru.sm.lab.demo.models.jpa.AuthorJpa;
import ru.sm.lab.demo.models.mongo.Author;
import ru.sm.lab.demo.repositories.jpa.AuthorJpaRepository;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class AuthorStepConfig {

    private static final int CHUNK_SIZE = 3;
    private static final Map<String, Sort.Direction> SORTS = Map.of("id", Sort.Direction.ASC);

    private final JobRepository jobRepository;

    private final PlatformTransactionManager platformTransactionManager;

    private final MongoTemplate mongoTemplate;

    private final AuthorJpaRepository authorJpaRepository;

    private final AuthorConverter authorConverter;

    @Bean
    public ItemReader<AuthorJpa> authorItemReader() {
        return new RepositoryItemReaderBuilder<AuthorJpa>()
                .name("authorItemReader")
                .sorts(SORTS)
                .repository(authorJpaRepository)
                .methodName("findAll")
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    public ItemProcessor<AuthorJpa, Author> authorProcessor() {
        return authorConverter::convertToDomain;
    }

    @Bean
    public MongoItemWriter<Author> authorItemWriter() {
        return new MongoItemWriterBuilder<Author>()
                .collection("authors")
                .template(mongoTemplate)
                .build();
    }

    @Bean
    public Step migrateAuthorsStep(ItemReader<AuthorJpa> reader,
                                   ItemProcessor<AuthorJpa, Author> itemProcessor,
                                   MongoItemWriter<Author> writer) {
        return new StepBuilder("migrateAuthorsStep", jobRepository)
                .<AuthorJpa, Author>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(reader)
                .processor(itemProcessor)
                .writer(writer)
                .build();
    }
}

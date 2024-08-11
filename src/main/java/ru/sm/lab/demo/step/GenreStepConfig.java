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
import ru.sm.lab.demo.converters.GenreConverter;
import ru.sm.lab.demo.models.jpa.GenreJpa;
import ru.sm.lab.demo.models.mongo.Genre;
import ru.sm.lab.demo.repositories.jpa.GenreJpaRepository;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class GenreStepConfig {

    private static final int CHUNK_SIZE = 3;

    private static final Map<String, Sort.Direction> SORTS = Map.of("id", Sort.Direction.ASC);

    private final JobRepository jobRepository;

    private final PlatformTransactionManager platformTransactionManager;

    private final MongoTemplate mongoTemplate;

    private final GenreJpaRepository genreJpaRepository;

    private final GenreConverter genreConverter;

    @Bean
    public ItemReader<GenreJpa> genreItemReader() {
        return new RepositoryItemReaderBuilder<GenreJpa>()
                .name("genreItemReader")
                .sorts(SORTS)
                .repository(genreJpaRepository)
                .methodName("findAll")
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    public ItemProcessor<GenreJpa, Genre> genreProcessor() {
        return genreConverter::convertToDomain;
    }

    @Bean
    public MongoItemWriter<Genre> genreItemWriter() {
        return new MongoItemWriterBuilder<Genre>()
                .collection("genres")
                .template(mongoTemplate)
                .build();
    }

    @Bean
    public Step migrateGenresStep(ItemReader<GenreJpa> reader,
                                  ItemProcessor<GenreJpa, Genre> itemProcessor,
                                  MongoItemWriter<Genre> writer) {
        return new StepBuilder("migrateGenresStep", jobRepository)
                .<GenreJpa, Genre>chunk(CHUNK_SIZE, platformTransactionManager)
                .reader(reader)
                .processor(itemProcessor)
                .writer(writer)
                .build();
    }
}

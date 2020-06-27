package batch.jobs.inactive;

import batch.domain.ObjectKeyInfo;
import batch.jobs.inactive.listener.InactiveChunkListener;
import batch.jobs.inactive.processor.ObjectKeyFilterProcessor;
import batch.repository.ObjectKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.persistence.EntityManagerFactory;


@Configuration
@RequiredArgsConstructor
@ComponentScan("batch")
@Slf4j
public class SelectObjectKeyJobConfig {
    private final static int CHUNK_SIZE = 5;

    private final ObjectKeyRepository objectKeyRepository;

    private final EntityManagerFactory entityManagerFactory;

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final ObjectKeyFilterProcessor objectKeyFilterProcessor;

    private final InactiveChunkListener inactiveChunkListener;

    private Resource outputResource = new FileSystemResource("result.csv");

    @Bean
    public Job selectObjectKeyJobJob() {
        return jobBuilderFactory.get("selectObjectKeyJobJob")
                .incrementer(new RunIdIncrementer())
                .start(selectObjectKeyJobStep())
                .build();
    }

    @Bean
    public Step selectObjectKeyJobStep() {
        return stepBuilderFactory.get("selectObjectKeyJobStep")
                .<ObjectKeyInfo, ObjectKeyInfo> chunk(CHUNK_SIZE)
                .reader(jpaPagingItemReader())
                .processor(objectKeyFilterProcessor)
                .writer(fileWriter())
                .build();
    }

    @Bean
    public JpaPagingItemReader<ObjectKeyInfo> jpaPagingItemReader() {
        return new JpaPagingItemReaderBuilder<ObjectKeyInfo>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("select o from ObjectKeyInfo o")
                .build();
    }

    @Bean
    public JpaItemWriter<ObjectKeyInfo> inactiveUserWriter() {
        JpaItemWriter<ObjectKeyInfo> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    @Bean
    public FlatFileItemWriter<ObjectKeyInfo> fileWriter() {
        FlatFileItemWriter<ObjectKeyInfo> writer = new FlatFileItemWriter<>();

        writer.setResource(outputResource);
        //writer.setAppendAllowed(true);
        writer.setLineAggregator(new DelimitedLineAggregator<ObjectKeyInfo>() {
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor<ObjectKeyInfo>() {
                    {
                        setNames(new String[] {"objectKey", "lastModified"});
                    }
                });
            }
        });
        return writer;
    }



}

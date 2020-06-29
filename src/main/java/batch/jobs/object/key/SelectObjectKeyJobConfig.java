package batch.jobs.object.key;

import batch.domain.ObjectKeyInfo;
import batch.jobs.object.key.listener.ObjectKeyChunkListener;
import batch.jobs.object.key.listener.ObjectKeyStepListener;
import batch.jobs.object.key.processor.ObjectKeyFilterProcessor;
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
import org.springframework.batch.repeat.RepeatStatus;
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

    private final ObjectKeyChunkListener objectKeyChunkListener;

    private final ObjectKeyStepListener objectKeyStepListener;

    private Resource outputResource = new FileSystemResource("result.csv");

    @Bean
    public Job selectObjectKeyJob() {
        return jobBuilderFactory.get("selectObjectKeyJob")
                .incrementer(new RunIdIncrementer()) //동일 Job Parameter로 계속 실행이 될 수 있게끔
                .start(selectObjectKeyStep())
                .build();
    }

//    @Bean
//    public Job selectObjectKeyJob() {
//        return jobBuilderFactory.get("selectObjectKeyJob")
//                .incrementer(new RunIdIncrementer())
//                .start(selectObjectKeyStep())
//                .on("FAILED") // FAILED 일 경우
//                .to(step3()) // step3으로 이동한다.
//                .on("*") // step3의 결과 관계 없이
//                .end() // step3으로 이동하면 Flow가 종료한다.
//                .from(selectObjectKeyStep()) // step1로부터
//                .on("*") // FAILED 외에 모든 경우
//                .to(step2()) // step2로 이동한다.
//                .on("*") // step2의 결과 관계 없이
//                .end() // step2으로 이동하면 Flow가 종료한다.
//                .end() // Job 종료
//                .build();
//    }


    @Bean
    public Step selectObjectKeyStep() {
        return stepBuilderFactory.get("selectObjectKeyStep")
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

    @Bean
    public JpaItemWriter<ObjectKeyInfo> jpaPagingItemWriter() {
        JpaItemWriter<ObjectKeyInfo> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }

    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is Step2");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step step3() {
        return stepBuilderFactory.get("step3")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> This is Step3");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }



}

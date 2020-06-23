package batch.jobs.inactive;

import batch.domain.ObjectKeyInfo;
import batch.repository.ObjectKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.util.List;


@Configuration
@RequiredArgsConstructor
@ComponentScan("batch")
@Slf4j
public class InactiveUserJobConfig {
    private final static int CHUNK_SIZE = 5;

    private final ObjectKeyRepository objectKeyRepository;

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;


    @Bean
    public Step inactiveJobStep() {
        return stepBuilderFactory.get("inactiveUserStep")
                .<ObjectKeyInfo, ObjectKeyInfo> chunk(CHUNK_SIZE)
                .reader(inactiveUserReader())
                .processor(inactiveUserProcessor())
                .writer(inactiveUserWriter())
                .build();
    }

    @Bean
    public Job inactiveUserJob() {
        return jobBuilderFactory.get("inactiveUserJob")
                .start(inactiveJobStep())
                .build();
    }


//    @Bean
//    public Job inactiveUserJob(JobBuilderFactory jobBuilderFactory, InactiveIJobListener inactiveJobListener, Step inactiveJobStep) {
//        return jobBuilderFactory.get("inactiveUserJob")
//                .preventRestart()
//                .listener(inactiveJobListener)
//                .start(inactiveJobStep)
//                .build();
//    }


//    @Bean
//    public Job inactiveUserJob(JobBuilderFactory jobBuilderFactory, InactiveIJobListener inactiveJobListener, Step partitionerStep) {
//        return jobBuilderFactory.get("inactiveUserJob")
//                .preventRestart()
//                .listener(inactiveJobListener)
//                .start(partitionerStep)
//                .build();
//    }
//
//    @Bean
//    @JobScope
//    public Step partitionerStep(StepBuilderFactory stepBuilderFactory, Step inactiveJobStep) {
//        return stepBuilderFactory.get("partitionerStep")
//                .partitioner("partitionerStep", new InactivePartitioner())
//                .gridSize(5)
//                .step(inactiveJobStep)
//                .taskExecutor(taskExecutor())
//                .build();
//    }



//    @Bean
//    public Step inactiveJobStep(StepBuilderFactory stepBuilderFactory, ListItemReader<User> inactiveUserReader) {
//        return stepBuilderFactory.get("inactiveUserStep")
//                .<User, User> chunk(CHUNK_SIZE)
//                //.reader(inactiveUserJpaReader())
//                .reader(inactiveUserReader)
//                .processor(inactiveUserProcessor())
//                .writer(inactiveUserWriter())
//                //.listener(inactiveChunkListener)
//                //.listener(inativeStepListener)
//                //.taskExecutor(taskExecutor)
//                //.throttleLimit(2)
//                .build();
//    }

    /*@Bean
    public Step inactiveJobStep(StepBuilderFactory stepBuilderFactory, InactiveItemTasklet inactiveItemTasklet) {
        return stepBuilderFactory.get("inactiveUserTaskleyStep")
                .tasklet(inactiveItemTasklet)
                .build();
    }*/

    @Bean
    public TaskExecutor taskExecutor(){
        return new SimpleAsyncTaskExecutor("Batch_Task");
    }

//    @Bean
//    @StepScope
//    public ListItemReader<User> inactiveUserReader(@Value("#{jobParameters[nowDate]}") Date nowDate, UserRepository userRepository) {
//        LocalDateTime now = LocalDateTime.ofInstant(nowDate.toInstant(), ZoneId.systemDefault());
//        List<User> inactiveUsers = userRepository.findByUpdatedDateBeforeAndStatusEquals(now.minusYears(1), UserStatus.ACTIVE);
//        return new ListItemReader<>(inactiveUsers);
//    }

//    @Bean
//    @StepScope
//    public ListItemReader<User> inactiveUserReader(@Value("#{stepExecutionContext[grade]}") String grade, UserRepository userRepository) {
//        log.info(Thread.currentThread().getName());
//        List<User> inactiveUsers = userRepository.findByUpdatedDateBeforeAndStatusEqualsAndGradeEquals(LocalDateTime.now().minusYears(1),UserStatus.ACTIVE, Grade.valueOf(grade));
//        return new ListItemReader<>(inactiveUsers);
//    }


    @Bean
    public ListItemReader<ObjectKeyInfo> inactiveUserReader() {
        //LocalDateTime now = LocalDateTime.ofInstant(nowDate.toInstant(), ZoneId.systemDefault());
        List<ObjectKeyInfo> inactiveObjectKeyInfos = objectKeyRepository.findAll();
        //return new ListItemReader<>(inactiveUsers);
        return new ListItemReader<>(inactiveObjectKeyInfos);
    }

//    @Bean
//    @StepScope
//    public ListItemReader<User> inactiveUserReader() {
//        List<User> oldUsers =
//                userRepository.findByUpdatedDateBeforeAndStatusEquals(LocalDateTime.now().minusYears(1), UserStatus.ACTIVE);
//        return new ListItemReader<>(oldUsers);
//    }

//    @Bean(destroyMethod="")
//    @StepScope
//    public JpaPagingItemReader<User> inactiveUserJpaReader() {
//        JpaPagingItemReader<User> jpaPagingItemReader = new JpaPagingItemReader(){
//            @Override
//            public int getPage(){
//                return 0;
//            }
//        };
//        jpaPagingItemReader.setQueryString("select u from User as u where u.createdDate < :createdDate and u.status = :status");
//
//        LocalDateTime now = LocalDateTime.now();
//        Map<String, Object> map = new HashMap<>();
//        //LocalDateTime now = LocalDateTime.ofInstant(nowDate.toInstant(), ZoneId.systemDefault());
//        map.put("createdDate", now.minusYears(1));
//        map.put("status", UserStatus.ACTIVE);
//
//        jpaPagingItemReader.setParameterValues(map);
//        jpaPagingItemReader.setEntityManagerFactory(entityManagerFactory);
//        jpaPagingItemReader.setPageSize(15);
//        return jpaPagingItemReader;
//    }

    private InactiveItemProcessor inactiveUserProcessor() {
        return new InactiveItemProcessor();
    }

//    private JpaItemWriter<User> inactiveUserWriter() {
//        JpaItemWriter<User> jpaItemWriter = new JpaItemWriter<>();
//        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
//        return jpaItemWriter;
//    }

    private ItemWriter<ObjectKeyInfo> inactiveUserWriter() {

    }
}

package batch.jobs.inactive;

import batch.domain.User;
import batch.domain.enums.UserStatus;
import batch.jobs.inactive.listener.InactiveChunkListener;
import batch.jobs.inactive.listener.InactiveIJobListener;
import batch.jobs.inactive.listener.InativeStepListener;
import batch.jobs.inactive.listener.reader.QueueItemReader;
import batch.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Created by KimYJ on 2018-03-07
 */
@Configuration
@AllArgsConstructor
@ComponentScan("batch")
public class InactiveUserJobConfig {
    private final static int CHUNK_SIZE = 5;

  //  private final EntityManagerFactory entityManagerFactory;

    private UserRepository userRepository;

//    @Bean
//    public Job inactiveUserJob(JobBuilderFactory jobBuilderFactory, InactiveIJobListener inactiveIJobListener, Step inactiveJobStep) {
//        return jobBuilderFactory.get("inactiveUserJob")
//                .preventRestart()
//                .listener(inactiveIJobListener)
//                .start(inactiveJobStep)
//                .build();
//    }

    @Bean
    public Job inactiveUserJob(JobBuilderFactory jobBuilderFactory, Step inactiveJobStep) {
        return jobBuilderFactory.get("inactiveUserJob")
                .preventRestart()
                //.listener(inactiveIJobListener)
                .start(inactiveJobStep)
                .build();
    }

//    @Bean
//    public Step inactiveJobStep(StepBuilderFactory stepBuilderFactory, ListItemReader<User> inactiveUserReader, InactiveChunkListener inactiveChunkListener, InativeStepListener inativeStepListener, TaskExecutor taskExecutor) {
//        return stepBuilderFactory.get("inactiveUserStep")
//                .<User, User> chunk(CHUNK_SIZE)
//                .reader(inactiveUserReader)
//                .processor(inactiveUserProcessor())
//                .writer(inactiveUserWriter())
//                .listener(inactiveChunkListener)
//                .listener(inativeStepListener)
//                .taskExecutor(taskExecutor)
//                .throttleLimit(2)
//                .build();
//    }

    @Bean
    public Step inactiveJobStep(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("inactiveUserStep")
                .<User, User> chunk(CHUNK_SIZE)
                .reader(inactiveUserReader())
                //.reader(inactiveUserReader)
                .processor(inactiveUserProcessor())
                .writer(inactiveUserWriter())
                //.listener(inactiveChunkListener)
                //.listener(inativeStepListener)
                //.taskExecutor(taskExecutor)
                //.throttleLimit(2)
                .build();
    }

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

    @Bean
    @StepScope
    public QueueItemReader<User> inactiveUserReader() {
        //LocalDateTime now = LocalDateTime.ofInstant(nowDate.toInstant(), ZoneId.systemDefault());
        List<User> inactiveUsers = userRepository.findByUpdatedDateBeforeAndStatusEquals(LocalDateTime.now().minusYears(1), UserStatus.ACTIVE);
        //return new ListItemReader<>(inactiveUsers);
        return new QueueItemReader<>(inactiveUsers);
    }

    /*@Bean(destroyMethod="")
    @StepScope
    public JpaPagingItemReader<User> inactiveUserJpaReader(@Value("#{jobParameters[nowDate]}") Date nowDate) {
        JpaPagingItemReader<User> jpaPagingItemReader = new JpaPagingItemReader<>();
        jpaPagingItemReader.setQueryString("select u from User as u where u.createdDate < :createdDate and u.status = :status");

        Map<String, Object> map = new HashMap<>();
        LocalDateTime now = LocalDateTime.ofInstant(nowDate.toInstant(), ZoneId.systemDefault());
        map.put("createdDate", now.minusYears(1));
        map.put("status", UserStatus.ACTIVE);

        jpaPagingItemReader.setParameterValues(map);
        jpaPagingItemReader.setEntityManagerFactory(entityManagerFactory);
        jpaPagingItemReader.setPageSize(CHUNK_SIZE);
        return jpaPagingItemReader;
    }*/

    private InactiveItemProcessor inactiveUserProcessor() {
        return new InactiveItemProcessor();
    }

//    private JpaItemWriter<User> inactiveUserWriter() {
//        JpaItemWriter<User> jpaItemWriter = new JpaItemWriter<>();
//        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
//        return jpaItemWriter;
//    }

    private ItemWriter<User> inactiveUserWriter() {
        return userRepository::saveAll;
    }
}

# Spring Boot Batch


## 배치 어플리케이션의 특징 
- 단발성으로 대용량의 데이터를 처리하는 어플리케이션
- 데이터를 읽고(read) 가공한 후(process) 수정된 데이터를 다시 저장소에 저장(write)
- 실시간 처리가 어려운 대용량 데이터나 대규모 데이터일 경우에 배치 어플리케이션을 사용

## 배치 관련 객체 관계도

![batch_relation](./image/batch_relation.png)

### pom.xml 의존성 설정
  ```
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-batch</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
  ```    

### @EnableBatchProcessing

  - 스프링 부트 배치 스타터에 미리 정의된 설정들을 실행(JobBuilder, StepBuilder 등의 설정이 자동 주입)
  ```
      @SpringBootApplication
      @EnableBatchProcessing
      public class BatchApplication {
      	public static void main(String[] args) {
      		SpringApplication.run(BatchApplication.class, args);
      	}
      }
  ```
   

### Job
  > 배치 처리 과정을 하나의 단위로 만들어 표현한 객체
  
  - job 선언 예제  

  - ```
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
        public Job inactiveUserJob() {
            return jobBuilderFactory.get("inactiveUserJob")
                    .start(inactiveJobStep())
                    .build();
        }
    ```

### Step
 > Job을 처리하는 단위. 모든 Job에는 1개 이상의 Step이 있음
   
   - step 선언 예제
   
     ```
        public class InactiveUserJobConfig {
            private final static int CHUNK_SIZE = 5;
        
            private final UserRepository userRepository;
        
            private final JobBuilderFactory jobBuilderFactory;
        
            private final StepBuilderFactory stepBuilderFactory;
        
        
            @Bean
            public Step inactiveJobStep() {
                return stepBuilderFactory.get("inactiveUserStep")
                        .<User, User> chunk(CHUNK_SIZE)
                        .reader(inactiveUserReader())
                        .processor(inactiveUserProcessor())
                        .writer(inactiveUserWriter())
                        .build();
            }
        ```
        
  - CHUNK_SIZE : Writer 가 처리하는 트랜잭션 단위(commit interval)
    - Chunk 단위로 트랜잭션을 수행하기 때문에 실패할 경우엔 해당 Chunk 만큼만 롤백이 되고, 이전에 커밋된 트랜잭션 범위까지는 반영

### Reader, Processor, Writer 

#### Reader

  - ItemReader 인터페이스의 구현체
    - JdbcCursorItemReader
    - JdbcPagingItemReader
    - JpaPagingItemReader
    - ListItemReader
     




## 참고 자료
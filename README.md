# Spring Boot Batch


## batch application
-  단발성으로 대용량의 데이터를 처리하는 어플리케이션
-  데이터를 읽고(read) 가공한 후(process) 수정된 데이터를 다시 저장소에 저장(write)



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
    
        private final UserRepository userRepository;
    
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
        
  - CHUNK_SIZE : Reader & Writer가 묶일 Chunk 트랜잭션 범위


#### Reader, Processor, Writer 




## 참고 자료
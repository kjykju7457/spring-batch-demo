# Spring Boot Batch


## batch application
-  단발성으로 대용량의 데이터를 처리하는 어플리케이션
-  데이터를 읽고(read) 가공한 후(process) 수정된 데이터를 다시 저장소에 저장(write)


### 배치 관련 객체 관계도

![batch_relation](./image/batch_relation.png)




- Job
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

- Step
 > Job을 처리하는 단위. 모든 Job에는 1개 이상의 Step이 있음
   
   - step 선언 예제
   
 
      - ```
      
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
- CHUNK_SIZE :  Reader & Writer가 묶일 Chunk 트랜잭션 범위


#### thenCombine, thenAccept, thenApply, thenCompose



- thenCombine

  - 독립적인 2개의 작업을 병렬로 실행시키고 하나로 합치는 역할

    ```
    @Test
        public void thenCombineTest() {
            CommonUtils.exampleStart();
            Shop bestShop = new Shop("BestShop");
    
            CompletableFuture<String> futurePriceInUSD =
                    CompletableFuture.supplyAsync(() -> bestShop.getPrice("pencil"))    //thread1
                            .thenCombine(
                                    CompletableFuture.supplyAsync(
                                            () -> ExchangeService.getRate(ExchangeService.Money.EUR, ExchangeService.Money.USD)
                                    ),  //thread2
                                    (pencilPrice, rate) -> "pencil price(USD) : " + pencilPrice * rate   //thread1
                            );
    
            CommonUtils.exampleComplete();
            System.out.println(futurePriceInUSD.join());
            
        }
    ```

    

  - 실행 결과

    ```
    example start
    example end
    pencil price(USD) : 87.8984
    ```

    

-  thenAccept 

   - 비동기 연산의 결과를 받고 처리 후 아무 것도 반환하지 않을때 사용

    ```
    @Test
     public void resolvedListenableFuture() {
      
         CommonUtils.exampleStart();
         ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
      
         Shop bestShop = new Shop("BestShop");
      
         CompletableFuture<Void> futurePrice =
                 CompletableFuture.supplyAsync(() -> bestShop.getPrice("pencil")
                         , executorService)
                         .thenAccept(result -> {
                                     System.out.println("pencil price: " + result + " at bestShop[" + Thread.currentThread().getName() + "]");
                                 }
                         );
      
         CommonUtils.exampleComplete();
         CommonUtils.delay(3000);
         assertTrue(futurePrice.isDone());
     }
    ```

    

  - 실행 결과

    ```
    example start
    example end
    pencil price: 152.0 at bestShop[pool-1-thread-1]
    ```

    

  

- thenApply, thenCompose

  - thenApply : 비동기 연산 결과로 넘어온 값을 입력으로 받고 처리 후 반환

  - thenCompose : 하나의 *Future* 의 결과 값이 다음 *Future* 로 전달되어 처리 할 수 있도록 하는 조합 방식

  - ```
        @Test
        public void thenComposeTest() {
            Shop bestShop = new Shop("BestShop");
            CompletableFuture<Double> futurePrice =
                    CompletableFuture.supplyAsync(() -> bestShop.getPriceWithDisCountCode("pencil")) // shopName + ":" + price + ":" + code
                            .thenApply(DiscountInfo::parse)
                            .thenCompose(discountInfo ->
                                    CompletableFuture.supplyAsync(() -> DiscountService.applyDiscount(discountInfo))
                            );
    
            System.out.println("pencil price: " + futurePrice.join());
            assertTrue(futurePrice.isDone());
        }
    ```

  - 실행 결과 

    ```
    pencil price: 164.05
    ```


#### 예외 처리



- exceptionally 함수 (default 값 지정하고 다음 단계로 진행)

  - ```
        @Test
      	public void exceptionTestWithExceptionally() {
      		Shop bestShop = new Shop("BestShop");
    
          double defaultPrice = 100.0;
    
          CompletableFuture<Void> futurePrice =
              CompletableFuture.supplyAsync(() -> bestShop.getPrice("pencil"))
                      .thenApply(price -> Double.parseDouble(price + "x"))
                      .exceptionally(ex -> defaultPrice)
                      .thenAccept(Log::i);
    
         futurePrice.join();
         assertTrue(futurePrice.isDone());
    }
    ```

  - 실행 결과

    ```
    ForkJoinPool.commonPool-worker-9 | value = 100.0
    ```

- whenComplete 함수

  - ```
        @Test
        public void exceptionTestWithWhenComplete() {
            Shop bestShop = new Shop("BestShop");
    
            CompletableFuture<Double> futurePrice =
                    CompletableFuture.supplyAsync(() -> bestShop.getPrice("pencil"))
                            .thenApply(price -> Double.parseDouble(price + "x"))
                            .whenComplete((result, ex) -> {
                                if (ex != null) {
                                    throw new RuntimeException(ex);
                                } else {
                                    Log.i(result);
                                }
                            });
    
            assertThrows(RuntimeException.class, futurePrice::join);
        }
    ```

    

## Reactor 라이브러리

- [Pivotal](https://pivotal.io/)의 오픈소스 프로젝트로, JVM 위에서 동작하는 논블럭킹 애플리케이션을 만들기 위한 리액티브 라이브러리

- 발행-구독 패턴(publisher-subscriber)을 구현, 기존 pull 방식의 프로그래밍 개념을 push 방식의 프로그래밍 개념으로 바꿈

- 리액티브 스트림의 구현체

  - ```
    public interface Publisher<T> {
       public void subscribe(Subscriber<? super T> s);
    }
    
    public interface Subscription {
       public void request(long n);
       public void cancel();
    }
    
    public interface Subscriber<T> {
       public void onSubscribe(Subscription s);
       public void onNext(T t);
       public void onError(Throwable t);
       public void onComplete();
    }
    ```

- Publisher 인터페이스를 구현한 클래스 Mono, Flux 

  - Mono : 0 - 1개 의 결과만을 처리하기 위한 Reactor 의 객체
  - Flux : 0 - N개인 여러 개의 결과를 처리하는 Reactor 의 객체

- 2개의 작업을 병렬로 실행(스케줄러 활용)

  - ```
        @Test
        public void thenCombineToReactiveStream() {
    
            CommonUtils.exampleStart();
    
            Shop bestShop = new Shop("BestShop");
    
            Mono<Double> futurePrice = Mono.fromCallable(() -> bestShop.getPrice("pencil"))
                                            .subscribeOn(Schedulers.parallel())
                                            .doOnNext(Log::i);
            Mono<Double> EURtoUSD = Mono.fromCallable(() -> ExchangeService.getRate(ExchangeService.Money.EUR, ExchangeService.Money.USD))
                                            .subscribeOn(Schedulers.parallel())
                                            .doOnNext(Log::i);
            Flux.zip(
                    futurePrice,
                    EURtoUSD,
                    (pencilPrice, rate) -> pencilPrice * rate
            )
             .map(USDPrice -> "pencil price(USD) : " + USDPrice)
             .subscribe(Log::i);
    
            CommonUtils.exampleComplete();
            CommonUtils.delay(3000);
        }
    ```

  - 실행 결과

    ```
    example start
    example end
    parallel-1 | value = 210.0
    parallel-2 | value = 0.7386
    parallel-2 | value = pencil price(USD) : 155.106
    ```


## 참고 자료

- [자바 8 인 액션](http://www.yes24.com/Product/Goods/17252419) 11장 
- [RxJava 프로그래밍](http://www.yes24.com/Product/Goods/45506284) 5장
- [사용하면서 알게 된 Reactor, 예제 코드로 살펴보기](https://tech.kakao.com/2018/05/29/reactor-programming/)

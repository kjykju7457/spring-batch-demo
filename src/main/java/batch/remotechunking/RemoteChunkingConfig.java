package batch.remotechunking;

import batch.jobs.object.key.UniqueRunIdIncrementer;
import batch.jobs.object.key.listener.RemoteChunkingStepListener;
import batch.jobs.object.key.reader.CustomReader;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.item.ChunkProcessor;
import org.springframework.batch.core.step.item.SimpleChunkProcessor;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.ChunkMessageChannelItemWriter;
import org.springframework.batch.integration.chunk.ChunkProcessorChunkHandler;
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilderFactory;
import org.springframework.batch.integration.chunk.RemoteChunkingWorkerBuilder;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.ItemSqlParameterSourceProvider;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatCallback;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.ChannelPublishingJmsMessageListener;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.IntStream;

@Configuration
public class RemoteChunkingConfig {

    /*
     * Configure inbound flow (requests coming from the master)
     */
//    @Bean(name = "workerRequest")
//    public DirectChannel workerRequest() {
//        return new DirectChannel();
//    }
//
//    /*
//     * Configure outbound flow (replies going to the master)
//     */
//    @Bean(name = "workerReplies")
//    public DirectChannel workerReplies() {
//        return new DirectChannel();
//    }


//    /*
//     * Configure inbound flow (replies coming from workers)
//     */
//    @Bean(name = "replies")
//    public QueueChannel replies() {
//        return new QueueChannel();
//    }
//
//    /*
//     * Configure outbound flow (requests going to workers)
//     */
//    @Bean(name = "request")
//    public DirectChannel request() {
//        return new DirectChannel();
//    }

//    @Bean(name = "managerOutboundFlow")
//    public IntegrationFlow managerOutboundFlow(ActiveMQConnectionFactory connectionFactory) {
//        return IntegrationFlows
//                .from(managerRequest())
//                .handle(Jms.outboundAdapter(connectionFactory).destination("managerRequest"))
//                .get();
//    }
//
//    @Bean(name = "managerInboundFlow")
//    public IntegrationFlow managerInboundFlow(ActiveMQConnectionFactory connectionFactory) {
//        return IntegrationFlows
//                .from(Jms.messageDrivenChannelAdapter(connectionFactory).destination("managerReplies"))
//                .channel(managerReplies())
//                .get();
//    }


    @Configuration
    @EnableBatchIntegration
    @EnableIntegration
    @ConditionalOnProperty(name = "job.name", havingValue = "remoteChunkingJob")
    @Profile("manager")
    public static class ManagerConfig {

        @Autowired
        private JobBuilderFactory jobBuilderFactory;

        @Autowired
        private RemoteChunkingManagerStepBuilderFactory remoteChunkingManagerStepBuilderFactory;

        @Autowired
        private CustomReader customReader;

        @Autowired
        private RemoteChunkingStepListener remoteChunkingStepListener;


        @Value("${broker.url}")
        private String brokerUrl;

        @Bean
        public ActiveMQConnectionFactory connectionFactory() {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
            connectionFactory.setBrokerURL(this.brokerUrl);
            connectionFactory.setTrustAllPackages(true);
            return connectionFactory;
        }

        /*
         * Configure inbound flow (replies coming from workers)
         */
        @Bean(name = "replies")
        public QueueChannel replies() {
            return new QueueChannel();
        }

        /*
         * Configure outbound flow (requests going to workers)
         */
        @Bean(name = "request")
        public DirectChannel request() {
            return new DirectChannel();
        }

        @Bean(name = "inboundFlow")
        public IntegrationFlow inboundFlow(
                ActiveMQConnectionFactory connectionFactory) {
            return IntegrationFlows
                    .from(Jms.messageDrivenChannelAdapter(connectionFactory).destination("replies"))
                    .channel(replies())
                    .get();
        }

        @Bean(name = "outboundFlow")
        public IntegrationFlow outboundFlow(
                ActiveMQConnectionFactory connectionFactory) {
            return IntegrationFlows
                    .from(request())
                    .handle(Jms.outboundAdapter(connectionFactory).destination("request"))
                    .get();
        }

        /*
         * Configure master step components
         */

        @Bean
        public RepeatTemplate repeatTemplate(){
            RepeatTemplate template = new RepeatTemplate();
            template.setCompletionPolicy(new SimpleCompletionPolicy(4));

            template.iterate(new RepeatCallback() {

                @Override
                public RepeatStatus doInIteration(RepeatContext context) throws Exception {
                    return RepeatStatus.CONTINUABLE;
                }

            });
            return template;
        }

        @Bean
        public TaskletStep managerStep() {
            return this.remoteChunkingManagerStepBuilderFactory.get("managerStep")
                    .<Integer, Integer>chunk(5)
                    .reader(customReader)
                    .listener(remoteChunkingStepListener)
                    .stepOperations(repeatTemplate())
                    .outputChannel(request())
                    .inputChannel(replies())
                    .build();
        }


        @Bean
        public Job remoteChunkingJob() {
            return this.jobBuilderFactory.get("remoteChunkingJob")
                    .incrementer(new UniqueRunIdIncrementer())
                    .start(managerStep())
                    .on("CONTINUE")
                    .to(managerStep())
                    .on("FINISHED")
                    .end()
                    .end()
                    .build();
        }

    }

    @Configuration
    @EnableBatchIntegration
    @EnableIntegration
    @Profile("worker")
    //@ConditionalOnProperty(name = "job.name", havingValue = "workerRemoteChunkingJob")
    public static class WorkerConfig {

        @Autowired
        private RemoteChunkingWorkerBuilder<Integer, Integer> remoteChunkingWorkerBuilder;

        @Value("${broker.url}")
        private String brokerUrl;

        @Bean
        public ActiveMQConnectionFactory connectionFactory() {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
            connectionFactory.setBrokerURL(this.brokerUrl);
            connectionFactory.setTrustAllPackages(true);
            return connectionFactory;
        }

        /*
         * Configure inbound flow (replies coming from workers)
         */
        @Bean(name = "replies")
        public DirectChannel replies() {
            return new DirectChannel();
        }

        /*
         * Configure outbound flow (requests going to workers)
         */
        @Bean(name = "request")
        public DirectChannel request() {
            return new DirectChannel();
        }



        @Bean(name = "inboundFlow")
        public IntegrationFlow inboundFlow(
                ActiveMQConnectionFactory connectionFactory) {
            return IntegrationFlows
                    .from(Jms.messageDrivenChannelAdapter(connectionFactory).destination("request"))
                    .channel(request())
                    .get();
        }

        @Bean(name = "outboundFlow")
        public IntegrationFlow outboundFlow(
                ActiveMQConnectionFactory connectionFactory) {
            return IntegrationFlows
                    .from(replies())
                    .handle(Jms.outboundAdapter(connectionFactory).destination("replies"))
                    .get();
        }

        /*
         * Configure worker components
         */
        @Bean
        public ItemProcessor<Integer, Integer> itemProcessor() {
            return item -> {
                System.out.println("processing item " + item);
                return item;
            };
        }

        @Bean
        public ItemWriter<Integer> itemWriter() {
            return items -> {
                for (Integer item : items) {
                    System.out.println("writing item " + item);
                }
            };
        }

//        @Bean
//        public IntegrationFlow workerIntegrationFlow() {
//            return this.remoteChunkingWorkerBuilder
//                    .itemProcessor(itemProcessor())
//                    .itemWriter(itemWriter())
//                    .inputChannel(request())
//                    .outputChannel(replies())
//                    .build();
//        }
/*
 * Configure the ChunkProcessorChunkHandler
 */
@Bean
@ServiceActivator(inputChannel = "request", outputChannel = "replies")
public ChunkProcessorChunkHandler<Integer> chunkProcessorChunkHandler() {
    ChunkProcessor<Integer> chunkProcessor
            = new SimpleChunkProcessor<>(itemProcessor(), itemWriter());
    ChunkProcessorChunkHandler<Integer> chunkProcessorChunkHandler
            = new ChunkProcessorChunkHandler<>();
    chunkProcessorChunkHandler.setChunkProcessor(chunkProcessor);
    return chunkProcessorChunkHandler;
}

    }

}

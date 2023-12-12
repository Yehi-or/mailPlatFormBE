package com.bsh.mailplatformmailservice.config;

import com.bsh.mailplatformmailservice.partitioner.MailJobListener;
import com.bsh.mailplatformmailservice.partitioner.MailPartitioner;
import com.bsh.mailplatformmailservice.service.SendMailService;
import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Iterator;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PartitionConfig {
    @Value("${chunkSize:100}")
    private int chunkSize;
    @Value("${poolSize:5}")
    private int poolSize;
    private final String MAIL_JOB_NAME = "partitionBatchForMail";
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SendMailService sendMailService;

    @Bean(name = MAIL_JOB_NAME + "_taskPool")
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setThreadNamePrefix("mail-partition-thread");
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
        executor.initialize();
        return executor;
    }

    @Bean(name = MAIL_JOB_NAME + "_partitionHandler")
    public TaskExecutorPartitionHandler partitionHandler() {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(mailStep());
        partitionHandler.setTaskExecutor(executor());
        partitionHandler.setGridSize(poolSize);
        return partitionHandler;
    }

    @Bean(name = MAIL_JOB_NAME + "_partitioner")
    @StepScope
    public MailPartitioner partitioner(@Value("#{jobParameters['channelId']}") String channelId) {
        return new MailPartitioner(channelId, redisTemplate);
    }

    @Bean(name = MAIL_JOB_NAME)
    public Job partitionLocalMailBatch() {
        return jobBuilderFactory.get(MAIL_JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .start(mailStepManager())
                .listener(new MailJobListener(redisTemplate))
                .preventRestart()
                .build();
    }

    @Bean(name = MAIL_JOB_NAME + "_mailStepManager")
    public Step mailStepManager() {
        return stepBuilderFactory.get("mailStep.manager")
                .partitioner("mailStep", partitioner(null))
                .step(mailStep())
                .partitionHandler(partitionHandler())
                .build();
    }

    @Bean(name = MAIL_JOB_NAME + "_step")
    public Step mailStep() {
        return stepBuilderFactory.get(MAIL_JOB_NAME + "_step")
                .chunk(chunkSize)
                .reader(reader(null, null, null))
                .writer(writer(null, null))
                .build();
    }

    @Bean(name = MAIL_JOB_NAME + "_reader")
    @StepScope
    public ItemReader<Object> reader(@Value("#{jobParameters['channelId']}") String channelId, @Value("#{stepExecutionContext['start_index']}") Long startIndex
            , @Value("#{stepExecutionContext['end_index']}") Long endIndex) {
        if(startIndex != null && endIndex != null && channelId != null) {
            Set<Object> channelList;

            try {
                channelList = redisTemplate.opsForZSet().range(channelId, startIndex, endIndex);
            } catch(RedisException e) {
                //스탭이 실패를 한다고 이미 처리된 스탭들을 중복 처리 할 수는 없으니 db 에 값을 저장하고 후 처리를 해줄 예정
                log.error("redis range error : {} db save", e.toString());
                return null;
            }

            if(channelList != null) {
                log.info("reader size : {}", channelList.size());
                Iterator<Object> mailIterator = channelList.iterator();
                return () -> mailIterator.hasNext() ? mailIterator.next() : null;
            }
        }
        return null;
    }

    @Bean(name = MAIL_JOB_NAME + "_writer")
    @StepScope
    public ItemWriter<Object> writer(@Value("#{jobParameters['mailTemplate']}") String mailTemplate, @Value("#{jobParameters['mailTitle']}") String mailTitle) {
        return items -> {
            for (Object item : items) {
                String userEmail = item.toString();
                sendMailService.sendEmail(mailTitle, userEmail, mailTemplate);
            }
        };
    }

}

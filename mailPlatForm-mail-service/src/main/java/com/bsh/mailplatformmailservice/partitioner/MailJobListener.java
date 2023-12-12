package com.bsh.mailplatformmailservice.partitioner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
@RequiredArgsConstructor
public class MailJobListener implements JobExecutionListener {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("before job : {}", jobExecution.toString());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            JobParameters jobParameters = jobExecution.getJobParameters();
            String channelId = jobParameters.getString("channelId");
            assert channelId != null;
            redisTemplate.opsForZSet().incrementScore("channel_id_update", channelId, 1);
            log.info("finish job");
        }
    }
}

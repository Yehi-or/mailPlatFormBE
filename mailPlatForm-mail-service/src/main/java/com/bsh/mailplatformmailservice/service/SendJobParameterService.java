package com.bsh.mailplatformmailservice.service;

import com.bsh.mailplatformmailservice.dto.KafkaPostDTO;
import com.bsh.mailplatformmailservice.entity.DltDupProcess;
import com.bsh.mailplatformmailservice.repository.DltDuplicationProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendJobParameterService {
    private final JobLauncher jobLauncher;
    private final Job partitionLocalMailBatch;
    private final DltDuplicationProcessRepository dltDuplicationProcessRepository;

    @Async
    public void sendJobParameterManualCommit(KafkaPostDTO kafkaPostDTO, Acknowledgment acknowledgment, RedisTemplate<String, Object> redisTemplate)
            throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        sendJobParameter(kafkaPostDTO, redisTemplate);
        acknowledgment.acknowledge();
    }

    @Transactional
    public void sendJobParameter(KafkaPostDTO kafkaPostDTO, RedisTemplate<String, Object> redisTemplate)
            throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        Optional<String> uuid = dltDuplicationProcessRepository.findByUuid(kafkaPostDTO.getUuid().toString());

        if(uuid.isEmpty()) {
            dltDuplicationProcessRepository.save(DltDupProcess.builder()
                    .uuid(kafkaPostDTO.getUuid().toString())
                    .build());

            //mail sender 처리
            Double score = redisTemplate.opsForZSet().score("channel_id_update", kafkaPostDTO.getChannelId());
            long updateNumber;

            if(score == null) { score = 1.0; }
            updateNumber = score.longValue();

            //구독자가 있을 경우
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("channelId", kafkaPostDTO.getChannelId())
                    .addLong("updateNumber", updateNumber)
                    .addString("mailTemplate", kafkaPostDTO.getMessage())
                    .addString("mailTitle", kafkaPostDTO.getTitle())
                    .toJobParameters();

            jobLauncher.run(partitionLocalMailBatch, jobParameters);
        }
    }
}

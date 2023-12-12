package com.bsh.mailplatformmailservice.service;

import com.bsh.mailplatformmailservice.dto.KafkaSubDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaveMailService {
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${REDIS_MAIL_FRONT}")
    private String REDIS_MAIL_FRONT;

    //구독
    @Async
    public void saveMailToRedis(KafkaSubDTO kafkaSubDTO, Acknowledgment acknowledgment) {
        String redisKey = REDIS_MAIL_FRONT + kafkaSubDTO.getChannelId();
        redisTemplate.opsForSet().add(redisKey, kafkaSubDTO.getSubId());
        acknowledgment.acknowledge();
    }

    @Async
    public void cancelSubscription(KafkaSubDTO kafkaSubDTO, Acknowledgment acknowledgment) {
        String redisKey = REDIS_MAIL_FRONT + kafkaSubDTO.getChannelId();
        redisTemplate.opsForSet().remove(redisKey, kafkaSubDTO.getSubId());
        acknowledgment.acknowledge();
    }

    //구독취소

}

package com.bsh.mailplatformmainservice.service;

import com.bsh.mailplatformmainservice.dto.KafkaSubDTO;
import com.bsh.mailplatformmainservice.dto.ReturnSubDTO;
import com.bsh.mailplatformmainservice.dto.SubscriptionLockSaveDTO;
import com.bsh.mailplatformmainservice.entity.Subscribe;
import com.bsh.mailplatformmainservice.entity.User;
import com.bsh.mailplatformmainservice.repository.SubscribeRepository;
import com.bsh.mailplatformmainservice.repository.UserRepository;
import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SubscribeRepository subscribeRepository;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;

    @Value("${REDIS_CHANNEL}")
    private String redisChannel;
    @Value("${LOCKWAITTIME}")
    private long lockWaitTime;
    @Value("${LOCKLEASETIME}")
    private long lockLeaseTime;

    /**
     * 구독 처리에 redis 분산 락 적용 -> 본인이 구독, 구독 취소를 누를 때 순차적으로 처리하기 위해 (특정 채널에 구독, 구독 취소를 여러번 눌렀을 때 요청에 따라 정확한 처리를 위해 마지막 처리에 구독을 한지 구독취소를 한지)
     * 그리고 혹시나 구독 버튼을 따닥 눌렀을 때를 대비해서 구독자 + 구독 채널 복합키 유니크 설정을 해둠 -> 제약조건에 의해 구독이 2번 안되게
     * 삭제 2번 요청은 알아서 처리
     * **/

    @Async
    @Transactional(value = "transactionManager")
    public CompletableFuture<ReturnSubDTO> subscription(Long subId, Long channelId, boolean notification, UUID uuid) {
        SubscriptionLockSaveDTO subscriptionLockSaveDTO = subscriptionLockSave(subId, channelId, true);

        if(subscriptionLockSaveDTO.getSubName() != null) {
            KafkaSubDTO kafkaSubDTO = KafkaSubDTO.builder()
                    .subId(subscriptionLockSaveDTO.getSubName())
                    .channelId(subscriptionLockSaveDTO.getChannelName())
                    .uuid(uuid)
                    .build();
            try {
                sendTransactionalMessage("subscription", kafkaSubDTO, notification, channelId);
                return CompletableFuture.completedFuture(returnSubDTO(true, "success", true));
            } catch(Exception e) {
                log.error("Subscription Kafka Error : {}", e.toString());
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return CompletableFuture.completedFuture(returnSubDTO(false, "rollback",true));
            }
        }
        return CompletableFuture.completedFuture(returnSubDTO(false, "error",false));
    }

    @Async
    @Transactional(value = "transactionManager")
    public CompletableFuture<ReturnSubDTO> cancelSubscription(Long subId, Long channelId, UUID uuid) {
        SubscriptionLockSaveDTO subscriptionLockSaveDTO = subscriptionLockSave(subId, channelId, false);

        if(subscriptionLockSaveDTO.getSubName() != null) {
            KafkaSubDTO kafkaSubDTO = KafkaSubDTO.builder()
                    .subId(subscriptionLockSaveDTO.getSubName())
                    .channelId(subscriptionLockSaveDTO.getChannelName())
                    .uuid(uuid)
                    .build();
            try {
                sendTransactionalMessage("cancelSubscription", kafkaSubDTO, false, null);
                return CompletableFuture.completedFuture(returnSubDTO(true, "success", true));
            } catch(Exception e) {
                log.error("Subscription Kafka Error : {}", e.toString());
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return CompletableFuture.completedFuture(returnSubDTO(false, "rollback",true));
            }
        }
        return CompletableFuture.completedFuture(returnSubDTO(false, "error",false));
    }

    private ReturnSubDTO returnSubDTO(boolean successSub, String returnMessage, boolean lock) {
        return ReturnSubDTO.builder()
                .returnMessage(returnMessage)
                .successSub(successSub)
                .lock(lock)
                .build();
    }

    private SubscriptionLockSaveDTO subscriptionLockSave(Long subId, Long channelId, boolean saveOrDelete) {

        RLock lock = redissonClient.getLock("subscription-" + subId + "-" + channelId);

        try {
            boolean isLocked = lock.tryLock(lockWaitTime, lockLeaseTime, TimeUnit.SECONDS);
            log.info("isLock : {}", isLocked);

            if(!isLocked) {
                return SubscriptionLockSaveDTO.builder()
                        .subName(null)
                        .channelName(null)
                        .build();
            }

            User subUser = userRepository.findById(subId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            User channelUser = userRepository.findById(channelId)
                    .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

            Subscribe subscribe = Subscribe.builder()
                    .sub(subUser)
                    .pub(channelUser)
                    .notification(true)
                    .build();

            // 구독 디비 저장
            if(saveOrDelete) {
                subscribeRepository.save(subscribe);
            } else {
                subscribeRepository.delete(subscribe);
            }

            log.info("success save or delete sub");

            return SubscriptionLockSaveDTO.builder()
                    .subName(subUser.getUserEmail())
                    .channelName(channelUser.getUserEmail())
                    .build();

        } catch (Exception e) {
            log.error("sub lock error : {}", e.toString());
                //굳이 에러를 전파해서 롤백 시킬 필요없음 빈 null 객체 리턴
                return SubscriptionLockSaveDTO.builder()
                        .subName(null)
                        .channelName(null)
                        .build();
        } finally {
            lock.unlock();
        }
    }

    @Transactional(value = "kafkaTransactionManager")
    public void sendTransactionalMessage(String topic, KafkaSubDTO kafkaSubDTO, boolean notification, Long channelId) throws KafkaException, RedisException {
        kafkaTemplate.send(topic, kafkaSubDTO);
        if(notification) {
            String redisKey = redisChannel + channelId;
            Long incrementValue = redisTemplate.opsForValue().increment(redisKey, 1);
            log.info("success Subscription Number for channelId : {}, {}", channelId, incrementValue);
        }
    }

}

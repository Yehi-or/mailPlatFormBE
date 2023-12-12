package com.bsh.mailplatformmainservice.controller;

import com.bsh.mailplatformmainservice.dto.CancelSubscriptionDTO;
import com.bsh.mailplatformmainservice.dto.ReturnSubDTO;
import com.bsh.mailplatformmainservice.dto.SubscriptionDTO;
import com.bsh.mailplatformmainservice.service.SubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/main-service/sub")
public class SubController {
    private final SubService subService;

    /**
     * 비동기를 쓰는 이유가 병렬처리를 위해서인데 메서드 들 안에 특정 작업들이 있을 때 그 작업들을 병렬 처리함으로써 속도를 높이는 건데
     * 현재는 굳이 subscribe 메서드들을 비동기 메서드로 만들 이유가 없는것 같음 걸 부분이 한 곳 있는데 한번 테스트 해봐야 알듯 (2023-11-08)
     * 분산 락 적용시켜서 데이터 일관성 맞출예정
     * **/

    @PostMapping("/trySubscription")
    public ResponseEntity<ReturnSubDTO> subscribe(@RequestBody SubscriptionDTO subscriptionDTO) {
        log.info("Subscriber : {}, Channel Id : {}", subscriptionDTO.getSubId(), subscriptionDTO.getChannelId());

        Long subId = subscriptionDTO.getSubId();
        Long channelId = subscriptionDTO.getChannelId();
        boolean notification = subscriptionDTO.isNotification();

        if(subId != null && channelId != null) {
            try {
                CompletableFuture<ReturnSubDTO> subOperation = subService.subscription(subId, channelId, notification, subscriptionDTO.getUuid());
                ReturnSubDTO returnSubDTO = subOperation.get();
                return ResponseEntity.ok().body(returnSubDTO);
            } catch(Exception e) {
                log.info("sub method Error : {}", e.toString());
                return ResponseEntity.status(500).body(ReturnSubDTO.builder().build());
            }
        }

        return ResponseEntity.status(500).body(ReturnSubDTO.builder()
                .returnMessage("NULL_VALUE")
                .build());
    }

    //분산 락 적용시켜서 데이터 일관성 맞출예정
    @DeleteMapping("/cancelSubscription")
    public ResponseEntity<ReturnSubDTO> cancelSubscribe(@RequestBody CancelSubscriptionDTO cancelSubscriptionDTO) {
        log.info("Subscriber : {}, Channel Id : {}", cancelSubscriptionDTO.getSubId(), cancelSubscriptionDTO.getChannelId());

        Long subId = cancelSubscriptionDTO.getSubId();
        Long channelId = cancelSubscriptionDTO.getChannelId();

        if(subId != null && channelId != null) {
            try {
                CompletableFuture<ReturnSubDTO> subOperation = subService.cancelSubscription(subId, channelId, cancelSubscriptionDTO.getUuid());
                ReturnSubDTO returnSubDTO = subOperation.get();

                //처리 바꿔야함 에러가 올수도 있음
                return ResponseEntity.ok().body(returnSubDTO);
            } catch(Exception e) {
                return ResponseEntity.status(500).body(ReturnSubDTO.builder().build());
            }
        }

        return ResponseEntity.status(500).body(ReturnSubDTO.builder()
                .returnMessage("NULL_VALUE")
                .build());
    }
}

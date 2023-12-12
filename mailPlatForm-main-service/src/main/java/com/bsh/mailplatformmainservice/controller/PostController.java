package com.bsh.mailplatformmainservice.controller;

import com.bsh.mailplatformmainservice.dto.KafkaPostDTO;
import com.bsh.mailplatformmainservice.dto.PostDTO;
import com.bsh.mailplatformmainservice.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/main-service/post")
public class PostController {

    private final PostService postService;

    @PostMapping("/post")
    public ResponseEntity<Void> post(@RequestBody PostDTO postDTO) {
        boolean successOrFail = postService.tryPost(postDTO);

        if(successOrFail) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(500).build();
    }

    @RetryableTopic(
            backoff = @Backoff(value = 3000L),
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            exclude = { IllegalArgumentException.class, NullPointerException.class } )
    @KafkaListener(topics = "mail.dlt", groupId = "mail.dlt-group", containerFactory = "kafkaListenerMailDltContainerFactory")
    public void sendingMailDlt(KafkaPostDTO kafkaPostDTO) {
        log.info("update mail success or fail");
        postService.updateSuccessOrFail(kafkaPostDTO);
    }

    @DltHandler
    public void retryMailDlt() {
        log.info("최종적으로 mail.dlt db 에 값 저장(업데이트 x)");
    }
}

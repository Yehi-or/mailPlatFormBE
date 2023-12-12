package com.bsh.mailplatformmainservice.service;

import com.bsh.mailplatformmainservice.component.RedisLockRepository;
import com.bsh.mailplatformmainservice.dto.KafkaPostDTO;
import com.bsh.mailplatformmainservice.dto.PostDTO;
import com.bsh.mailplatformmainservice.entity.Post;
import com.bsh.mailplatformmainservice.entity.User;
import com.bsh.mailplatformmainservice.repository.PostRepository;
import com.bsh.mailplatformmainservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {
    private final RedisLockRepository redisLockRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Value("${POSTLOCKNAME}")
    private String lockName;

    @Transactional(value = "transactionManager")
    public boolean tryPost(PostDTO postDTO) {
        try {
            boolean successLock = redisLockRepository.lock(lockName + postDTO.getUserId());

            if(successLock) {
                User user = userRepository.findById(postDTO.getUserId())
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

                Post post = Post.builder()
                        .postUser(user)
                        .success(true)
                        .html(postDTO.getHtml())
                        .postDate(postDTO.getDate())
                        .title(postDTO.getTitle())
                        .build();

                postRepository.save(post);

                KafkaPostDTO kafkaPostDTO = KafkaPostDTO.builder()
                        .postId(post.getPostId())
                        .channelId(user.getUserEmail())
                        .title(postDTO.getTitle())
                        .message(postDTO.getHtml())
                        .uuid(postDTO.getUuid())
                        .build();

                log.info("channelId : {}, title : {}", user.getUserEmail(), postDTO.getTitle());
                kafkaTemplate.send("mail", kafkaPostDTO);
            }

        } catch(Exception e) {
            if(e instanceof KafkaException) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            return false;
        } finally {
            redisLockRepository.unlock(lockName + postDTO.getUserId());
        }

        return false;
    }

    public void updateSuccessOrFail(KafkaPostDTO kafkaPostDTO) {
        Post post = postRepository.findById(kafkaPostDTO.getPostId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        post.setSuccess(false);
        postRepository.save(post);
    }

}

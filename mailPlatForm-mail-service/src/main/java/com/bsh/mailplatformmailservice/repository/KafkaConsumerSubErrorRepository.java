package com.bsh.mailplatformmailservice.repository;

import com.bsh.mailplatformmailservice.entity.KafkaConsumerSubError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KafkaConsumerSubErrorRepository extends JpaRepository<KafkaConsumerSubError, Long> {
}

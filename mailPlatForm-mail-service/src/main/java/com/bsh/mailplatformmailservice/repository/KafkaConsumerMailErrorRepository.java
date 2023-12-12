package com.bsh.mailplatformmailservice.repository;

import com.bsh.mailplatformmailservice.entity.KafkaConsumerMailError;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KafkaConsumerMailErrorRepository extends JpaRepository<KafkaConsumerMailError, Long> {
}

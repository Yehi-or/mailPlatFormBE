package com.bsh.mailplatformmailservice.repository;

import com.bsh.mailplatformmailservice.entity.DltDupProcess;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DltDuplicationProcessRepository extends JpaRepository<DltDupProcess, Long> {
    @Query("SELECT d.uuid from DltDupProcess d where d.uuid = :uuid")
    Optional<String> findByUuid(@Param("uuid") String uuid);


}

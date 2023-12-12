package com.bsh.mailplatformmainservice.repository;

import com.bsh.mailplatformmainservice.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
}

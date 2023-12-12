package com.bsh.mailplatformmainservice.repository;

import com.bsh.mailplatformmainservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

package com.bsh.mailplatformmainservice.repository;

import com.bsh.mailplatformmainservice.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}

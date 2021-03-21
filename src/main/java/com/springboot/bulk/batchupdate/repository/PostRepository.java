package com.springboot.bulk.batchupdate.repository;

import com.springboot.bulk.batchupdate.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
}

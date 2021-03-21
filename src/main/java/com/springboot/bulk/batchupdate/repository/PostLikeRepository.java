package com.springboot.bulk.batchupdate.repository;

import com.springboot.bulk.batchupdate.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Integer> {
    @Query("select id, createTime from PostLike where likeCnt = :likeCnt")
    List<Object[]> testDynamicFetchField(@Param("likeCnt") Integer likeCnt);
}

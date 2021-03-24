package com.springboot.bulk.batchupdate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.springboot.bulk.batchupdate.model.PostLike;
import com.springboot.bulk.batchupdate.repository.PostLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/post-like")
public class PostLikeController {
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private Integer BATCH_SIZE;

    @Autowired
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PostLikeRepository postLikeRepository;

    @GetMapping("/batch-insert")
    public ObjectNode batchInsert() {
        ObjectNode objectNode = objectMapper.createObjectNode();

        List<PostLike> postLikeBatchList = new ArrayList<>();
        Random rand = new Random();
        for(int i = 0; i < BATCH_SIZE; i++) {
            PostLike postLike = new PostLike();
            postLike.setLikeCnt(rand.nextInt(50));
            postLikeBatchList.add(postLike);
        }
        postLikeRepository.saveAll(postLikeBatchList);
        postLikeBatchList.clear();

        objectNode.put("status", "SUCCESS");
        return objectNode;
    }

    @GetMapping("/batch-update")
    public ObjectNode batchUpdate() {
        ObjectNode objectNode = objectMapper.createObjectNode();

        List<PostLike> postLikeBatchList = new ArrayList<>();
        List<PostLike> postLikeList = postLikeRepository.findAll();
        Random rand = new Random();
        for(PostLike postLike : postLikeList) {
            postLike.setLikeCnt(rand.nextInt(50));
            postLikeBatchList.add(postLike);
        }
        postLikeRepository.saveAll(postLikeBatchList);
        postLikeBatchList.clear();

        objectNode.put("status", "SUCCESS");
        return objectNode;
    }

    @GetMapping("/test/{likeCnt}")
    public ObjectNode test(@PathVariable Integer likeCnt) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        List<Object[]> postList = postLikeRepository.testDynamicFetchField(likeCnt);
        String id = String.valueOf(postList.get(0)[0]);
        LocalDateTime createTime = (LocalDateTime) postList.get(0)[1];
        objectNode.put("id", id);
        objectNode.put("createTime", createTime.toString());
        return objectNode;
    }

    @GetMapping("/test2/{likeCnt}")
    public ObjectNode test2(@PathVariable Integer likeCnt) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        TypedQuery<Tuple> query = em.createQuery("select p.id as id, p.createTime as createTime from PostLike p where likeCnt = :likeCnt", Tuple.class);
        query.setParameter("likeCnt", likeCnt);
        List<Tuple> tupleList = query.getResultList();
        Tuple tuple = tupleList.get(0);
        objectNode.put("id", tuple.get("id", Integer.class).toString());
        objectNode.put("createTime", tuple.get("createTime", LocalDateTime.class).toString());
        return objectNode;
    }

    @GetMapping("/test3/{likeCnt}")
    public ObjectNode test3(@PathVariable Integer likeCnt) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        List<PostLike> postLikeList = postLikeRepository.findAll();
        for(PostLike postLike : postLikeList) {
            postLike.setLikeCnt(likeCnt);
        }
        postLikeRepository.saveAll(postLikeList);
        objectNode.put("status", "success");
        return objectNode;
    }

}

package com.springboot.bulk.batchupdate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.springboot.bulk.batchupdate.dto.PostDto;
import com.springboot.bulk.batchupdate.model.Post;
import com.springboot.bulk.batchupdate.model.PostLike;
import com.springboot.bulk.batchupdate.repository.PostLikeRepository;
import com.springboot.bulk.batchupdate.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@RestController
@RequestMapping("/post")
public class PostController {
    @Autowired
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PostLikeRepository postLikeRepository;

    @PostMapping("/save")
    public String create(@RequestBody PostDto postDto) {
        Post post = new Post();
        post.setContent(postDto.getContent());
        PostLike postLike = postLikeRepository.getOne(2);
        post.setPostLike(postLike);
        postRepository.save(post);
        return "success";
    }

    @Transactional
    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id, @RequestBody PostDto postDto) {
        Post post = em.find(Post.class, id);
        if(post == null) {
            return "no record";
        }
        post.setContent(postDto.getContent());
        em.flush();
        return "success";
    }

    @PostMapping("/test1/{id}")
    public ObjectNode test3(@PathVariable Integer id, @RequestBody PostDto postDto) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        Post post = postRepository.getOne(id);
        post.setContent(postDto.getContent());
        postRepository.save(post);
        objectNode.put("status", "SUCCESS");
        return objectNode;
    }
}

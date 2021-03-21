package com.springboot.bulk.batchupdate.repository;

import com.springboot.bulk.batchupdate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
}

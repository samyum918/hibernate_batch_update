package com.springboot.bulk.batchupdate.repository;

import com.springboot.bulk.batchupdate.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    long countByStatus(String status);
}

package com.springboot.bulk.batchupdate.repository;

import com.springboot.bulk.batchupdate.model.SettlementBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, Integer> {
}

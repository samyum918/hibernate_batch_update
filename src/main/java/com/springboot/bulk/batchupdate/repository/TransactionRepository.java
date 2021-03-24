package com.springboot.bulk.batchupdate.repository;

import com.springboot.bulk.batchupdate.model.Merchant;
import com.springboot.bulk.batchupdate.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    long countByStatus(String status);

    @Query("select t from Transaction t where t.merchant = :merchant and t.status = 'POSTED'")
    List<Transaction> findAllByMerchant(@Param("merchant") Merchant merchant);
}

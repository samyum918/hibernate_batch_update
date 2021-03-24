package com.springboot.bulk.batchupdate.repository;

import com.springboot.bulk.batchupdate.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Integer> {
}

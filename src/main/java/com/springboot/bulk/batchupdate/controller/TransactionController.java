package com.springboot.bulk.batchupdate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.bulk.batchupdate.model.Merchant;
import com.springboot.bulk.batchupdate.model.SettlementBatch;
import com.springboot.bulk.batchupdate.model.Transaction;
import com.springboot.bulk.batchupdate.repository.MerchantRepository;
import com.springboot.bulk.batchupdate.repository.SettlementBatchRepository;
import com.springboot.bulk.batchupdate.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/transaction")
public class TransactionController {
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private Integer BATCH_SIZE;

    @Autowired
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MerchantRepository merchantRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    SettlementBatchRepository settlementBatchRepository;

    //batch insert is not available because of GenerationType.IDENTITY
    @PostMapping("/create/{cnt}")
    public String create(@PathVariable Integer cnt) {
        List<Transaction> transactionList = new ArrayList<>();
        Random rand = new Random();
        for(int i=0; i<cnt; i++) {
            Transaction transaction = new Transaction();
            transaction.setCurrency("USD");
            transaction.setStatus("POSTED");
            transaction.setAmount(BigDecimal.valueOf(rand.nextInt(100)));
            transactionList.add(transaction);
            if(transactionList.size() >= BATCH_SIZE) {
                transactionRepository.saveAll(transactionList);
                transactionList.clear();
            }
        }
        if(transactionList.size() > 0) {
            transactionRepository.saveAll(transactionList);
            transactionList.clear();
        }

        log.info("Create success");
        return "success";
    }

    @PostMapping("/settle")
    public String settle() {
        long merchantCnt = merchantRepository.count();
        long postedTrxCnt = transactionRepository.countByStatus("POSTED");
        int recordPerPage = (int) Math.ceil((double) postedTrxCnt / merchantCnt);

        List<Merchant> merchantList = merchantRepository.findAll();
        for(int i=0; i<merchantCnt; i++) {
            Page<Transaction> transactionPage = transactionRepository.findAll(PageRequest.of(i, recordPerPage));
            List<Transaction> transactionList = transactionPage.getContent();

            if(transactionList.size() > 0) {
                BigDecimal totalAmount = transactionList.stream().map(t -> t.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);

                Merchant merchant = merchantList.get(i);
                merchant.setBatchNumber(merchant.getBatchNumber() + 1);
                merchantRepository.save(merchant);

                SettlementBatch settlementBatch = new SettlementBatch();
                settlementBatch.setBatchNumber(merchant.getBatchNumber());
                settlementBatch.setCurrency(transactionList.get(0).getCurrency());
                settlementBatch.setTotalAmount(totalAmount);
                settlementBatch.setTotalTransaction(BigDecimal.valueOf(transactionList.size()));
                settlementBatchRepository.save(settlementBatch);

                for(Transaction transaction : transactionList) {
                    transaction.setSettlementBatch(settlementBatch);
                    transaction.setBatchNumber(merchant.getBatchNumber());
                    transaction.setStatus("SETTLED");
                }
                transactionRepository.saveAll(transactionList);
            }
        }

        log.info("Settle success");
        return "success";
    }

    @DeleteMapping("/clear")
    public String clear() {
        List<Transaction> transactionList = transactionRepository.findAll();
        for(Transaction transaction : transactionList) {
            transaction.setStatus("POSTED");
            transaction.setSettlementBatch(null);
        }
        transactionRepository.saveAll(transactionList);
        settlementBatchRepository.deleteAll();

        log.info("Clear success");
        return "success";
    }
}

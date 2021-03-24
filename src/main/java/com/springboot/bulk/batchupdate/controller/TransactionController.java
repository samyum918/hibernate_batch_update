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
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/transaction")
public class TransactionController {
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private Integer BATCH_SIZE;

    @PersistenceContext
    EntityManager em;

    @Autowired
    private TransactionTemplate transactionTemplate;

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

        Long merchantCnt = merchantRepository.count();
        Random rand = new Random();
        for(int i=0; i<cnt; i++) {
            Transaction transaction = new Transaction();
            transaction.setCurrency("USD");
            transaction.setStatus("POSTED");
            transaction.setMerchant(em.getReference(Merchant.class, rand.nextInt(merchantCnt.intValue()) + 1));
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

    @PostMapping("/create-parallel/{cnt}")
    public String createParallel(@PathVariable Integer cnt) throws InterruptedException {
        int threadCount = 5;
        int recordsPerThread = cnt / threadCount;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch awaitTermination = new CountDownLatch(threadCount);
        List<Callable<Void>> tasks = new ArrayList<>();

        Long merchantCnt = merchantRepository.count();
        Random rand = new Random();
        for (int i = 0; i < threadCount; i++) {
            tasks.add(
                () -> {
                    transactionTemplate.execute((TransactionCallback<Void>) transactionStatus -> {
                        for (int j = 0; j < recordsPerThread; j++) {
                            Transaction transaction = new Transaction();
                            transaction.setCurrency("USD");
                            transaction.setStatus("POSTED");
                            transaction.setMerchant(em.getReference(Merchant.class, rand.nextInt(merchantCnt.intValue()) + 1));
                            transaction.setAmount(BigDecimal.valueOf(rand.nextInt(100)));
                            em.persist(transaction);

                            if (j > 0 && j % 100 == 0) {
                                log.info(j + " records created for thread: " + Thread.currentThread().getId());
                            }
                        }
                        return null;
                    });

                    awaitTermination.countDown();
                    return null;
                }
            );
        }

        executorService.invokeAll(tasks);
        awaitTermination.await();

        log.info("Create parallel success");
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
                settlementBatch.setTotalTransaction(transactionList.size());
                settlementBatchRepository.save(settlementBatch);

                for(Transaction transaction : transactionList) {
                    transaction.setSettlementBatch(settlementBatch);
                    transaction.setBatchNumber(merchant.getBatchNumber());
                    transaction.setStatus("SETTLED");
                }
                transactionRepository.saveAll(transactionList);

                log.info("merchant id {} has done settlement for total {} transaction, in thread: {}", i, transactionList.size(), Thread.currentThread().getId());
            }
        }

        log.info("Settle success");
        return "success";
    }

    @PostMapping("/settle-parallel")
    public String settleParallel() throws InterruptedException {
        int threadCount = 5;
        long merchantCnt = merchantRepository.count();
        int merchantPerThread = (int) (merchantCnt / threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch awaitTermination = new CountDownLatch(threadCount);
        List<Callable<Integer>> tasks = new ArrayList<>();

        List<Merchant> merchantList = merchantRepository.findAll();
        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            tasks.add(
                    () -> {
                        transactionTemplate.execute((TransactionCallback<Void>) transactionStatus -> {
                            int rangeStart = finalI * merchantPerThread;
                            int rangeEnd = rangeStart + merchantPerThread;
                            for (int j = rangeStart; j < rangeEnd; j++) {
                                Merchant merchant = merchantList.get(j);
                                List<Transaction> transactionList = transactionRepository.findAllByMerchant(merchant);

                                if(transactionList.size() > 0) {
                                    BigDecimal totalAmount = transactionList.stream().map(t -> t.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);

                                    merchant.setBatchNumber(merchant.getBatchNumber() + 1);
                                    merchantRepository.save(merchant);

                                    SettlementBatch settlementBatch = new SettlementBatch();
                                    settlementBatch.setBatchNumber(merchant.getBatchNumber());
                                    settlementBatch.setCurrency(transactionList.get(0).getCurrency());
                                    settlementBatch.setTotalAmount(totalAmount);
                                    settlementBatch.setTotalTransaction(transactionList.size());
                                    settlementBatchRepository.save(settlementBatch);

                                    for(Transaction transaction : transactionList) {
                                        transaction.setSettlementBatch(settlementBatch);
                                        transaction.setBatchNumber(merchant.getBatchNumber());
                                        transaction.setStatus("SETTLED");
                                    }
                                    transactionRepository.saveAll(transactionList);
                                }

                                log.info("merchant id {} has done settlement for total {} transaction, in thread: {}", j, transactionList.size(), Thread.currentThread().getId());
                            }
                            return null;
                        });

                        awaitTermination.countDown();
                        return null;
                    }
            );
        }

        executorService.invokeAll(tasks);
        awaitTermination.await();

        log.info("Settle parallel success");
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

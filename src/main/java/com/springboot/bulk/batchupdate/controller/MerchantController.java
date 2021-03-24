package com.springboot.bulk.batchupdate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.bulk.batchupdate.model.Merchant;
import com.springboot.bulk.batchupdate.repository.MerchantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/merchant")
public class MerchantController {
    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private Integer BATCH_SIZE;

    @Autowired
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MerchantRepository merchantRepository;

    @PostMapping("/create/{cnt}")
    public String create(@PathVariable Integer cnt) {
        List<Merchant> merchantList = new ArrayList<>();
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        Random rand = new Random();
        for(int i=0; i<cnt; i++) {
            Merchant merchant = new Merchant();
            merchant.setBatchNumber(1L);
            merchant.setNextSettlementDate(LocalDate.now().atStartOfDay());
            StringBuffer stringBuffer = new StringBuffer();
            for(int j=0; j<5+rand.nextInt(5); j++) {
                stringBuffer.append(alphabet.charAt(rand.nextInt(25)));
            }
            merchant.setName(stringBuffer.toString());

            merchantList.add(merchant);
            if(merchantList.size() >= BATCH_SIZE) {
                merchantRepository.saveAll(merchantList);
                merchantList.clear();
            }
        }
        if(merchantList.size() > 0) {
            merchantRepository.saveAll(merchantList);
            merchantList.clear();
        }

        log.info("Create success");
        return "success";
    }

    @DeleteMapping("/clear")
    public String clear() {
        merchantRepository.deleteAll();
        log.info("Clear success");
        return "success";
    }
}

package com.springboot.bulk.batchupdate.model;

import com.springboot.bulk.batchupdate.model.base.BaseModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@DynamicUpdate
@Getter
@Setter
@ToString
@Entity
@Table(name = "settlement_batch")
public class SettlementBatch extends BaseModel {
    @Column(name = "batch_number")
    private Long batchNumber;

    @Column(name = "currency")
    private String currency;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "total_transaction")
    private BigDecimal totalTransaction;
}

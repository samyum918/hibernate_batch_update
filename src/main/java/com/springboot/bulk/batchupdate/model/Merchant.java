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
import java.time.LocalDateTime;

@DynamicUpdate
@Getter
@Setter
@ToString
@Entity
@Table(name = "merchant")
public class Merchant extends BaseModel {
    @Column(name = "name")
    String name;

    @Column(name = "batch_number")
    Long batchNumber;

    @Column(name = "next_settlement_date")
    LocalDateTime nextSettlementDate;
}

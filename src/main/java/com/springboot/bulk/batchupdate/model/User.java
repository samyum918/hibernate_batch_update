package com.springboot.bulk.batchupdate.model;

import com.springboot.bulk.batchupdate.model.base.BaseModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = "user")
public class User extends BaseModel {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address1")
    private String address1;

    @Column(name = "address2")
    private String address2;

    @Column(name = "phone")
    private String phone;

    @Column(name = "birth")
    private LocalDateTime birth;
}

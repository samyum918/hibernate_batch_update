package com.springboot.bulk.batchupdate.model;

import com.springboot.bulk.batchupdate.model.base.BaseModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@DynamicUpdate
@Getter
@Setter
@ToString
@Entity
@Table(name = "post_like")
public class PostLike extends BaseModel {
    @Column(name = "like_cnt")
    private Integer likeCnt;
}

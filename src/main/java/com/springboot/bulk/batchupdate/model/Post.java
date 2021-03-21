package com.springboot.bulk.batchupdate.model;

import com.springboot.bulk.batchupdate.model.base.BaseModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@DynamicUpdate
@Getter
@Setter
@ToString
@Entity
@Table(name = "post")
public class Post extends BaseModel {
    @Column(name = "content")
    private String content;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_like_id")
    private PostLike postLike;

    @Version
    private Integer version;
}

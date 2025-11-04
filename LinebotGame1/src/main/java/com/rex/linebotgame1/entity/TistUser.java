package com.rex.linebotgame1.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

//@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tist_user")
@Data
public class TistUser {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true, columnDefinition = "varchar(50) COMMENT 'TIST員工編號'")
    private String tistId;

    @Column(length = 50, nullable = false, unique = true, columnDefinition = "varchar(50) COMMENT 'LINE用戶ID'")
    private String lineId;

    @Column(length = 50, nullable = false, unique = true, columnDefinition = "varchar(50) COMMENT '系統內部員工編號'")
    private String systexId;

    @Column(length = 100, nullable = false, columnDefinition = "varchar(100) COMMENT '單位'")
    private String unitName;

    @Column(length = 50, nullable = false, columnDefinition = "varchar(50) COMMENT '職稱'")
    private String title;

    @Column(length = 50, nullable = false, columnDefinition = "varchar(50) COMMENT '姓名'")
    private String name;

    @Column(length = 50, nullable = false, columnDefinition = "varchar(50) COMMENT '暱稱'")
    private String nickname;

    @Column(length = 255, columnDefinition = "varchar(255) COMMENT '備註'")
    private String remark;

    @Column(length = 255, columnDefinition = "varchar(255) COMMENT 'line圖像網址'")
    private String imageUrl;

    @JsonIgnore
    @Column(nullable = false)
    private Long createdUserId;

    @JsonIgnore
    private Long updateUserId;

    @JsonIgnore
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date createdAt;

    @JsonIgnore
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updatedAt;
}

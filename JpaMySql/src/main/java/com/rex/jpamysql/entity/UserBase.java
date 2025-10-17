package com.rex.jpamysql.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

//@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_base")
@Data
public class UserBase {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @NotBlank(message = "uuid is mandatory")
//    @Size(min = 36, max = 36, message = "UUID size is 36")
    @Column(updatable = false, nullable = false, length = 36, unique = true , columnDefinition = "VARCHAR(36) COMMENT '唯一數UUID'")
    private String uuid;

    @Column(updatable = false, nullable = false, length = 100, unique = true ,columnDefinition = "varchar(100) COMMENT '登入帳號'")
    private String account;

    @JsonIgnore
    @Column(nullable = false, length = 100, columnDefinition = "VARCHAR(100) COMMENT '登入密碼'")
    private String password;

    @Column(length = 255, columnDefinition = "varchar(255) COMMENT '備註'")
    private String remark;

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

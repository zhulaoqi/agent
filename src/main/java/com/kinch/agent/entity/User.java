package com.kinch.agent.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author kinch
 * @date 2025-12-29
 */
@Data
@Entity
@Table(name = "t_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码（加密）
     */
    @Column(nullable = false)
    private String password;

    /**
     * 邮箱
     */
    @Column(length = 100)
    private String email;

    /**
     * 昵称
     */
    @Column(length = 50)
    private String nickname;

    /**
     * 头像URL
     */
    @Column(length = 255)
    private String avatar;

    /**
     * 账号状态
     */
    @Column(length = 20)
    private String status = "active";

    /**
     * 用户角色
     */
    @Column(length = 20)
    private String role = "user";

    /**
     * Token配额
     */
    @Column(name = "token_quota")
    private Integer tokenQuota = 100000;

    /**
     * 已使用Token数
     */
    @Column(name = "token_used")
    private Integer tokenUsed = 0;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}

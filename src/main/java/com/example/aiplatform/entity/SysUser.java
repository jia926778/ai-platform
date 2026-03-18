package com.example.aiplatform.entity;

/**
 * 系统用户实体类
 * <p>
 * 对应数据库表 sys_user，存储用户基本信息、认证凭据和账户状态。
 * 通过 {@code @ManyToMany} 关联角色表 sys_role，实现基于 RBAC 的权限模型。
 * 使用 Lombok 注解自动生成 getter/setter、构造器和 Builder 模式支持。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_user")
public class SysUser {

    /** 用户唯一标识（主键，自增） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户名（唯一，用于登录认证，最大长度50） */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /** 加密后的登录密码 */
    @Column(nullable = false)
    private String password;

    /** 用户邮箱地址 */
    @Column(length = 100)
    private String email;

    /** 用户昵称（用于前端展示） */
    @Column(length = 50)
    private String nickname;

    /** 用户头像URL地址 */
    @Column(length = 255)
    private String avatar;

    /** 账户状态：1-启用，0-禁用 */
    @Column(nullable = false)
    @Builder.Default
    private Integer status = 1;

    /** 账户创建时间（不可更新） */
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /** 账户信息最后更新时间 */
    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /** 用户关联的角色集合（多对多，立即加载），通过中间表 sys_user_role 关联 */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "sys_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<SysRole> roles = new HashSet<>();
}

package com.example.aiplatform.entity;

/**
 * 系统角色实体类
 * <p>
 * 对应数据库表 sys_role，存储角色的基本信息。
 * 与 {@link SysUser} 通过多对多关系关联，实现 RBAC 权限控制。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_role")
public class SysRole {

    /** 角色唯一标识（主键，自增） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 角色显示名称（如：管理员、普通用户） */
    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    /** 角色编码（唯一标识，如：ROLE_ADMIN、ROLE_USER） */
    @Column(name = "role_code", unique = true, nullable = false, length = 50)
    private String roleCode;

    /** 角色描述信息 */
    @Column(length = 200)
    private String description;

    /** 角色创建时间（不可更新） */
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

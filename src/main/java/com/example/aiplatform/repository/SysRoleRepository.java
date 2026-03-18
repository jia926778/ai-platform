package com.example.aiplatform.repository;

import com.example.aiplatform.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 系统角色数据访问接口
 * <p>
 * 继承 JpaRepository 提供 SysRole 实体的基本 CRUD 操作，
 * 并支持通过角色编码查询角色信息。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    /**
     * 根据角色编码查询角色信息
     *
     * @param roleCode 角色编码（如：ROLE_ADMIN、ROLE_USER）
     * @return 包含角色信息的 Optional 对象
     */
    Optional<SysRole> findByRoleCode(String roleCode);
}

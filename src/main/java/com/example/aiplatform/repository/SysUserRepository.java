package com.example.aiplatform.repository;

import com.example.aiplatform.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 系统用户数据访问接口
 * <p>
 * 继承 JpaRepository 提供 SysUser 实体的基本 CRUD 操作，
 * 并定义用户名查询、用户名/邮箱唯一性校验等自定义查询方法。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 包含用户信息的 Optional 对象（用户不存在时为空）
     */
    Optional<SysUser> findByUsername(String username);

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return 存在返回 true，否则返回 false
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否已被注册
     *
     * @param email 邮箱地址
     * @return 存在返回 true，否则返回 false
     */
    boolean existsByEmail(String email);
}

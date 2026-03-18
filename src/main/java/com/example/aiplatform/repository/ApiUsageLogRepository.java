package com.example.aiplatform.repository;

import com.example.aiplatform.entity.ApiUsageLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * API使用日志数据访问接口
 * <p>
 * 继承 JpaRepository 提供 ApiUsageLog 实体的基本 CRUD 操作，
 * 并提供分页查询、统计总token消耗量、按API类型分组统计等自定义查询方法。
 * 主要用于用户用量监控和费用统计功能。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Repository
public interface ApiUsageLogRepository extends JpaRepository<ApiUsageLog, Long> {

    /**
     * 分页查询指定用户的API调用日志，按创建时间降序排列
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 分页的日志数据
     */
    Page<ApiUsageLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 统计指定用户的API调用总次数
     *
     * @param userId 用户ID
     * @return 调用次数
     */
    long countByUserId(Long userId);

    /**
     * 计算指定用户消耗的总token数量
     *
     * @param userId 用户ID
     * @return 总token数（无记录时返回0）
     */
    @Query("SELECT COALESCE(SUM(a.totalTokens), 0) FROM ApiUsageLog a WHERE a.userId = :userId")
    long sumTotalTokensByUserId(@Param("userId") Long userId);

    /**
     * 按API类型分组统计指定用户的调用数据
     * <p>
     * 返回结果每行包含：API类型、调用次数、总token数、总费用
     * </p>
     *
     * @param userId 用户ID
     * @return 分组统计结果列表
     */
    @Query("SELECT a.apiType, COUNT(a), COALESCE(SUM(a.totalTokens), 0), COALESCE(SUM(a.cost), 0) " +
            "FROM ApiUsageLog a WHERE a.userId = :userId GROUP BY a.apiType")
    List<Object[]> getStatsByApiTypeForUser(@Param("userId") Long userId);
}

package com.example.aiplatform.repository;

import com.example.aiplatform.entity.AiRequestLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI请求日志MySQL数据访问层
 */
@Repository
public interface AiRequestLogRepository extends JpaRepository<AiRequestLog, Long> {

    /** 根据用户ID分页查询请求日志（按时间倒序） */
    Page<AiRequestLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** 根据用户ID和API类型查询 */
    Page<AiRequestLog> findByUserIdAndApiTypeOrderByCreatedAtDesc(Long userId, String apiType, Pageable pageable);

    /** 根据用户ID和时间范围查询 */
    @Query("SELECT r FROM AiRequestLog r WHERE r.userId = :userId AND r.createdAt BETWEEN :start AND :end ORDER BY r.createdAt DESC")
    Page<AiRequestLog> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                  @Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end,
                                                  Pageable pageable);

    /** 管理员：按条件筛选（用户ID可选） */
    @Query("SELECT r FROM AiRequestLog r WHERE " +
           "(:userId IS NULL OR r.userId = :userId) AND " +
           "(:apiType IS NULL OR r.apiType = :apiType) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:start IS NULL OR r.createdAt >= :start) AND " +
           "(:end IS NULL OR r.createdAt <= :end) " +
           "ORDER BY r.createdAt DESC")
    Page<AiRequestLog> findByFilters(@Param("userId") Long userId,
                                      @Param("apiType") String apiType,
                                      @Param("status") String status,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      Pageable pageable);

    /** 统计某用户的请求总数 */
    long countByUserId(Long userId);

    /** 统计某用户某API类型的请求数 */
    long countByUserIdAndApiType(Long userId, String apiType);
}

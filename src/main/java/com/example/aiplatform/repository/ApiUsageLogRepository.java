package com.example.aiplatform.repository;

import com.example.aiplatform.entity.ApiUsageLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiUsageLogRepository extends JpaRepository<ApiUsageLog, Long> {

    Page<ApiUsageLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(a.totalTokens), 0) FROM ApiUsageLog a WHERE a.userId = :userId")
    long sumTotalTokensByUserId(@Param("userId") Long userId);

    @Query("SELECT a.apiType, COUNT(a), COALESCE(SUM(a.totalTokens), 0), COALESCE(SUM(a.cost), 0) " +
            "FROM ApiUsageLog a WHERE a.userId = :userId GROUP BY a.apiType")
    List<Object[]> getStatsByApiTypeForUser(@Param("userId") Long userId);
}

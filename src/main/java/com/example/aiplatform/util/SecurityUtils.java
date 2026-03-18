package com.example.aiplatform.util;

import com.example.aiplatform.entity.SysUser;
import com.example.aiplatform.exception.BusinessException;
import com.example.aiplatform.repository.SysUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 安全工具类
 * <p>
 * 提供从 Spring Security 上下文中获取当前认证用户信息的静态方法。
 * 包括获取当前用户名和当前用户ID。
 * 私有构造函数防止实例化。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
public final class SecurityUtils {

    /** 私有构造函数，防止工具类被实例化 */
    private SecurityUtils() {
    }

    /**
     * 获取当前认证用户的用户名
     * <p>
     * 从 Spring Security 上下文中提取 UserDetails 对象获取用户名。
     * </p>
     *
     * @return 当前用户名
     * @throws BusinessException 用户未认证时抛出（状态码401）
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 检查认证对象是否为UserDetails类型（使用模式匹配）
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        throw new BusinessException(401, "User not authenticated");
    }

    /**
     * 获取当前认证用户的数据库ID
     * <p>
     * 先获取用户名，再从数据库中查询对应的用户ID。
     * </p>
     *
     * @param userRepository 用户数据访问接口
     * @return 当前用户ID
     * @throws BusinessException 用户未认证或用户不存在时抛出
     */
    public static Long getCurrentUserId(SysUserRepository userRepository) {
        String username = getCurrentUsername();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(401, "User not found"));
        return user.getId();
    }
}

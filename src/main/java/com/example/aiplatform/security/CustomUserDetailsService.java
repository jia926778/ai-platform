package com.example.aiplatform.security;

import com.example.aiplatform.entity.SysUser;
import com.example.aiplatform.repository.SysUserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义用户详情服务
 * <p>
 * 实现 Spring Security 的 {@link UserDetailsService} 接口，
 * 从数据库加载用户信息并转换为 Spring Security 所需的 {@link UserDetails} 对象。
 * 将系统角色转换为 Spring Security 的权限（GrantedAuthority）。
 * </p>
 *
 * @author AI Platform
 * @since 1.0.0
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserRepository userRepository;

    public CustomUserDetailsService(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 根据用户名加载用户详情
     * <p>
     * 从数据库查询用户信息，将角色编码转换为GrantedAuthority，
     * 并根据用户状态字段判断账户是否启用。
     * </p>
     *
     * @param username 用户名
     * @return Spring Security的UserDetails对象
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库查询用户，不存在则抛出异常
        SysUser sysUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 将用户角色转换为Spring Security权限列表
        List<GrantedAuthority> authorities = sysUser.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleCode()))
                .collect(Collectors.toList());

        // 构建UserDetails对象，status=1表示账户启用
        return new User(
                sysUser.getUsername(),
                sysUser.getPassword(),
                sysUser.getStatus() == 1,
                true,
                true,
                true,
                authorities
        );
    }
}

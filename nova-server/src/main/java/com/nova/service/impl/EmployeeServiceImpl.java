package com.nova.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.nova.constant.MessageConstant;
import com.nova.constant.PasswordConstant;
import com.nova.constant.StatusConstant;
import com.nova.context.BaseContext;
import com.nova.converter.EmployeeConverter;
import com.nova.dto.EmployeeDTO;
import com.nova.dto.EmployeeLoginDTO;
import com.nova.dto.EmployeePageQueryDTO;
import com.nova.dto.PasswordEditDTO;
import com.nova.entity.Employee;
import com.nova.exception.AccountLockedException;
import com.nova.exception.AccountNotFoundException;
import com.nova.exception.BaseException;
import com.nova.exception.PasswordEditFailedException;
import com.nova.exception.PasswordErrorException;
import com.nova.mapper.EmployeeMapper;
import com.nova.result.PageResult;
import com.nova.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final EmployeeConverter employeeConverter;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String ACCOUNT_LOCK_KEY = "login:fail:account:";
    private static final String IP_LIMIT_KEY = "login:fail:ip:";
    private static final int MAX_ACCOUNT_FAILURES = 5;
    private static final int MAX_IP_ATTEMPTS = 10;
    private static final long ACCOUNT_LOCK_MINUTES = 30;
    private static final long IP_LIMIT_SECONDS = 60;

    @Override
    public Employee login(EmployeeLoginDTO dto, String clientIp) {
        String username = dto.getUsername();
        String rawPassword = dto.getPassword();

        // 1. 检查账号锁
        checkAccountLock(username);
        // 2. 检查 IP 限频
        checkIpRateLimit(clientIp);

        Employee employee = employeeMapper.getByUsername(username);
        if (employee == null) {
            recordLoginFailure(username, clientIp);
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        boolean matched;
        String storedPwd = employee.getPassword();
        if (storedPwd != null && storedPwd.startsWith("$2")) {
            matched = passwordEncoder.matches(rawPassword, storedPwd);
        } else {
            matched = storedPwd != null && storedPwd.equals(DigestUtils.md5DigestAsHex(rawPassword.getBytes()));
            if (matched) {
                log.info("用户 {} 使用 MD5 密码登录成功，升级为 BCrypt", username);
                employee.setPassword(passwordEncoder.encode(rawPassword));
                employeeMapper.update(employee);
            }
        }

        if (!matched) {
            recordLoginFailure(username, clientIp);
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        if (employee.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 登录成功 — 清除账号锁
        clearAccountLock(username);
        return employee;
    }

    private void checkAccountLock(String username) {
        String count = stringRedisTemplate.opsForValue().get(ACCOUNT_LOCK_KEY + username);
        if (count != null && Integer.parseInt(count) >= MAX_ACCOUNT_FAILURES) {
            log.warn("账号已锁定: {}", username);
            throw new AccountLockedException("账号已锁定，请 " + ACCOUNT_LOCK_MINUTES + " 分钟后重试");
        }
    }

    private void checkIpRateLimit(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) return;
        String count = stringRedisTemplate.opsForValue().get(IP_LIMIT_KEY + clientIp);
        if (count != null && Integer.parseInt(count) >= MAX_IP_ATTEMPTS) {
            log.warn("IP 被限流: {}", clientIp);
            throw new BaseException("操作过于频繁，请稍后重试");
        }
    }

    private void recordLoginFailure(String username, String clientIp) {
        // 按账号计数：INCR，仅在首次设置 TTL
        Long accountCount = stringRedisTemplate.opsForValue().increment(ACCOUNT_LOCK_KEY + username);
        if (accountCount != null && accountCount == 1) {
            stringRedisTemplate.expire(ACCOUNT_LOCK_KEY + username, ACCOUNT_LOCK_MINUTES, TimeUnit.MINUTES);
        }
        // 按 IP 计数：INCR，仅在首次设置 TTL
        if (clientIp != null && !clientIp.isBlank()) {
            Long ipCount = stringRedisTemplate.opsForValue().increment(IP_LIMIT_KEY + clientIp);
            if (ipCount != null && ipCount == 1) {
                stringRedisTemplate.expire(IP_LIMIT_KEY + clientIp, IP_LIMIT_SECONDS, TimeUnit.SECONDS);
            }
        }
    }

    private void clearAccountLock(String username) {
        stringRedisTemplate.delete(ACCOUNT_LOCK_KEY + username);
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(dto);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public Employee getById(Long id) {
        return employeeMapper.getById(id);
    }

    @Override
    public void save(EmployeeDTO dto) {
        Employee employee = employeeConverter.toEntity(dto);
        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword(passwordEncoder.encode(PasswordConstant.DEFAULT_PASSWORD));
        employeeMapper.insert(employee);
    }

    @Override
    public void update(EmployeeDTO dto) {
        Employee employee = employeeConverter.updateEntity(dto);
        employeeMapper.update(employee);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        employeeMapper.update(employee);
    }

    @Override
    public void editPassword(PasswordEditDTO dto) {
        Long empId = dto.getEmpId();
        if (empId == null) {
            empId = BaseContext.getCurrentId();
        }
        Employee employee = employeeMapper.getById(empId);
        if (employee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        String storedPwd = employee.getPassword();
        boolean oldPwdMatched;
        if (storedPwd != null && storedPwd.startsWith("$2")) {
            oldPwdMatched = passwordEncoder.matches(dto.getOldPassword(), storedPwd);
        } else {
            oldPwdMatched = storedPwd != null && storedPwd.equals(DigestUtils.md5DigestAsHex(dto.getOldPassword().getBytes()));
        }
        if (!oldPwdMatched) {
            throw new PasswordEditFailedException(MessageConstant.PASSWORD_EDIT_FAILED);
        }

        Employee updated = Employee.builder()
                .id(empId)
                .password(passwordEncoder.encode(dto.getNewPassword()))
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        employeeMapper.update(updated);
    }
}

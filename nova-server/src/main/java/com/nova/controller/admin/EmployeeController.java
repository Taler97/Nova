package com.nova.controller.admin;

import com.nova.constant.JwtClaimsConstant;
import com.nova.dto.EmployeeDTO;
import com.nova.dto.EmployeeLoginDTO;
import com.nova.dto.EmployeePageQueryDTO;
import com.nova.dto.PasswordEditDTO;
import com.nova.entity.Employee;
import com.nova.properties.JwtProperties;
import com.nova.result.PageResult;
import com.nova.result.Result;
import com.nova.service.EmployeeService;
import com.nova.utils.JwtUtil;
import com.nova.vo.EmployeeLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Tag(name = "员工管理相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping("/login")
    @Operation(summary = "员工登录")
    public Result<EmployeeLoginVO> login(@Valid @RequestBody EmployeeLoginDTO employeeLoginDTO,
                                         HttpServletRequest request) {
        log.info("员工登录：{}", employeeLoginDTO);
        String clientIp = getClientIp(request);
        Employee employee = employeeService.login(employeeLoginDTO, clientIp);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(), claims);

        EmployeeLoginVO vo = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token).build();
        return Result.success(vo);
    }

    @PostMapping("/logout")
    @Operation(summary = "员工退出")
    public Result logout() {
        return Result.success();
    }

    /**
     * 从请求中提取客户端 IP，考虑反向代理场景下的 X-Forwarded-For 头部。
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多个 IP：客户端 IP、代理1、代理2
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @PostMapping
    @Operation(summary = "新增员工")
    public Result save(@Valid @RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工：{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO dto) {
        PageResult r = employeeService.pageQuery(dto);
        return Result.success(r);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询员工信息")
    public Result<Employee> getById(@PathVariable Long id) {
        return Result.success(employeeService.getById(id));
    }

    @PutMapping
    @Operation(summary = "编辑员工信息")
    public Result update(@Valid @RequestBody EmployeeDTO employeeDTO) {
        employeeService.update(employeeDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "启用/禁用员工账号")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    @PutMapping("/editPassword")
    @Operation(summary = "员工修改密码")
    public Result editPassword(@Valid @RequestBody PasswordEditDTO dto) {
        employeeService.editPassword(dto);
        return Result.success();
    }
}

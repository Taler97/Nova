package com.nova.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private Long id;
    private String openid;
    private String name;
    private String phone;
    private String password;
    private String sex;
    private String avatar;
    private LocalDateTime createTime;
}

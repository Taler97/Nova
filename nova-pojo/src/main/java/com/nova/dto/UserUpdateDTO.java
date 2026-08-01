package com.nova.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserUpdateDTO implements Serializable {
    private String name;
    private String avatar;
}

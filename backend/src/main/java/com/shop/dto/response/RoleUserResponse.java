package com.shop.dto.response;

import lombok.Data;

@Data
public class RoleUserResponse {

    private Long id;

    private String username;

    private String email;

    private String phone;

    private Integer status;
}

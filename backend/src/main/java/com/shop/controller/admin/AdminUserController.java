package com.shop.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.dao.entity.User;
import com.shop.dto.request.UserCreateRequest;
import com.shop.dto.request.UserQueryRequest;
import com.shop.dto.request.UserUpdateRequest;
import com.shop.dto.response.ApiResponse;
import com.shop.dto.response.PageResponse;
import com.shop.dto.response.UserDetailResponse;
import com.shop.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "后台用户管理接口")
public class AdminUserController {

    private final UserService userService;

    @GetMapping("")
    public ApiResponse<PageResponse<User>> listUsers(@Valid UserQueryRequest request) {
        Page<User> page = userService.listUsers(request);
        return ApiResponse.success(PageResponse.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDetailResponse> getUserDetail(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserDetail(id));
    }

    @PostMapping("")
    public ApiResponse<Long> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.success(userService.createUser(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateUserStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        userService.updateUserStatus(id, request.getStatus());
        return ApiResponse.success(null);
    }

    @PostMapping("/batch-disable")
    public ApiResponse<Void> batchDisable(@RequestBody IdsRequest request) {
        userService.batchUpdateUserStatus(request.getIds(), 0);
        return ApiResponse.success(null);
    }

    @PostMapping("/batch-enable")
    public ApiResponse<Void> batchEnable(@RequestBody IdsRequest request) {
        userService.batchUpdateUserStatus(request.getIds(), 1);
        return ApiResponse.success(null);
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Void> batchDelete(@RequestBody IdsRequest request) {
        for (Long id : request.getIds()) {
            userService.deleteUser(id);
        }
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request.getPassword());
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/roles")
    public ApiResponse<Void> assignRoles(@PathVariable Long id, @RequestBody RoleIdsRequest request) {
        userService.assignUserRoles(id, request.getRoleIds());
        return ApiResponse.success(null);
    }

    @Data
    public static class StatusRequest {
        private Integer status;
    }

    @Data
    public static class IdsRequest {
        private List<Long> ids;
    }

    @Data
    public static class RoleIdsRequest {
        private List<Long> roleIds;
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank
        private String password;
    }
}

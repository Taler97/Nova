package com.nova.controller.user;

import com.nova.dto.AddressBookDTO;
import com.nova.entity.AddressBook;
import com.nova.result.Result;
import com.nova.service.AddressBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController("userAddressBookController")
@RequestMapping("/user/addressBook")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户端-地址管理接口")
public class AddressBookController {

    private final AddressBookService addressBookService;

    @GetMapping("/list")
    @Operation(summary = "查询所有地址")
    public Result<List<AddressBook>> list() {
        return Result.success(addressBookService.list());
    }

    @PostMapping
    @Operation(summary = "新增地址")
    public Result save(@Valid @RequestBody AddressBookDTO dto) {
        addressBookService.save(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改地址")
    public Result update(@Valid @RequestBody AddressBookDTO dto) {
        addressBookService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除地址")
    public Result deleteById(@PathVariable Long id) {
        addressBookService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/default")
    @Operation(summary = "查询默认地址")
    public Result<AddressBook> getDefault() {
        return Result.success(addressBookService.getDefault());
    }
}

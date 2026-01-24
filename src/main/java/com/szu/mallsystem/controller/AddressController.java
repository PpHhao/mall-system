package com.szu.mallsystem.controller;

import com.szu.mallsystem.common.Result;
import com.szu.mallsystem.dto.address.CreateAddressRequest;
import com.szu.mallsystem.dto.address.UpdateAddressRequest;
import com.szu.mallsystem.entity.User;
import com.szu.mallsystem.security.CurrentUserProvider;
import com.szu.mallsystem.service.AddressService;
import com.szu.mallsystem.vo.AddressVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public Result<List<AddressVO>> listAddresses() {
        User user = currentUserProvider.getCurrentUser();
        return Result.success(addressService.listAddresses(user.getId()));
    }

    @PostMapping
    public Result<AddressVO> createAddress(@Valid @RequestBody CreateAddressRequest request) {
        User user = currentUserProvider.getCurrentUser();
        return Result.success(addressService.createAddress(user.getId(), request));
    }

    @PutMapping("/{addressId}")
    public Result<AddressVO> updateAddress(@PathVariable Long addressId,
                                           @Valid @RequestBody UpdateAddressRequest request) {
        User user = currentUserProvider.getCurrentUser();
        return Result.success(addressService.updateAddress(user.getId(), addressId, request));
    }

    @DeleteMapping("/{addressId}")
    public Result<Void> deleteAddress(@PathVariable Long addressId) {
        User user = currentUserProvider.getCurrentUser();
        addressService.deleteAddress(user.getId(), addressId);
        return Result.success();
    }
}

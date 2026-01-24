package com.szu.mallsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.szu.mallsystem.dto.address.CreateAddressRequest;
import com.szu.mallsystem.dto.address.UpdateAddressRequest;
import com.szu.mallsystem.entity.Address;
import com.szu.mallsystem.vo.AddressVO;

import java.util.List;

public interface AddressService extends IService<Address> {
    AddressVO createAddress(Long userId, CreateAddressRequest request);

    AddressVO updateAddress(Long userId, Long addressId, UpdateAddressRequest request);

    void deleteAddress(Long userId, Long addressId);

    List<AddressVO> listAddresses(Long userId);

    Address getUserAddress(Long userId, Long addressId);
}

package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.dto.address.CreateAddressRequest;
import com.szu.mallsystem.dto.address.UpdateAddressRequest;
import com.szu.mallsystem.entity.Address;
import com.szu.mallsystem.mapper.AddressMapper;
import com.szu.mallsystem.service.AddressService;
import com.szu.mallsystem.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    @Transactional
    public AddressVO createAddress(Long userId, CreateAddressRequest request) {
        long existingCount = lambdaQuery()
                .eq(Address::getUserId, userId)
                .count();

        boolean makeDefault = request.getIsDefault() != null && request.getIsDefault() == 1;
        if (existingCount == 0) {
            makeDefault = true;
        }

        if (makeDefault) {
            clearDefault(userId);
        }

        Address address = new Address();
        address.setUserId(userId);
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());
        address.setPostalCode(request.getPostalCode());
        address.setIsDefault(makeDefault ? 1 : 0);
        save(address);
        return buildAddressVO(address);
    }

    @Override
    @Transactional
    public AddressVO updateAddress(Long userId, Long addressId, UpdateAddressRequest request) {
        Address address = getUserAddress(userId, addressId);

        if (StringUtils.hasText(request.getReceiverName())) {
            address.setReceiverName(request.getReceiverName());
        }
        if (StringUtils.hasText(request.getReceiverPhone())) {
            address.setReceiverPhone(request.getReceiverPhone());
        }
        if (StringUtils.hasText(request.getProvince())) {
            address.setProvince(request.getProvince());
        }
        if (StringUtils.hasText(request.getCity())) {
            address.setCity(request.getCity());
        }
        if (StringUtils.hasText(request.getDistrict())) {
            address.setDistrict(request.getDistrict());
        }
        if (StringUtils.hasText(request.getDetail())) {
            address.setDetail(request.getDetail());
        }
        if (StringUtils.hasText(request.getPostalCode())) {
            address.setPostalCode(request.getPostalCode());
        }
        if (request.getIsDefault() != null) {
            if (request.getIsDefault() == 1) {
                clearDefault(userId);
                address.setIsDefault(1);
            } else {
                address.setIsDefault(0);
            }
        }

        updateById(address);
        return buildAddressVO(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = getUserAddress(userId, addressId);
        removeById(addressId);

        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            Address latest = lambdaQuery()
                    .eq(Address::getUserId, userId)
                    .orderByDesc(Address::getId)
                    .last("limit 1")
                    .one();
            if (latest != null) {
                latest.setIsDefault(1);
                updateById(latest);
            }
        }
    }

    @Override
    public List<AddressVO> listAddresses(Long userId) {
        List<Address> addresses = lambdaQuery()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getId)
                .list();
        if (addresses.isEmpty()) {
            return new ArrayList<>();
        }
        return addresses.stream()
                .map(this::buildAddressVO)
                .collect(Collectors.toList());
    }

    @Override
    public Address getUserAddress(Long userId, Long addressId) {
        Address address = getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Address not found");
        }
        return address;
    }

    private void clearDefault(Long userId) {
        lambdaUpdate()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0)
                .update();
    }

    private AddressVO buildAddressVO(Address address) {
        return AddressVO.builder()
                .id(address.getId())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .detail(address.getDetail())
                .postalCode(address.getPostalCode())
                .isDefault(address.getIsDefault())
                .build();
    }
}

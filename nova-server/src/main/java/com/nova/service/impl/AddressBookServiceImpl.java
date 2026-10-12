package com.nova.service.impl;

import com.nova.config.LocalCacheConfiguration;
import com.nova.context.BaseContext;
import com.nova.converter.AddressBookConverter;
import com.nova.dto.AddressBookDTO;
import com.nova.entity.AddressBook;
import com.nova.mapper.AddressBookMapper;
import com.nova.service.AddressBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressBookServiceImpl implements AddressBookService {

    private final AddressBookMapper addressBookMapper;
    private final AddressBookConverter addressBookConverter;

    /** 用户私有数据 -> 本地缓存（Caffeine），key 为用户 ID。 */
    @Override
    @Cacheable(cacheManager = "caffeineCacheManager",
            cacheNames = LocalCacheConfiguration.USER_ADDRESS,
            key = "T(com.nova.context.BaseContext).getCurrentId()")
    public List<AddressBook> list() {
        return addressBookMapper.list(BaseContext.getCurrentId());
    }

    @Override
    @CacheEvict(cacheManager = "caffeineCacheManager",
            cacheNames = LocalCacheConfiguration.USER_ADDRESS,
            key = "T(com.nova.context.BaseContext).getCurrentId()")
    public void save(AddressBookDTO dto) {
        AddressBook addressBook = addressBookConverter.toEntity(dto);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setCreateTime(LocalDateTime.now());
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            addressBookMapper.clearDefault(BaseContext.getCurrentId());
        }
        addressBookMapper.insert(addressBook);
    }

    @Override
    @CacheEvict(cacheManager = "caffeineCacheManager",
            cacheNames = LocalCacheConfiguration.USER_ADDRESS,
            key = "T(com.nova.context.BaseContext).getCurrentId()")
    public void update(AddressBookDTO dto) {
        AddressBook addressBook = addressBookConverter.updateEntity(dto);
        addressBook.setUserId(BaseContext.getCurrentId());
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            addressBookMapper.clearDefault(BaseContext.getCurrentId());
        }
        addressBookMapper.update(addressBook);
    }

    @Override
    @CacheEvict(cacheManager = "caffeineCacheManager",
            cacheNames = LocalCacheConfiguration.USER_ADDRESS,
            key = "T(com.nova.context.BaseContext).getCurrentId()")
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id, BaseContext.getCurrentId());
    }

    @Override
    public AddressBook getDefault() {
        return addressBookMapper.getDefaultByUserId(BaseContext.getCurrentId());
    }
}

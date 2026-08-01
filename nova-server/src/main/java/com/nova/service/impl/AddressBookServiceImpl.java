package com.nova.service.impl;

import com.nova.context.BaseContext;
import com.nova.converter.AddressBookConverter;
import com.nova.dto.AddressBookDTO;
import com.nova.entity.AddressBook;
import com.nova.mapper.AddressBookMapper;
import com.nova.service.AddressBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressBookServiceImpl implements AddressBookService {

    private final AddressBookMapper addressBookMapper;
    private final AddressBookConverter addressBookConverter;

    @Override
    public List<AddressBook> list() {
        return addressBookMapper.list(BaseContext.getCurrentId());
    }

    @Override
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
    public void update(AddressBookDTO dto) {
        AddressBook addressBook = addressBookConverter.updateEntity(dto);
        addressBook.setUserId(BaseContext.getCurrentId());
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            addressBookMapper.clearDefault(BaseContext.getCurrentId());
        }
        addressBookMapper.update(addressBook);
    }

    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id, BaseContext.getCurrentId());
    }

    @Override
    public AddressBook getDefault() {
        return addressBookMapper.getDefaultByUserId(BaseContext.getCurrentId());
    }
}

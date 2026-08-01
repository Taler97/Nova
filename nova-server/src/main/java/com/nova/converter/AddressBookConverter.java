package com.nova.converter;

import com.nova.dto.AddressBookDTO;
import com.nova.entity.AddressBook;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressBookConverter {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    AddressBook toEntity(AddressBookDTO dto);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    AddressBook updateEntity(AddressBookDTO dto);
}

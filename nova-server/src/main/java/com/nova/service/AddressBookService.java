package com.nova.service;

import com.nova.dto.AddressBookDTO;
import com.nova.entity.AddressBook;
import java.util.List;

public interface AddressBookService {
    List<AddressBook> list();
    void save(AddressBookDTO dto);
    void update(AddressBookDTO dto);
    void deleteById(Long id);
    AddressBook getDefault();
}

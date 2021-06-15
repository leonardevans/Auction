package com.auction.services;

import com.auction.model.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    Page<User> findPaginated(int pageNo, int pageSize);
}

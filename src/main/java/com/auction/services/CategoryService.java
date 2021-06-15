package com.auction.services;

import com.auction.model.Category;
import org.springframework.data.domain.Page;

public interface CategoryService {
    Page<Category> findPaginated(int pageNo, int pageSize);
}

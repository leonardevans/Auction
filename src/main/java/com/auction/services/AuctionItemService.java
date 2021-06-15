package com.auction.services;

import com.auction.model.AuctionItem;
import com.auction.model.Category;
import com.auction.model.User;
import org.springframework.data.domain.Page;

public interface AuctionItemService {
    Page<AuctionItem> getAll(int pageNo, int pageSize);
    Page<AuctionItem> getAllByCategory(Category category, int pageNo, int pageSize);
    Page<AuctionItem> getAllBySeller(User seller, int pageNo, int pageSize);
}

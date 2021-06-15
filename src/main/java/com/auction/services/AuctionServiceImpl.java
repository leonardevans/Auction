package com.auction.services;

import com.auction.model.AuctionItem;
import com.auction.model.Category;
import com.auction.model.User;
import com.auction.repo.AuctionItemRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AuctionServiceImpl implements AuctionItemService{
    @Autowired
    AuctionItemRepo auctionItemRepo;

    @Override
    public Page<AuctionItem> getAll(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return this.auctionItemRepo.findAll(pageable);
    }

    @Override
    public Page<AuctionItem> getAllByCategory(Category category, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return this.auctionItemRepo.findByCategory(category, pageable);
    }

    @Override
    public Page<AuctionItem> getAllBySeller(User seller, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return this.auctionItemRepo.findBySeller(seller, pageable);
    }
}

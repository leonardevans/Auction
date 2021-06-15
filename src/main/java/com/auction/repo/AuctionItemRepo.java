package com.auction.repo;


import com.auction.model.AuctionItem;
import com.auction.model.Category;
import com.auction.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionItemRepo extends JpaRepository<AuctionItem, Long> {
    Page<AuctionItem> findAll(Pageable pageable);
    Page<AuctionItem> findByCategory(Category category, Pageable pageable);
    boolean existsByCategory(Category category);
    Page<AuctionItem> findBySeller(User seller, Pageable pageable);
    List<AuctionItem> findByClosed(boolean closed);
}

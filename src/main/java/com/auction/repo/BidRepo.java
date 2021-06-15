package com.auction.repo;

import com.auction.model.AuctionItem;
import com.auction.model.Bid;
import com.auction.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BidRepo extends JpaRepository<Bid, Long> {
    Page<Bid> findAllByAuctionItem(AuctionItem auctionItem, Pageable pageable);
    Page<Bid> findByBidder(User bidder, Pageable pageable);
}

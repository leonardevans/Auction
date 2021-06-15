package com.auction.services;

import com.auction.model.AuctionItem;
import com.auction.model.Bid;
import com.auction.model.User;
import org.springframework.data.domain.Page;

public interface BidService {
    Page<Bid> getAllAuctionItemBids(AuctionItem auctionItem, int pageNo, int pageSize);
    Page<Bid> getUserBids(User bidder, int pageNo, int pageSize);
}

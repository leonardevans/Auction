package com.auction.services;

import com.auction.model.AuctionItem;
import com.auction.model.Bid;
import com.auction.model.User;
import com.auction.repo.BidRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class BidServiceImpl implements BidService{
    @Autowired
    BidRepo bidRepo;

    @Override
    public Page<Bid> getAllAuctionItemBids(AuctionItem auctionItem, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by("price").descending());
        return bidRepo.findAllByAuctionItem(auctionItem,pageable);
    }

    @Override
    public Page<Bid> getUserBids(User bidder, int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, Sort.by("bidDate").descending());
        return bidRepo.findByBidder(bidder, pageable);
    }
}

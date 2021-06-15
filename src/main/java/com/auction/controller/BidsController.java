package com.auction.controller;

import com.auction.model.AuctionItem;
import com.auction.model.Bid;
import com.auction.model.User;
import com.auction.payloads.BidRequest;
import com.auction.repo.AuctionItemRepo;
import com.auction.repo.CategoryRepo;
import com.auction.security.AuthUtil;
import com.auction.services.AuctionItemService;
import com.auction.services.EmailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
public class BidsController {
    @Autowired
    AuctionItemRepo auctionItemRepo;

    @Autowired
    AuctionItemService auctionItemService;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    private EmailSenderService emailSenderService;

    @Value( "${app.emailfrom}" )
    private String emailFrom;

    //placing a bid
    @PostMapping("/api/item/bid")
    public ResponseEntity<Object> placeBid(@RequestBody BidRequest bidRequest){
        User loggedInUser = authUtil.getLoggedInUser();
        if (loggedInUser== null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized");

        //check if this auction item exists
        Optional<AuctionItem> optionalAuctionItem = auctionItemRepo.findById(bidRequest.getAuctionItemId());
        if (optionalAuctionItem.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not_found");

        AuctionItem auctionItem = optionalAuctionItem.get();

        //check if bidding is closed for this item, if closed, tell the bidder bidding has closed
        if (auctionItem.isClosed())
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("bidding_closed");

        //check if there a bids for this item
        if (auctionItem.getBids().size() > 0){
            //get the bids and save them to a list
            List<Bid> bids = new ArrayList<>(auctionItem.getBids());

            //sort the list by bid price into descending order
            bids.sort(new SortByBidPrice().reversed());

            double topBid = bids.get(0).getPrice();

            if (topBid >= bidRequest.getBidPrice()){
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("bid_is_less_than_top_bid");
            }
        }

        //check if this bid is greater than the starting bid
        if (auctionItem.getStartingBid() >= bidRequest.getBidPrice())
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("bid_is_less_than_starting_bid");

        //create bid
        Bid bid = new Bid(auctionItem,loggedInUser, bidRequest.getBidPrice());
        auctionItem.getBids().add(bid);

        auctionItemRepo.save(auctionItem);

        //prepare email message
        String message = "Your bid was placed successfully.";
        message += "\nAuction item: " + auctionItem.getName();
        message += "\nBid amount: " + bid.getPrice();

        //prepare email about the bid
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(loggedInUser.getEmail());
        mailMessage.setSubject("Bid Placement");
        mailMessage.setFrom(emailFrom);
        mailMessage.setText(message);

        //send email about the bid
        emailSenderService.sendEmail(mailMessage);


        return ResponseEntity.status(HttpStatus.OK).body("bid_placed");

    }

    public static class SortByBidPrice implements Comparator<Bid> {
        // Used for sorting in ascending order of
        // roll number
        public int compare(Bid a, Bid b) {
            return (int) (a.getPrice() - b.getPrice());
        }
    }
}

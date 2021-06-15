package com.auction.services;

import com.auction.controller.BidsController;
import com.auction.model.AuctionItem;
import com.auction.model.Bid;
import com.auction.model.User;
import com.auction.repo.AuctionItemRepo;
import com.auction.repo.BidRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AuctionBidsService implements CommandLineRunner {
    @Autowired
    private BidRepo boot;

    @Autowired
    private AuctionItemRepo auctionItemRepo;

    @Autowired
    private EmailSenderService emailSenderService;

    @Value( "${app.emailfrom}" )
    private String emailFrom;

    @Override
    public void run(String... args) throws Exception {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 7);
        c.set(Calendar.SECOND, 0);

        final int[] count = {0};

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //Call your method here
                //setEmail(emailContent, subject);

                //get a list of auction items open to bid
                List<AuctionItem> auctionItemsOpenToBid = auctionItemRepo.findByClosed(false);

                //loop through these items to see if the auction items closing date has reached
                for (AuctionItem auctionItem : auctionItemsOpenToBid
                ) {
                    //Getting the current date
                    Date date = new Date();
                    //This method returns the time in millis
                    long curTimeMilli = date.getTime();

                    //get auctionItemClosing date in millisecond
                    long itemClosingTimeMilli = auctionItem.getClosingDate().getTime();

                    //check if closing date has reached, if yes send email to winner and close the bid
                    if (curTimeMilli > itemClosingTimeMilli){
                        //set item bidding to closed
                        auctionItem.setClosed(true);

                        //save this auction item
                        auctionItemRepo.save(auctionItem);

                        //get the top bidder and declare them the winner
                        //first check if there are bids fot this item
                        if (auctionItem.getBids().size() > 0){
                            //get the bids and save them to a list
                            List<Bid> bids = new ArrayList<>(auctionItem.getBids());

                            //sort the list by bid price into descending order
                            bids.sort(new BidsController.SortByBidPrice().reversed());

                            //get to bid
                            Bid topBid = bids.get(0);

                            //get the top bidder
                            User topBidderAndWinner = topBid.getBidder();

                            //send email to the winner
                            //prepare email message
                            String message = "Congratulations " + topBidderAndWinner.getName() +"!You just won the following item you had bid.";
                            message += "\nAuction item: " + auctionItem.getName();
                            message += "\nBid amount: " + topBid.getPrice();
                            message += "\nBid date: " + topBid.getBidDate();
                            message += "\nPlease pay the following to get the item: " + topBid.getPrice() + "kr";


                            //prepare email about the bid
                            SimpleMailMessage mailMessage = new SimpleMailMessage();
                            mailMessage.setTo(topBidderAndWinner.getEmail());
                            mailMessage.setSubject("You won an item");
                            mailMessage.setFrom(emailFrom);
                            mailMessage.setText(message);

                            //send email about the bid
                            emailSenderService.sendEmail(mailMessage);
                        }
                    }
                }
            }
        }, c.getTime(), 1000);
    }

    static class SortByBidPrice implements Comparator<Bid> {
        // Used for sorting in ascending order of
        // roll number
        public int compare(Bid a, Bid b) {
            return (int) (a.getPrice() - b.getPrice());
        }
    }
}

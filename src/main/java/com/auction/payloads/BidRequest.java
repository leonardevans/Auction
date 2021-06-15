package com.auction.payloads;

public class BidRequest {
    private double bidPrice;
    private long auctionItemId;

    public double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public long getAuctionItemId() {
        return auctionItemId;
    }

    public void setAuctionItemId(long auctionItemId) {
        this.auctionItemId = auctionItemId;
    }
}

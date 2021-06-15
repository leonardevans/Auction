package com.auction.controller;

import com.auction.model.AuctionItem;
import com.auction.model.Bid;
import com.auction.model.Category;
import com.auction.payloads.BidRequest;
import com.auction.repo.AuctionItemRepo;
import com.auction.repo.CategoryRepo;
import com.auction.security.AuthUtil;
import com.auction.services.AuctionItemService;
import com.auction.services.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
public class AuctionItemController {
    @Autowired
    AuctionItemRepo auctionItemRepo;

    @Autowired
    AuctionItemService auctionItemService;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    BidService bidService;

    //show single items
    @GetMapping("/item/{id}")
    public String showItem(@PathVariable("id") long id, Model model){
        Optional<AuctionItem> optionalAuctionItem = auctionItemRepo.findById(id);
        if (optionalAuctionItem.isEmpty())
            return "redirect:/";

        AuctionItem auctionItem = optionalAuctionItem.get();

        //get the bids and save them to a list
        List<Bid> bids = new ArrayList<>(auctionItem.getBids());

        //sort the list by bid price into descending order
        bids.sort(new BidsController.SortByBidPrice().reversed());

        //this is a list for the top 3 bids
        List<Bid> top3Bids = new ArrayList<>();

        //add 3 bids from all the bids
        int total = 0;
        for (Bid bid : bids
        ) {
            top3Bids.add(bid);
            total ++;
            if (total >= 3){break;}
        }

        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("top3Bids", top3Bids);
        model.addAttribute("auctionItem", optionalAuctionItem.get());
        return "item";
    }

    //show items by category
    @GetMapping("/items/category/{id}")
    public String showByCategory(@PathVariable("id") long id, Model model){
        return getPaginatedItems(id, 1, model);
    }

    //return paginated results for category page
    @GetMapping("/items/{categoryId}/page/{pageNo}")
    public String getPaginatedItems(@PathVariable(value = "categoryId") long categoryId,@PathVariable(value = "pageNo") int pageNo, Model model) {
        int pageSize = 20;

        //get category by id
        Optional<Category> optionalCategory = categoryRepo.findById(categoryId);
        if (optionalCategory.isEmpty())
            return "redirect:/";

        Page<AuctionItem> page = auctionItemService.getAllByCategory(optionalCategory.get(),pageNo, pageSize);
        List< AuctionItem > auctionItems = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("forCategoryId", categoryId);
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("auctionItems", auctionItems);
        return "items_by_category";
    }

    @GetMapping("/item/{id}/bids")
    public String getAllItemsBids(@PathVariable("id") long id, Model model){
        return getPaginatedItemBids(id, 1, model);
    }

    //return paginated results for category page
    @GetMapping("/item/{auctionItemId}/bids/page/{pageNo}")
    public String getPaginatedItemBids(@PathVariable(value = "auctionItemId") long auctionItemId,@PathVariable(value = "pageNo") int pageNo, Model model) {
        int pageSize = 5;

        Optional<AuctionItem> optionalAuctionItem = auctionItemRepo.findById(auctionItemId);
        if (optionalAuctionItem.isEmpty())
            return "redirect:/";

        Page<Bid> page = bidService.getAllAuctionItemBids(optionalAuctionItem.get(),pageNo, pageSize);
        List< Bid > bids = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("auctionItemId", auctionItemId);
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("bids", bids);
        return "item_bids";
    }

    static class SortByBidPrice implements Comparator<Bid> {
        // Used for sorting in ascending order of
        // roll number
        public int compare(Bid a, Bid b) {
            return (int) (a.getPrice() - b.getPrice());
        }
    }
}

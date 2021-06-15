package com.auction.controller;

import com.auction.model.AuctionItem;
import com.auction.model.Bid;
import com.auction.model.User;
import com.auction.payloads.AuctionItemRequest;
import com.auction.repo.AuctionItemRepo;
import com.auction.repo.CategoryRepo;
import com.auction.security.AuthUtil;
import com.auction.services.AuctionItemService;
import com.auction.services.BidService;
import com.auction.services.EmailSenderService;
import com.auction.services.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class AuctionItemsController {
    @Autowired
    AuctionItemRepo auctionItemRepo;

    @Autowired
    AuctionItemService auctionItemService;

    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    FileUploadService fileUploadService;

    @Autowired
    BidService bidService;

    @Autowired
    private EmailSenderService emailSenderService;

    @Value( "${app.emailfrom}" )
    private String emailFrom;

    @GetMapping("/")
    public String showAllAuctionItems(Model model){
        return getPaginatedItems(1, model);
    }

    //return paginated results for home page
    @GetMapping("/items/page/{pageNo}")
    public String getPaginatedItems(@PathVariable(value = "pageNo") int pageNo, Model model) {
        int pageSize = 20;

        Page<AuctionItem> page = auctionItemService.getAll(pageNo, pageSize);
        List< AuctionItem > auctionItems = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("categories", categoryRepo.findAll());
        model.addAttribute("auctionItems", auctionItems);
        return "index";
    }

    //get seller's auction items
    @GetMapping("/seller/items")
    public String showUserAuctionItems(Model model){
        return findPaginatedSellerItems(1, model);
    }

    //get all auction items for the admin
    @GetMapping("/admin/items")
    public String showAdminAuctionItems(Model model){
        return findPaginatedAdminItems(1, model);
    }

    //get all bids placed by user
    @GetMapping("/user/bids")
    public String showAllUserBids(Model model){
        return findPaginatedUserBids(1, model);
    }

    //return paginated results
    @GetMapping("/user/bids/page/{pageNo}")
    public String findPaginatedUserBids(@PathVariable(value = "pageNo") int pageNo, Model model) {
        User loggedInUser = authUtil.getLoggedInUser();
        if (loggedInUser== null)
            return "redirect:/";

        int pageSize = 10;

        Page<Bid> page = bidService.getUserBids(loggedInUser,pageNo, pageSize);
        List< Bid > userBids = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("userBids", userBids);
        return "my_bids";
    }

    //return paginated results
    @GetMapping("/seller/items/page/{pageNo}")
    public String findPaginatedSellerItems(@PathVariable(value = "pageNo") int pageNo, Model model) {
        User loggedInUser = authUtil.getLoggedInUser();
        if (loggedInUser== null)
            return "redirect:/";

        int pageSize = 10;

        Page<AuctionItem> page = auctionItemService.getAllBySeller(loggedInUser,pageNo, pageSize);
        List< AuctionItem > auctionItems = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("auctionItems", auctionItems);
        return "my_items";
    }

    //return paginated results
    @GetMapping("/admin/items/page/{pageNo}")
    public String findPaginatedAdminItems(@PathVariable(value = "pageNo") int pageNo, Model model) {
        User loggedInUser = authUtil.getLoggedInUser();
        if (loggedInUser== null)
            return "redirect:/";

        int pageSize = 10;

        Page<AuctionItem> page = auctionItemService.getAll(pageNo, pageSize);
        List< AuctionItem > auctionItems = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("auctionItems", auctionItems);
        return "all_items";
    }

    //show add category page
    @GetMapping("/seller/add/item")
    public String showAddItem(Model model){
        model.addAttribute("auctionItemRequest", new AuctionItemRequest());
        model.addAttribute("categories", categoryRepo.findAll());
        return "add_item";
    }

    @PostMapping("/seller/add/item")
    public String addItem(@ModelAttribute("auctionItemRequest") AuctionItemRequest auctionItemRequest, Model model) throws IOException, ParseException {
        User loggedInUser = authUtil.getLoggedInUser();
        if (loggedInUser== null)
            return "redirect:/";

        String filename =  fileUploadService.uploadToLocal(auctionItemRequest.getImage(),"uploads/images/items/");
        if (filename == null){
            return  "redirect:/seller/items?add_error";
        }

//        Date date1=new SimpleDateFormat("yyyy-MM-dd").parse(auctionItemRequest.getClosingDate());

        AuctionItem auctionItem = new AuctionItem(auctionItemRequest.getName(), auctionItemRequest.getDescription(), auctionItemRequest.getStartingBid(), auctionItemRequest.getClosingDate(),auctionItemRequest.getCategory(),loggedInUser,"/"+filename);
        auctionItem.setClosed(auctionItemRequest.isClosed());

        AuctionItem savedAuctionItem = auctionItemRepo.save(auctionItem);

        //prepare email message
        String message = "Your auction item was added successfully.";
        message += "\nAuction item: " + savedAuctionItem.getName();
        message += "\nCategory: " + savedAuctionItem.getCategory().getName();
        message += "\nStarting bid amount: " + savedAuctionItem.getStartingBid();
        message += "\nClosing date: " + savedAuctionItem.getClosingDate();
        message += "\nDescription: \n" + savedAuctionItem.getDescription();


        //prepare email about the auction item
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(loggedInUser.getEmail());
        mailMessage.setSubject("New Auction item added");
        mailMessage.setFrom(emailFrom);
        mailMessage.setText(message);

        //send email about the auction item
        emailSenderService.sendEmail(mailMessage);

        return "redirect:/seller/items?add_success";
    }

    //show add category page
    @GetMapping("/seller/edit/item/{id}")
    public String showEditItem(@PathVariable("id") long id, Model model){
        Optional<AuctionItem> optionalAuctionItem = auctionItemRepo.findById(id);
        if (optionalAuctionItem.isEmpty())
            return "redirect:/seller/items?not_found";

        AuctionItem auctionItem = optionalAuctionItem.get();

        AuctionItemRequest auctionItemRequest = new AuctionItemRequest();
        auctionItemRequest.setName(auctionItem.getName());
        auctionItemRequest.setCategory(auctionItem.getCategory());
        auctionItemRequest.setClosingDate(auctionItem.getClosingDate());
        auctionItemRequest.setDescription(auctionItem.getDescription());
        auctionItemRequest.setId(auctionItem.getId());
        auctionItemRequest.setImageUrl(auctionItem.getImage());
        auctionItemRequest.setStartingBid(auctionItem.getStartingBid());
        auctionItemRequest.setClosed(auctionItem.isClosed());

        model.addAttribute("auctionItemRequest", auctionItemRequest);
        model.addAttribute("categories", categoryRepo.findAll());
        return "edit_item";
    }

    @PostMapping("/seller/update/item")
    public String updateItem(@ModelAttribute("auctionItemRequest") AuctionItemRequest auctionItemRequest, Model model) throws IOException, ParseException {
        User loggedInUser = authUtil.getLoggedInUser();
        if (loggedInUser== null)
            return "redirect:/";

        Optional<AuctionItem> optionalAuctionItem = auctionItemRepo.findById(auctionItemRequest.getId());
        if (optionalAuctionItem.isEmpty()){
            return  "redirect:/seller/items?not_found";
        }

        AuctionItem auctionItem = optionalAuctionItem.get();


        String filename = null;
        if (!auctionItemRequest.getImage().isEmpty()){
            filename =  fileUploadService.uploadToLocal(auctionItemRequest.getImage(),"uploads/images/items/");
            if (filename == null){
                return  "redirect:/seller/items?add_error";
            }

            fileUploadService.deleteLocalFile(auctionItem.getImage().substring(1));
        }



//        Date date1=new SimpleDateFormat("yyyy-MM-dd").parse();


        auctionItem.setName(auctionItemRequest.getName());
        auctionItem.setCategory(auctionItemRequest.getCategory());
        auctionItem.setClosingDate(auctionItemRequest.getClosingDate());
        auctionItem.setDescription(auctionItemRequest.getDescription());
        auctionItem.setStartingBid(auctionItemRequest.getStartingBid());
        auctionItem.setClosed(auctionItemRequest.isClosed());

        if (filename != null)
            auctionItem.setImage("/"+filename);

        AuctionItem updatedAuctionItem = auctionItemRepo.save(auctionItem);

        //prepare email message
        String message = "Your auction item was updated successfully.";
        message += "\nAuction item: " + updatedAuctionItem.getName();
        message += "\nCategory: " + updatedAuctionItem.getCategory().getName();
        message += "\nStarting bid amount: " + updatedAuctionItem.getStartingBid();
        message += "\nClosing date: " + updatedAuctionItem.getClosingDate();
        message += "\nDescription: \n" + updatedAuctionItem.getDescription();


        //prepare email about the auction item
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(loggedInUser.getEmail());
        mailMessage.setSubject("Auction item updated");
        mailMessage.setFrom(emailFrom);
        mailMessage.setText(message);

        //send email about the auction item
        emailSenderService.sendEmail(mailMessage);

        return "redirect:/seller/items?update_success";
    }

    @GetMapping("/seller/delete/item/{id}")
    public String deleteItem(@PathVariable("id") long id) throws IOException {
        Optional<AuctionItem> optionalAuctionItem = auctionItemRepo.findById(id);
        if (optionalAuctionItem.isPresent()){
            fileUploadService.deleteLocalFile(optionalAuctionItem.get().getImage().substring(1));
        }
        auctionItemRepo.deleteById(id);;
        return "redirect:/seller/items?delete_success";
    }
}

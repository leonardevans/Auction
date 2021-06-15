package com.auction.controller;

import com.auction.model.Category;
import com.auction.repo.AuctionItemRepo;
import com.auction.repo.CategoryRepo;
import com.auction.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class CategoriesController {
    @Autowired
    CategoryRepo categoryRepo;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AuctionItemRepo auctionItemRepo;

    //show all categories to admin
    @GetMapping("/admin/categories")
    public String showCategories(Model model){
        return findPaginated(1, model);
    }

    //return paginated results
    @GetMapping("/categories/page/{pageNo}")
    public String findPaginated(@PathVariable(value = "pageNo") int pageNo, Model model) {
        int pageSize = 10;

        Page<Category> page = categoryService.findPaginated(pageNo, pageSize);
        List< Category > categories = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("categories", categories);
        return "categories";
    }

    //show add category page
    @GetMapping("/admin/add/category")
    public String showAddCategory(Model model){
        model.addAttribute("category", new Category());
        return "add_category";
    }

    //show edit category page by id
    @GetMapping("/admin/edit/category/{id}")
    public String showEditCategory(@PathVariable long id, Model model){
        Optional<Category> optionalCategory = categoryRepo.findById(id);
        //check is such category exists if not return to all categories page
        if (optionalCategory.isEmpty()){
            return "redirect:/admin/categories?not_found";
        }

        model.addAttribute("category", optionalCategory.get());
        return "edit_category";
    }

    //add new category
    @PostMapping("/admin/add/category")
    public String addCategory(@ModelAttribute("category") Category category){
        Optional<Category> optionalCategory = categoryRepo.findByName(category.getName());
        if (optionalCategory.isPresent()){
            return "redirect:/admin/categories?exist";
        }

        categoryRepo.save(category);
        return "redirect:/admin/categories?add_success";
    }

    @PostMapping("/admin/update/category")
    public String updateCategory(@ModelAttribute("category") Category category){
        Optional<Category> optionalCategory = categoryRepo.findByName(category.getName());
        //check if there is another category with such name
        if (optionalCategory.isPresent()){
            if (optionalCategory.get().getId() != category.getId())
                return "redirect:/admin/categories?exist";
        }

        categoryRepo.save(category);
        return "redirect:/admin/categories?update_success";
    }

    //delete category by id
    @GetMapping("/admin/delete/category/{id}")
    public String deleteCategory(@PathVariable("id") long id){
        if (!categoryRepo.existsById(id)){
            return "redirect:/admin/categories?not_found";
        }

        //check if there are auction items in this category
        if (auctionItemRepo.existsByCategory(categoryRepo.findById(id).get())){
            return "redirect:/admin/categories?auction_item_here";
        }

        categoryRepo.deleteById(id);

        return "redirect:/admin/categories?delete_success";
    }
}

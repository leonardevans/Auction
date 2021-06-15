package com.auction.controller;

import com.auction.model.User;
import com.auction.repo.RoleRepository;
import com.auction.repo.UserRepository;
import com.auction.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class UsersController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserService userService;

    //show all users to admin
    @GetMapping("/admin/users")
    public String showUsers(Model model){
        return findPaginated(1, model);
    }

    //return paginated results
    @GetMapping("/users/page/{pageNo}")
    public String findPaginated(@PathVariable(value = "pageNo") int pageNo, Model model) {
        int pageSize = 10;

        Page < User > page = userService.findPaginated(pageNo, pageSize);
        List < User > users = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("users", users);
        return "users";
    }

    //show edit user page by id
    @GetMapping("/admin/edit/user/{id}")
    public String showEditUser(@PathVariable long id, Model model){
        Optional<User> optionalUser = userRepository.findById(id);
        //check is such user exists if not return to all users page
        if (optionalUser.isEmpty()){
            return "redirect:/admin/users?not_found";
        }

        model.addAttribute("user", optionalUser.get());
        model.addAttribute("allRoles", roleRepository.findAll());

        return "edit_user";
    }

    //show add user page
    @GetMapping("/admin/add/user")
    public String showAddUser(Model model){
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleRepository.findAll());
        return "add_user";
    }

    //add new user
    @PostMapping("/admin/add/user")
    public String addUser(@ModelAttribute("user") User user){
        Optional<User> optionalUser = userRepository.findByEmail(user.getEmail());
        if (optionalUser.isPresent()){
            return "redirect:/admin/users?exist";
        }

        userRepository.save(user);
        return "redirect:/admin/users?add_success";
    }

    @PostMapping("/admin/update/user")
    public String updateUser(@ModelAttribute("user") User user){
        Optional<User> optionalUser = userRepository.findById(user.getId());
        if (optionalUser.isEmpty()){
            return "redirect:/admin/users?not_found";
        }

        User userToSave = optionalUser.get();
        userToSave.setRoles(user.getRoles());

        userRepository.save(userToSave);
        return "redirect:/admin/users?update_success";
    }

    //I don't think it's good to delete users, we should disable their accounts or deny them some roles
//    @GetMapping("/admin/delete/user/{id}")
//    public String deleteUser(@PathVariable("id") long id){
//        if (!userRepository.existsById(id)){
//            return "redirect:/admin/users?not_found";
//        }
//
//        userRepository.deleteById(id);
//        return "redirect:/admin/users?delete_success";
//    }
}

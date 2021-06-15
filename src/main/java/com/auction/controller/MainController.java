package com.auction.controller;

import com.auction.model.User;
import com.auction.repo.RoleRepository;
import com.auction.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class MainController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    //show login page
    @GetMapping("/login")
    public String login(){
        return "login";
    }

}

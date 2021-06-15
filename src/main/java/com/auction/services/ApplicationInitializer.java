package com.auction.services;

import com.auction.model.Role;
import com.auction.model.User;
import com.auction.repo.RoleRepository;
import com.auction.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ApplicationInitializer implements CommandLineRunner {
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        //check if BIDDER role exists, if not create it and save
        Optional<Role> optionalRole = roleRepository.findByName("ROLE_BIDDER");
        if (optionalRole.isEmpty()){
            roleRepository.save(new Role("ROLE_BIDDER"));
        }

        //check if SELLER role exists, if not create it and save
        Optional<Role> optionalRole1 = roleRepository.findByName("ROLE_SELLER");
        if (optionalRole1.isEmpty()){
            roleRepository.save(new Role("ROLE_SELLER"));
        }

        //check if ADMIN role exists, if not create it and save
        Optional<Role> optionalRole2 = roleRepository.findByName("ROLE_ADMIN");
        if (optionalRole2.isEmpty()){
            roleRepository.save(new Role("ROLE_ADMIN"));
        }

        //check if BIDDER role exists, if not create it and save
        Optional<Role> optionalRole3 = roleRepository.findByName("ROLE_USER");
        if (optionalRole3.isEmpty()){
            roleRepository.save(new Role("ROLE_USER"));
        }

        try
        {
            Thread.sleep(3000);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }

        String adminEmail = "bizbedu@gmail.com";
        // check if there's admin user, if not add user with role admin
        if (!userRepository.existsByEmail(adminEmail)){
            User admin = new User("admin", adminEmail, "");

            //            get user role and add this role to admin
            Optional<Role> userRole = roleRepository.findByName("ROLE_USER");
            userRole.ifPresent(role -> admin.getRoles().add(role));

//            get admin role and add this role to admin
            Optional<Role> adminRole = roleRepository.findByName("ROLE_ADMIN");
            adminRole.ifPresent(role -> admin.getRoles().add(role));

//            save this user
            userRepository.save(admin);
        }
    }
}

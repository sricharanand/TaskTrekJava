package com.javaproject.java_project.service;

import com.javaproject.java_project.model.User;
import com.javaproject.java_project.repositories.UsersRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class AuthService
{
    @Autowired
    UsersRepository usersRepository;

    // In memory Database for now, Mongo later
    //private List<User> users = usersRepository.find;
    private int nextID = 1; // autoincrement this for registering users
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(16);

    // helper so other services can know who is logged in
    // currently logged-in user (set on successful login)
    @Getter
    private User currentUser;

    public User registerUser(String username, String password)
    {
        boolean usernameAlreadyExists = usersRepository.usernameExists(username);

        if (usernameAlreadyExists)
        {
            System.out.println("Username taken.");
            return null;
        }

        User newuser = User.builder()
                .id(nextID)
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .build();
        nextID++;

        return usersRepository.insert(newuser);
    }
        // check if username already exists, if so, return null (username taken)
        // if fine, create new user with the ID, username, passwordHash (just store the normal pwd for now)
        // lvl = 1, xp = 0 (see constructors with lombok...)
        // User object should match the model, add to arraylist
        // return the user object

    public User loginUser(String username, String password)
    {
        User user = usersRepository.findByUsername(username);

        if (user == null)
        {
            System.out.println("User not found");
            return null;
        }

        if (passwordEncoder.matches(password, user.getPasswordHash()))
        {
            currentUser = user; // Set logged-in user
            return user;
        }
        else
        {
            System.out.println("Incorrect password");
            return null;
        }
    }

        // check if user exists in the list, if not just null
        // If exists check if pwd matches what's there in the users list (simple string comp for now)
        // Match => return user
        // Else => Exception
}

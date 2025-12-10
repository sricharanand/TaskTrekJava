package com.javaproject.java_project.service;

import com.javaproject.java_project.model.User;
import com.javaproject.java_project.repository.UsersRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService
{
    UsersRepository usersRepository;
    // In Mongo now
    private final List<User> users = null; //new ArrayList<>();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private File dataFile;

    private int nextID = 1; // autoincrement this for registering user

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(16);

    // helper so other services can know who is logged in
    // currently logged-in user (set on successful login)
    @Getter
    private User currentUser;


    // Runs on startup
    @PostConstruct
    public void init()
    {
        // Store file in project root (or use a path from application.properties)
        dataFile = new File("users.json");

        try
        {
            // If the file exists and has data, load it in
            if (dataFile.exists() && dataFile.length() > 0) {
                List<User> loaded = loadUsers();

                // sync into the in-memory list
                users.clear();
                users.addAll(usersRepository.findAll());
                //users.addAll(loaded);


                nextID = users.stream()
                        .mapToInt(User::getId)
                        .max()
                        .orElse(0)
                        + 1;
            }
            // File missing or empty
            else
                saveUsers(); // gives an empty []
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // ------------------ File Helpers ------------------

    // Load
    private synchronized List<User> loadUsers() throws IOException
    {
        if (!dataFile.exists() || dataFile.length() == 0)
            return new ArrayList<>();

        // File Class method to read
        byte[] jsonData = Files.readAllBytes(dataFile.toPath());
        return objectMapper.readValue(jsonData, new TypeReference<List<User>>() {});
    }

    // Save
    // AT THE END OF EVERY METHOD INVOLVING SOME FILE I/O
    synchronized void saveUsers() {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, users);
    }


    // ------------------ CRUD Methods ------------------

    public User registerUser(String username, String password)
    {
        for (User user : users)
        {
            if (user.getUsername().equals(username))
            {
                System.out.println("Username taken.");
                return null;
            }
        }

        User newUser = User.builder()
                .id(nextID)
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .build();
        nextID++;

        users.add(newUser);
        saveUsers();
        usersRepository.saveAll(users);
        nextID++;
        return newUser;
    }


    // check if username already exists, if so, return null (username taken)
    // if fine, create new user with the ID, username, passwordHash (just store the normal pwd for now)
    // lvl = 1, xp = 0 (see constructors with lombok...)
    // User object should match the model, add to arraylist
    // return the user object

    public User loginUser(String username, String password)
    {
        User user = null;

        for (User check : users)
        {
            if (check.getUsername().equals(username)) {
                user = check;
                break;
            }
        }

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
    // If exists, check if pwd matches what's there in the users list (simple string comp for now)
    // Match => return user
    // Else => Exception
}
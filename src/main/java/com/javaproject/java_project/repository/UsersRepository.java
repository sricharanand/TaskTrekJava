package com.javaproject.java_project.repository;
import com.javaproject.java_project.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UsersRepository extends MongoRepository<User, Integer> {
    List<User> findById(int id);
    List<User> findByUsername(String username);
}

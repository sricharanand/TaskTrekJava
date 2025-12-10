package com.javaproject.java_project.repository;
import com.javaproject.java_project.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface UsersRepository extends MongoRepository<User, Integer> {
    User findByUsername(String username);

}

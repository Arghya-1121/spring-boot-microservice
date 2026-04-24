package com.example.backend.service;

import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public Optional<User> findUser(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUser(long id) {
        return userRepository.findById(Long.valueOf(id));
    }

    public Optional<User> findUserName(String username) {
        return userRepository.findByUsername(username);
    }

    public Long exist(String name) {
        var user = findUserName(name);
        if (user.isPresent()) return user.get().getId();
        return null;
    }

}

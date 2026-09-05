package com.project.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.app.entities.User;

public interface UserRepo extends JpaRepository<User,Integer> {

    
} 
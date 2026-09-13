package com.project.app.repositories;

import com.project.app.entities.Category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepo extends JpaRepository <Category, Integer > {

    
}

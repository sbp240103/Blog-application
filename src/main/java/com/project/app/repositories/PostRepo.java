package com.project.app.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.project.app.entities.Category;
import com.project.app.entities.Post;
import com.project.app.entities.User;

public interface PostRepo extends JpaRepository<Post, Integer> {

    Page<Post> findByUser(User user, Pageable pageable);

    Page<Post> findByCategory(Category category, Pageable pageable);

    Page<Post> findByTagsName(String tagName, Pageable pageable);

    @Query ("select p from Post p where p.title like :key")
    List<Post> searchByTitle(String key);

}

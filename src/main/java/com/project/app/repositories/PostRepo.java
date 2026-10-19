package com.project.app.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.project.app.entities.Category;
import com.project.app.entities.Post;
import com.project.app.entities.User;

public interface PostRepo extends JpaRepository<Post, Integer> {

    Page<Post> findByUser(User user, Pageable pageable);

    Page<Post> findByCategory(Category category, Pageable pageable);

    Page<Post> findByTagsName(String tagName, Pageable pageable);

    // atomic increment so concurrent readers never lose a view
    @Modifying
    @Transactional
    @Query ("update Post p set p.viewCount = p.viewCount + 1 where p.postId = :postId")
    void incrementViewCount(@Param("postId") Integer postId);

    @Query ("select p from Post p where p.title like :key")
    List<Post> searchByTitle(String key);

}

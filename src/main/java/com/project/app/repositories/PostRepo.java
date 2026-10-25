package com.project.app.repositories;

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

    // posts written by everyone the given user follows
    @Query ("select p from Post p where p.user in (select f.following from Follow f where f.follower = :user)")
    Page<Post> findFeed(@Param("user") User user, Pageable pageable);

    // atomic increment so concurrent readers never lose a view
    @Modifying
    @Transactional
    @Query ("update Post p set p.viewCount = p.viewCount + 1 where p.postId = :postId")
    void incrementViewCount(@Param("postId") Integer postId);

    // case insensitive match on title or content
    @Query ("select p from Post p where lower(p.title) like lower(concat('%', :keyword, '%')) "
            + "or lower(p.content) like lower(concat('%', :keyword, '%'))")
    Page<Post> search(@Param("keyword") String keyword, Pageable pageable);

}

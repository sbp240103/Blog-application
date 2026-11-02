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
import com.project.app.entities.PostStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepo extends JpaRepository<Post, Integer> {

    Optional<Post> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Post> findByUser(User user, Pageable pageable);

    Page<Post> findByUserAndStatus(User user, PostStatus status, Pageable pageable);

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    List<Post> findByStatusAndPublishAtLessThanEqual(PostStatus status, LocalDateTime time);

    Page<Post> findByCategoryAndStatus(Category category, PostStatus status, Pageable pageable);

    Page<Post> findByTagsNameAndStatus(String tagName, PostStatus status, Pageable pageable);

    // posts written by everyone the given user follows
    @Query ("select p from Post p where p.status = com.project.app.entities.PostStatus.PUBLISHED "
            + "and p.user in (select f.following from Follow f where f.follower = :user)")
    Page<Post> findFeed(@Param("user") User user, Pageable pageable);

    // atomic increment so concurrent readers never lose a view
    @Modifying
    @Transactional
    @Query ("update Post p set p.viewCount = p.viewCount + 1 where p.postId = :postId")
    void incrementViewCount(@Param("postId") Integer postId);

    // case insensitive match on title or content
    @Query ("select p from Post p where p.status = com.project.app.entities.PostStatus.PUBLISHED "
            + "and (lower(p.title) like lower(concat('%', :keyword, '%')) "
            + "or lower(p.content) like lower(concat('%', :keyword, '%')))")
    Page<Post> search(@Param("keyword") String keyword, Pageable pageable);

}

package com.project.app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.app.entities.Comments;
import com.project.app.entities.Post;
import com.project.app.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepo extends JpaRepository<Comments, Integer> {

    List<Comments> findByPostOrderByIdAsc(Post post);

    long countByPost(Post post);

    // comments written on any post of the given user
    long countByPostUser(User user);

    List<Comments> findByParentOrderByIdAsc(Comments parent);

    Page<Comments> findByPostAndParentIsNull(Post post, Pageable pageable);

    long countByParent(Comments parent);

    void deleteByUser(User user);

}

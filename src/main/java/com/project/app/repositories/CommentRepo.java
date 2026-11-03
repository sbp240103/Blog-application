package com.project.app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.app.entities.Comments;
import com.project.app.entities.Post;
import com.project.app.entities.User;

public interface CommentRepo extends JpaRepository<Comments, Integer> {

    List<Comments> findByPostOrderByIdAsc(Post post);

    long countByPost(Post post);

    List<Comments> findByParentOrderByIdAsc(Comments parent);

    void deleteByUser(User user);

}

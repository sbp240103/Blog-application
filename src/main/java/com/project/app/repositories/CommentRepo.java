package com.project.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.app.entities.Comments;
import com.project.app.entities.User;

public interface CommentRepo extends JpaRepository<Comments, Integer> {

    void deleteByUser(User user);

}

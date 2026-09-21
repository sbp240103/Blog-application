package com.project.app.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.app.entities.Comments;

public interface CommentRepo extends JpaRepository<Comments, Integer> {

}

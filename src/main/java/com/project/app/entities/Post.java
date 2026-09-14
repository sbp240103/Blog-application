package com.project.app.entities;

import java.sql.Date;

import com.project.app.entities.User;

import com.project.app.entities.Category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table (name = "post")
public class Post {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY )
    private Integer postId;

    @Column (name = "post_title", length = 100, nullable = false)
    private String title;

    @Column (length = 1000)
    private String content;

    private String imageName;

    private Date addDate;

    @ManyToOne 
    private Category category;

    @ManyToOne 
    private User user;

}

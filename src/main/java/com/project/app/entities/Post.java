package com.project.app.entities;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

import com.project.app.entities.User;

import com.project.app.entities.Category;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "post")
@NoArgsConstructor 
@Getter 
@Setter 
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
    @JoinColumn (name = "category_id")
    private Category category;

    @ManyToOne 
    private User user;

    @OneToMany (mappedBy = "post", cascade = CascadeType.ALL)
    private Set<Comments> comments = new HashSet<>();

}

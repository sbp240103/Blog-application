package com.project.app.entities;

import org.hibernate.annotations.ManyToAny;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "comments")
@Getter 
@Setter 
@NoArgsConstructor 
public class Comments {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY )
    private int Id;
    
    private String content;

    @ManyToOne ()
    private Post post;


}

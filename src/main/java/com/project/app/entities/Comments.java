package com.project.app.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

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

    // author of the comment
    @ManyToOne
    private User user;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // set when this comment is a reply, only one level of nesting is used
    @ManyToOne
    private Comments parent;

    // replies are removed together with the comment they answer
    @OneToMany (mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comments> replies = new HashSet<>();

}

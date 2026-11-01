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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Version;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

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

    // how many times the post was opened, defaults to 0 for existing rows
    @Column (nullable = false, columnDefinition = "int default 0")
    private Integer viewCount = 0;

    // existing rows become published so nothing disappears
    @Enumerated (EnumType.STRING)
    @Column (nullable = false, length = 20, columnDefinition = "varchar(20) default 'PUBLISHED'")
    private PostStatus status = PostStatus.PUBLISHED;

    // last time the post content was changed, view count updates do not touch it
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // optimistic locking, two people saving the same post at once can not overwrite each other
    @Version
    @Column (nullable = false, columnDefinition = "bigint default 0")
    private Long version = 0L;

    @ManyToOne 
    @JoinColumn (name = "category_id")
    private Category category;

    @ManyToOne 
    private User user;

    @OneToMany (mappedBy = "post", cascade = CascadeType.ALL)
    private Set<Comments> comments = new HashSet<>();

    @ManyToMany
    @JoinTable (
        name = "post_tags",
        joinColumns = @JoinColumn (name = "post_id"),
        inverseJoinColumns = @JoinColumn (name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // likes are removed together with the post
    @OneToMany (mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PostLike> likes = new HashSet<>();

    // bookmarks of a deleted post go away with it
    @OneToMany (mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Bookmark> bookmarks = new HashSet<>();

}

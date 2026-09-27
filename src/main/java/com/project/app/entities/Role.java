package com.project.app.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "roles")
@NoArgsConstructor 
@AllArgsConstructor 
@Getter 
@Setter 
public class Role {

    @Id 
    private Integer id;

    private String name;

}

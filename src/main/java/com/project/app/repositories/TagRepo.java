package com.project.app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.app.entities.Tag;

public interface TagRepo extends JpaRepository<Tag, Integer> {

    Optional<Tag> findByName(String name);

}

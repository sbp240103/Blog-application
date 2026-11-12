package com.project.app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.app.entities.Tag;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import com.project.app.payloads.TagDto;

public interface TagRepo extends JpaRepository<Tag, Integer> {

    Optional<Tag> findByName(String name);

    @Query("select new com.project.app.payloads.TagDto(t.id, t.name, count(p)) from Post p join p.tags t "
            + "where p.status = com.project.app.entities.PostStatus.PUBLISHED group by t.id, t.name order by count(p) desc")
    List<TagDto> findPopular(Pageable pageable);

}

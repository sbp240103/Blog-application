package com.project.app.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.project.app.payloads.TagDto;
import com.project.app.repositories.TagRepo;
import com.project.app.services.TagService;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepo tagRepo;

    // alphabetical list of every tag in use
    @Override
    public List<TagDto> getAllTags() {
        return this.tagRepo.findAll(Sort.by("name")).stream()
                .map(tag -> new TagDto(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
    }

}

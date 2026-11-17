package com.project.app.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.project.app.payloads.TagDto;
import com.project.app.repositories.TagRepo;
import com.project.app.services.TagService;
import org.springframework.data.domain.PageRequest;
import com.project.app.exceptions.ApiException;
import org.springframework.cache.annotation.Cacheable;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepo tagRepo;

    // tags used by the most published posts
    @Override
    @Cacheable(value = "popularTags", key = "#limit")
    public List<TagDto> getPopularTags(Integer limit) {
        if (limit == null || limit < 1 || limit > 50) {
            throw new ApiException("limit must be between 1 and 50");
        }
        return this.tagRepo.findPopular(PageRequest.of(0, limit));
    }

    // alphabetical list of every tag in use
    @Override
    @Cacheable("tags")
    public List<TagDto> getAllTags() {
        return this.tagRepo.findAll(Sort.by("name")).stream()
                .map(tag -> new TagDto(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
    }

}

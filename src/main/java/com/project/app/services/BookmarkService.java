package com.project.app.services;

import com.project.app.payloads.PageResponse;
import com.project.app.payloads.PostDto;

public interface BookmarkService {

    void bookmarkPost(Integer postId);

    void removeBookmark(Integer postId);

    PageResponse<PostDto> getMyBookmarks(Integer pageNumber, Integer pageSize);

}

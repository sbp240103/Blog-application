package com.project.app.config;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.app.entities.Post;
import com.project.app.entities.PostStatus;
import com.project.app.repositories.PostRepo;

// publishes scheduled drafts once their publishAt time has passed
@Component
public class PostPublishScheduler {

    private static final Logger log = LoggerFactory.getLogger(PostPublishScheduler.class);

    @Autowired
    private PostRepo postRepo;

    @Scheduled(fixedRateString = "${app.scheduler.publish-check-ms:60000}")
    public void publishDuePosts() {
        List<Post> due = this.postRepo.findByStatusAndPublishAtLessThanEqual(PostStatus.DRAFT, LocalDateTime.now());
        for (Post post : due) {
            post.setStatus(PostStatus.PUBLISHED);
            post.setAddDate(new Date(System.currentTimeMillis()));
            post.setPublishAt(null);
        }
        if (!due.isEmpty()) {
            this.postRepo.saveAll(due);
            log.info("published {} scheduled post(s)", due.size());
        }
    }
}

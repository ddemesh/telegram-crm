package io.dima.telegram.provider.redit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document
public class RedditPost {
    @Id
    private String id;
    private String title;
    private String url;
    private boolean video;

}

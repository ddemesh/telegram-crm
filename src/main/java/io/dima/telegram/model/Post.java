package io.dima.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class Post {
    @Id
    private String id;
    private String text;
    private boolean watched;
    private boolean posted;
    private boolean video;
    private String media;//todo change to binary format and split to different types
}

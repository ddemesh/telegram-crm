package io.dima.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.PriorityQueue;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class Channel {
    @Id
    private String id;

    @Indexed(unique=true)
    private String chatIdentifier;

    @Indexed(unique=true)
    private String name;

    @NotNull
    private List<DataSchedule> dataSchedules;

    @NotNull
    private List<PostSchedule> postSchedules;

    @NotNull
    @DBRef
    private PriorityQueue<FuturePost> futurePosts;

    @NotNull
    @DBRef
    private List<Post> posts;

    @NotNull
    @DBRef
    private Bot bot;
}

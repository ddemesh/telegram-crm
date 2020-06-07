package io.dima.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class FuturePost implements Comparable<FuturePost> {
    @Id
    private String id;
    private Date date;
    @DBRef
    private Post post;

    @CreatedDate
    private Date createdAt;

    @Override
    public int compareTo(FuturePost o) {
        return createdAt.compareTo(o.createdAt);
    }
}

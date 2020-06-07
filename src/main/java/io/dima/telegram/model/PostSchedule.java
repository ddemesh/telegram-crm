package io.dima.telegram.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Document
public class PostSchedule {
    @Id
    private UUID id = UUID.randomUUID();
    private List<Long> delays = new ArrayList<>();
    private List<Integer> times = new ArrayList<>();
}

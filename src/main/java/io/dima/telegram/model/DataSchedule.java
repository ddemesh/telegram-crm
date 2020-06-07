package io.dima.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Document
public class DataSchedule {
    @EqualsAndHashCode.Include
    @Id
    private UUID id = UUID.randomUUID();
    private List<Long> delays = new ArrayList<>();
    private List<Integer> times = new ArrayList<>();
    private boolean automated;
    @NotEmpty
    private String url;
//    todo implement and change to enum
    private String type;
//    @NotEmpty
//    todo replace with additional data as it's not used by reddit
    private String method;
}

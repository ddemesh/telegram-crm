package io.dima.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ScheduleInfo<T> {
    @EqualsAndHashCode.Include
    private T triggerTarget;
    private List<Long> delays;
    private List<Integer> times;
}

package io.dima.telegram.service.impl;

import io.dima.telegram.model.ScheduleInfo;
import io.dima.telegram.service.SchedulingService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
public class SchedulingServiceImpl implements SchedulingService {
    private Map<ScheduleInfo<?>, ScheduleConsumer> schedules = new ConcurrentHashMap<>();

    private final TaskScheduler taskScheduler;

    public SchedulingServiceImpl(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    private class ScheduleConsumer<T> implements Consumer<FluxSink<T>> {
        private ScheduleInfo<T> schedule;
        private FluxSink<T> fluxSink;
        private int timeIndex;
        private List<Long> times = new ArrayList<>();
        private T triggerTarget;

        public ScheduleConsumer(ScheduleInfo<T> scheduleInfo) {
            this.schedule = scheduleInfo;
            this.triggerTarget = schedule.getTriggerTarget();
        }

        @Override
        public void accept(FluxSink<T> fluxSink) {
            this.fluxSink = fluxSink;
            buildTimes();
            findNearestTime();
            delayedTrigger();
        }

        public void complete() {
            fluxSink.complete();
            fluxSink = null;
        }

        private void buildTimes() {
            List<Integer> times = schedule.getTimes();
            List<Long> delays = schedule.getDelays();
            int delayIndex = 0;
            long summaryTime = 0;
            long maxTime = TimeUnit.HOURS.toMillis(24);
            while (summaryTime < maxTime) {
                summaryTime += delays.get(delayIndex);
                this.times.add(summaryTime);
                delayIndex = (delayIndex + 1) % delays.size();
            }
            for (Integer time : times) {
                long longTime = time;
                this.times.add(longTime);
            }
            this.times = new ArrayList<>(new TreeSet<>(this.times));
        }

        private void findNearestTime() {
            List<Long> times = this.times;
            Instant now = Instant.now();
            for (int i = 0; i < times.size(); i++) {
                if (now.truncatedTo(ChronoUnit.DAYS).plusMillis(times.get(i)).isAfter(now)) {
                    timeIndex = i;
                    break;
                }
            }
        }

        private void delayedTrigger() {
            if (fluxSink == null) return;
            fluxSink.next(triggerTarget);
            Instant time = calculateMinDelay();
            taskScheduler.schedule(this::delayedTrigger, time);
        }

        private Instant calculateMinDelay() {
            long time = times.get(timeIndex);

            Instant timeSchedule = Instant
                    .now()
                    .truncatedTo(ChronoUnit.DAYS)
                    .plusMillis(time);
            timeIndex = (timeIndex + 1)  % times.size();

            return timeSchedule;
        }
    }

    @Override
    public <T> Flux<T> scheduleNext(ScheduleInfo<T> scheduleInfo) {
        ScheduleConsumer<T> consumer = new ScheduleConsumer<T>(scheduleInfo);
        Flux<T> flux = Flux.create(consumer);
        ScheduleConsumer<T> previous = schedules.put(scheduleInfo, consumer);
        completeIfNotNull(previous);
        return flux;
    }

    @Override
    public <T> void stop(ScheduleInfo<T> scheduleInfo) {
        ScheduleConsumer<T> schedule = schedules.remove(scheduleInfo);
        completeIfNotNull(schedule);
    }

    private <T> void completeIfNotNull(ScheduleConsumer<T> schedule) {
        if (schedule != null) {
            schedule.complete();
        }
    }
}

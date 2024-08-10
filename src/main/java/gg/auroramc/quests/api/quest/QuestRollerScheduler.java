package gg.auroramc.quests.api.quest;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import gg.auroramc.quests.AuroraQuests;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class QuestRollerScheduler {
    private final QuestPool pool;
    private Scheduler scheduler;
    private JobDetail job;
    private Trigger trigger;
    private ExecutionTime executionTime;
    @Getter
    private volatile boolean valid = false;
    private AtomicReference<ZonedDateTime> nextExecutionTime = new AtomicReference<>(null);

    public QuestRollerScheduler(QuestPool pool) {
        this.pool = pool;
        try {
            this.scheduler = StdSchedulerFactory.getDefaultScheduler();
            var config = pool.getConfig();
            String cronExpression = config.getResetFrequency();

            this.job = JobBuilder.newJob(QuestRollJob.class)
                    .withIdentity(pool.getId() + "-QuestRollJob")
                    .build();

            this.trigger = TriggerBuilder.newTrigger()
                    .withIdentity(pool.getId() + "-QuestRollTrigger")
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            CronDefinition definition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
            CronParser parser = new CronParser(definition);
            Cron cron = parser.parse(cronExpression);
            this.executionTime = ExecutionTime.forCron(cron);

            scheduler.scheduleJob(job, trigger);

            valid = true;
            AuroraQuests.logger().info("Scheduled quest reroll job for pool " + pool.getId() + " with next time: " + new SimpleDateFormat().format(getNextRollDate()));
        } catch (SchedulerException e) {
            AuroraQuests.logger().severe("Failed to start scheduler: " + e.getMessage());
        }
    }

    public Date getNextRollDate() {
        return trigger.getNextFireTime();
    }

    public boolean shouldReroll(Long timestamp) {
        if (timestamp == null) return true;

        ZonedDateTime lastRerollTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        ZonedDateTime now = ZonedDateTime.now();

        // Get the last execution time before now
        Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(now);

        if (lastExecution.isPresent()) {
            ZonedDateTime previousExecutionTime = lastExecution.get();

            // Check if the last reroll time is before the previous execution time
            return lastRerollTime.isBefore(previousExecutionTime);
        } else {
            // If there are no previous executions, assume reroll is needed
            return true;
        }
    }

    public Duration getDurationUntilNextRoll() {
        var next = nextExecutionTime.get();
        if (next != null) {
            ZonedDateTime now = ZonedDateTime.now();
            var duration = Duration.between(now, next);
            if (duration.isNegative()) {
                return Duration.ZERO;
            }
            return duration;
        }

        ZonedDateTime now = ZonedDateTime.now();
        Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);

        if (nextExecution.isPresent()) {
            ZonedDateTime nextExecutionTime = nextExecution.get();
            this.nextExecutionTime.set(nextExecutionTime);
            var duration = Duration.between(now, nextExecutionTime);
            if (duration.isNegative()) {
                return Duration.ZERO;
            }
            return duration;
        } else {
            return Duration.ZERO;
        }
    }


    public class QuestRollJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            Bukkit.getAsyncScheduler().runDelayed(AuroraQuests.getInstance(), (task) -> {
                nextExecutionTime.set(null);
                var players = new ArrayList<>(Bukkit.getOnlinePlayers());

                for (var player : players) {
                    pool.reRollQuests(player, true);
                }
            }, 100, TimeUnit.MILLISECONDS);
        }
    }

    public void shutdown() {
        if (scheduler == null) return;
        if (job == null) return;
        if (trigger == null) return;
        try {
            scheduler.deleteJob(job.getKey());
        } catch (SchedulerException e) {
            AuroraQuests.logger().severe("Failed to remove job from quest poll (" + pool.getId() + ") scheduler: " + e.getMessage());
        }
    }
}

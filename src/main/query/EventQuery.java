package main.query;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public interface EventQuery {
    int getNumberOfEvents(Date after, Date before);

    Set<Event> getAllEvents(Date after, Date before);

    Set<Event> getEventsForIP(String ip, Date after, Date before);

    Set<Event> getEventsForUser(String user, Date after, Date before);

    Set<Event> getFailedEvents(Date after, Date before);

    Set<Event> getErrorEvents(Date after, Date before);

    int getNumberOfAttemptsToCompleteTask(int task, Date after, Date before);

    int getNumberOfSuccessfulAttemptsToCompleteTask(int task, Date after, Date before);

    Map<Integer, Integer> getAllAttemptedTasksAndNumberOfAttempts(Date after, Date before);

    Map<Integer, Integer> getAllCompletedTasksAndNumberOfCompletions(Date after, Date before);
}

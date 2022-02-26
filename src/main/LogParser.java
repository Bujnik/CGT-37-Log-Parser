package main;

import main.query.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogParser implements IPQuery, UserQuery, DateQuery, EventQuery, QLQuery {
    private Path logDir;
    private List<LogEntry> entries;

    public LogParser(Path logDir) {
        this.logDir = logDir;
        entries = new ArrayList<>();
        extractEntries(logDir);
    }

    private void extractEntries(Path logDir) {
        File dir = logDir.toFile();
        File[] dirList = dir.listFiles();

        for (File file : dirList) {
            if (file.isDirectory()) {
                Path newPath = Paths.get(file.getAbsolutePath());
                extractEntries(newPath);
            }
            else {
                if (file.getName().endsWith(".log")){
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))){
                        String line = br.readLine();
                        while (line != null){
                            entries.add(new LogEntry(line));
                            line = br.readLine();
                        }
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    private List<LogEntry> getEntriesByDate(Date after, Date before){
        List<LogEntry> result = new ArrayList<>();
        long afterMs = Long.MIN_VALUE;
        long beforeMs = Long.MAX_VALUE;

        if (after != null) afterMs = after.getTime();
        if (before != null) beforeMs = before.getTime();

        for (LogEntry entry : entries) {
            long entryMs = entry.getDate().getTime();
            //We check if given date is within range, including before and after dates
            if (entryMs >= afterMs && entryMs <= beforeMs) result.add(entry);
        }

        return result;
    }

    /**
     * IPQuery methods
     */

    @Override
    public int getNumberOfUniqueIPs(Date after, Date before) {
        Set<String> uniqueIPs = getUniqueIPs(after, before);
        return uniqueIPs.size();
    }

    @Override
    public Set<String> getUniqueIPs(Date after, Date before) {
        Set<String> IPs = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        for (LogEntry entry : fitEntries) IPs.add(entry.getIp());
        return IPs;
    }

    @Override
    public Set<String> getIPsForUser(String user, Date after, Date before) {
        Set<String> IPs = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(after, before);

        for (LogEntry entry : fitEntries) {
            //Check if entry was submitted by given user
            if (entry.getUser().equals(user)) IPs.add(entry.getIp());
        }
        return IPs;
    }

    @Override
    public Set<String> getIPsForEvent(Event event, Date after, Date before) {
        Set<String> IPs = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(after, before);

        for (LogEntry entry : fitEntries) {
            //Check if event type matches given event
            if (entry.getEvent().equals(event)) IPs.add(entry.getIp());
        }
        return IPs;
    }

    @Override
    public Set<String> getIPsForStatus(Status status, Date after, Date before) {
        Set<String> IPs = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(after, before);

        for (LogEntry entry : fitEntries) {
            //Check if status matches given status
            if (entry.getStatus().equals(status)) IPs.add(entry.getIp());
        }
        return IPs;
    }

    private Set<String> getIPsForDate(Date parseDate, Date date1, Date date2) {
        Set<String> IPs = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(date1, date2);
        for (LogEntry entry : fitEntries) {
            if (entry.getDate().equals(parseDate)) IPs.add(entry.getIp());
        }
        return IPs;
    }

    /**
     * UserQuery methods
     */

    @Override
    public Set<String> getAllUsers() {
        Set<String> users = new HashSet<>();
        for (LogEntry entry : entries) users.add(entry.getUser());
        return users;
    }

    @Override
    public int getNumberOfUsers(Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<String> users = new HashSet<>();
        for (LogEntry entry : fitEntries) users.add(entry.getUser());
        return users.size();
    }

    @Override
    public int getNumberOfUserEvents(String user, Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        //We need to count only unique events here
        Set<Event> events = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //If user is assigned to given entry, increase the count
            if (entry.getUser().equals(user)) events.add(entry.getEvent());
        }
        return events.size();
    }

    @Override
    public Set<String> getUsersForIP(String ip, Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<String> users = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //If ip of given entry is equal to passed ip, add user to the set
            if (entry.getIp().equals(ip)) users.add(entry.getUser());
        }
        return users;
    }

    @Override
    public Set<String> getUsersWhoHaveLoggedIn(Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<String> users = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //Check for Event.LOGIN
            if (entry.getEvent().equals(Event.LOGIN)) {
                users.add(entry.getUser());
            }
        }
        return users;
    }

    @Override
    public Set<String> getUsersWhoHaveDownloadedPlugin(Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<String> users = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //Check for Event.DOWNLOAD_PLUGIN
            if (entry.getEvent().equals(Event.DOWNLOAD_PLUGIN)) {
                //Check for Status.OK
                if (entry.getStatus().equals(Status.OK)) users.add(entry.getUser());
            }
        }
        return users;
    }

    @Override
    public Set<String> getUsersWhoHaveSentMessages(Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<String> users = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //Check for Event.SEND_MESSAGE
            if (entry.getEvent().equals(Event.SEND_MESSAGE)) {
                //Check for Status.OK
                if (entry.getStatus().equals(Status.OK)) users.add(entry.getUser());
            }
        }
        return users;
    }

    @Override
    public Set<String> getUsersWhoHaveAttemptedTasks(Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<String> users = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //Check for Event.ATTEMPT_TASK
            if (entry.getEvent().equals(Event.ATTEMPT_TASK)) {
                users.add(entry.getUser());
            }
        }
        return users;
    }

    @Override
    public Set<String> getUsersWhoHaveAttemptedTasks(Date after, Date before, int task) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<String> users = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //Check for Event.ATTEMPT_TASK
            if (entry.getEvent().equals(Event.ATTEMPT_TASK)) {
                //Check for task number
                if (entry.getTaskNumber() == task) users.add(entry.getUser());
            }
        }
        return users;
    }

    @Override
    public Set<String> getUsersWhoHaveCompletedTasks(Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<String> users = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //Check for Event.COMPLETE_TASK
            if (entry.getEvent().equals(Event.COMPLETE_TASK)) {
                users.add(entry.getUser());
            }
        }
        return users;
    }

    @Override
    public Set<String> getUsersWhoHaveCompletedTasks(Date after, Date before, int task) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<String> users = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //Check for Event.COMPLETE_TASK
            if (entry.getEvent().equals(Event.COMPLETE_TASK)) {
                //Check for task number
                if (entry.getTaskNumber() == task) users.add(entry.getUser());
            }
        }
        return users;
    }

    private Set<String> getUsersForDate(Date parseDate, Date date1, Date date2) {
        Set<String> users = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(date1, date2);
        for (LogEntry entry : fitEntries) {
            if (entry.getDate().equals(parseDate)) users.add(entry.getUser());
        }
        return users;
    }

    private Set<String> getUsersForEvent(Event event, Date date1, Date date2) {
        Set<String> users = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(date1, date2);
        for (LogEntry entry : fitEntries) {
            if (entry.getEvent().equals(event)) users.add(entry.getUser());
        }
        return users;
    }

    private Set<String> getUsersForStatus(Status valueOf, Date date1, Date date2) {
        Set<String> users = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(date1, date2);
        for (LogEntry entry : fitEntries) {
            if (entry.getStatus().equals(valueOf)) users.add(entry.getUser());
        }
        return users;
    }

    /**
     * DateQuery methods
     */

    @Override
    public Set<Date> getDatesForUserAndEvent(String user, Event event, Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<Date> dates = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //Check for user match
            if (entry.getUser().equals(user)) {
                //Check for event match
                if (entry.getEvent().equals(event)) dates.add(entry.getDate());
            }
        }
        return dates;
    }

    @Override
    public Set<Date> getDatesWhenSomethingFailed(Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<Date> dates = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //Check for Status.FAILED
            if (entry.getStatus().equals(Status.FAILED)) dates.add(entry.getDate());
        }
        return dates;
    }

    @Override
    public Set<Date> getDatesWhenErrorOccurred(Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<Date> dates = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //Check for Status.ERROR
            if (entry.getStatus().equals(Status.ERROR)) dates.add(entry.getDate());
        }
        return dates;
    }

    @Override
    public Date getDateWhenUserLoggedInFirstTime(String user, Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        //We need to get THE EARLIEST date
        List<Date> sortedDates = new ArrayList<>();
        for (LogEntry entry : fitEntries) {
            //Check for user
            if (entry.getUser().equals(user)) {
                //Check for Event.LOGIN
                if (entry.getEvent().equals(Event.LOGIN)) sortedDates.add(entry.getDate());
            }
        }
        if (!sortedDates.isEmpty()) {
            Collections.sort(sortedDates);
            return sortedDates.get(0);
        }
        return null;
    }

    @Override
    public Date getDateWhenUserAttemptedTask(String user, int task, Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        //We need to get THE EARLIEST date
        List<Date> sortedDates = new ArrayList<>();
        for (LogEntry entry : fitEntries) {
            //Check for user
            if (entry.getUser().equals(user)) {
                //Check for Event.ATTEMPT_TASK
                if (entry.getEvent().equals(Event.ATTEMPT_TASK)) {
                    //Check for id
                    if (entry.getTaskNumber() == task) sortedDates.add(entry.getDate());
                }
            }
        }
        if (!sortedDates.isEmpty()) {
            Collections.sort(sortedDates);
            return sortedDates.get(0);
        }
        return null;
    }

    @Override
    public Date getDateWhenUserCompletedTask(String user, int task, Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        //We need to get THE EARLIEST date
        List<Date> sortedDates = new ArrayList<>();
        for (LogEntry entry : fitEntries) {
            //Check for user
            if (entry.getUser().equals(user)) {
                //Check for Event.COMPLETE_TASK
                if (entry.getEvent().equals(Event.COMPLETE_TASK)) {
                    //Check for id
                    if (entry.getTaskNumber() == task) sortedDates.add(entry.getDate());
                }
            }
        }
        if (!sortedDates.isEmpty()) {
            Collections.sort(sortedDates);
            return sortedDates.get(0);
        }
        return null;
    }

    @Override
    public Set<Date> getDatesWhenUserSentMessages(String user, Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<Date> dates = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //Check for user
            if (entry.getUser().equals(user)) {
                //Check for Event.SEND_MESSAGE
                if (entry.getEvent().equals(Event.SEND_MESSAGE)) dates.add(entry.getDate());
            }
        }
        return dates;
    }

    @Override
    public Set<Date> getDatesWhenUserDownloadedPlugin(String user, Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<Date> dates = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //Check for user
            if (entry.getUser().equals(user)) {
                //Check for Event.DOWNLOAD_PLUGIN
                if (entry.getEvent().equals(Event.DOWNLOAD_PLUGIN)) dates.add(entry.getDate());
            }
        }
        return dates;
    }

    public Set<Date> getAllDates(){
        Set<Date> dates = new HashSet<>();
        for (LogEntry entry : entries) dates.add(entry.getDate());
        return dates;
    }

    private Set<Date> getDatesForStatus(Status valueOf, Date date1, Date date2) {
        List<LogEntry> fitEntries = getEntriesByDate(date1, date2);
        Set<Date> dates = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            if (entry.getStatus().equals(valueOf)) dates.add(entry.getDate());
        }
        return dates;
    }

    private Set<Date> getDatesForEvent(Event valueOf, Date date1, Date date2) {
        Set<Date> dates = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(date1, date2);
        for (LogEntry entry : fitEntries) {
            if (entry.getEvent().equals(valueOf)) dates.add(entry.getDate());
        }
        return dates;
    }

    private Set<Date> getDatesForUser(String value1, Date date1, Date date2) {
        Set<Date> dates = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(date1, date2);
        for (LogEntry entry : fitEntries) {
            if (entry.getUser().equals(value1)) dates.add(entry.getDate());
        }
        return dates;
    }

    private Set<Date> getDatesForIP(String value1, Date date1, Date date2) {
        Set<Date> dates = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(date1, date2);
        for (LogEntry entry : fitEntries) {
            if (entry.getIp().equals(value1)) dates.add(entry.getDate());
        }
        return dates;
    }

    /**
     * EventQuery methods
     */

    @Override
    public int getNumberOfEvents(Date after, Date before) {
        return getAllEvents(after, before).size();
    }

    @Override
    public Set<Event> getAllEvents(Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<Event> events = new HashSet<>();
        for (LogEntry entry : fitEntries) events.add(entry.getEvent());
        return events;
    }

    @Override
    public Set<Event> getEventsForIP(String ip, Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<Event> events = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //Check for IP
            if (entry.getIp().equals(ip)) events.add(entry.getEvent());
        }
        return events;
    }

    @Override
    public Set<Event> getEventsForUser(String user, Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<Event> events = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //Check for user
            if (entry.getUser().equals(user)) events.add(entry.getEvent());
        }
        return events;
    }

    @Override
    public Set<Event> getFailedEvents(Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<Event> events = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //Check for Status.FAILED
            if (entry.getStatus().equals(Status.FAILED)) events.add(entry.getEvent());
        }
        return events;
    }

    @Override
    public Set<Event> getErrorEvents(Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Set<Event> events = new HashSet<>();
        for (LogEntry entry : fitEntries) {
            //Check for Status.ERROR
            if (entry.getStatus().equals(Status.ERROR)) events.add(entry.getEvent());
        }
        return events;
    }

    @Override
    public int getNumberOfAttemptsToCompleteTask(int task, Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        int count = 0;
        for (LogEntry entry : fitEntries) {
            //Check for Event.ATTEMPT_TASK
            if (entry.getEvent().equals(Event.ATTEMPT_TASK)) {
                //Check for task id
                if (entry.getTaskNumber() == task) count++;
            }
        }
        return count;
    }

    @Override
    public int getNumberOfSuccessfulAttemptsToCompleteTask(int task, Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        int count = 0;
        for (LogEntry entry : fitEntries) {
            //Check for Event.COMPLETE_TASK
            if (entry.getEvent().equals(Event.COMPLETE_TASK)) {
                //Check for task id
                if (entry.getTaskNumber() == task) count++;
            }
        }
        return count;
    }

    @Override
    public Map<Integer, Integer> getAllAttemptedTasksAndNumberOfAttempts(Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Map<Integer, Integer> result = new HashMap<>();
        for (LogEntry entry : fitEntries) {
            //Check for Event.ATTEMPT_TASK
            if (entry.getEvent().equals(Event.ATTEMPT_TASK)) {
                //If map does not contain given ID, get number of attempts and put entry to the map
                int id = entry.getTaskNumber();
                if (!result.containsKey(id)) {
                    int count = getNumberOfAttemptsToCompleteTask(id, after, before);
                    result.put(id, count);
                }
            }
        }
        return result;
    }

    @Override
    public Map<Integer, Integer> getAllCompletedTasksAndNumberOfCompletions(Date after, Date before) {
        List<LogEntry> fitEntries = getEntriesByDate(after, before);
        Map<Integer, Integer> result = new HashMap<>();
        for (LogEntry entry : fitEntries) {
            //Check for Event.COMPLETE_TASK
            if (entry.getEvent().equals(Event.COMPLETE_TASK)) {
                //If map does not contain given ID, get number of attempts and put entry to the map
                int id = entry.getTaskNumber();
                if (!result.containsKey(id)) {
                    int count = getNumberOfSuccessfulAttemptsToCompleteTask(id, after, before);
                    result.put(id, count);
                }
            }
        }
        return result;
    }

    private Set<Event> getEventsForStatus(Status value1, Date date1, Date date2) {
        Set<Event> events = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(date1, date2);
        for (LogEntry entry : fitEntries) {
            if (entry.getStatus().equals(value1)) events.add(entry.getEvent());
        }
        return events;
    }

    private Set<Event> getEventsForDate(Date parseDate, Date date1, Date date2) {
        Set<Event> events = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(date1, date2);
        for (LogEntry entry : fitEntries) {
            if (entry.getDate().equals(parseDate)) events.add(entry.getEvent());
        }
        return events;
    }

    /**
     * StatusQuery
     */

    public Set<Status> getAllStatus(){
        Set<Status> result = new HashSet<>();
        for (LogEntry entry : entries) result.add(entry.getStatus());
        return result;
    }

    private Set<Status> getAllStatusForEvent(Event valueOf, Date date1, Date date2) {
        Set<Status> result = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(date1, date2);
        for (LogEntry entry : fitEntries) {
            if (entry.getEvent().equals(valueOf)) result.add(entry.getStatus());
        }
        return result;
    }

    private Set<Status> getAllStatusForDate(Date parseDate, Date date1, Date date2) {
        Set<Status> result = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(date1, date2);
        for (LogEntry entry : fitEntries) {
            if (entry.getDate().equals(parseDate)) result.add(entry.getStatus());
        }
        return result;
    }

    private Set<Status> getAllStatusForUser(String value1, Date date1, Date date2) {
        Set<Status> result = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(date1, date2);
        for (LogEntry entry : fitEntries) {
            if (entry.getUser().equals(value1)) result.add(entry.getStatus());
        }
        return result;
    }

    private Set<Status> getAllStatusForIp(String value1, Date date1, Date date2) {
        Set<Status> result = new HashSet<>();
        List<LogEntry> fitEntries = getEntriesByDate(date1, date2);
        for (LogEntry entry : fitEntries) {
            if (entry.getIp().equals(value1)) result.add(entry.getStatus());
        }
        return result;
    }

    /**
     * QLQuery method
     */

    @Override
    public Set<Object> execute(String query) {
        Set<Object> result = new HashSet<>();
        String[] queryArr = query.split(" ", 6);

        if (queryArr.length < 6){
            /**
             * Default query
             */
            switch (query) {
                case "get ip":
                    result.addAll(getUniqueIPs(null, null));
                    break;
                case "get user":
                    result.addAll(getAllUsers());
                    break;
                case "get date":
                    result.addAll(getAllDates());
                    break;
                case "get event":
                    result.addAll(getAllEvents(null, null));
                    break;
                case "get status":
                    result.addAll(getAllStatus());
                    break;
                default:
                    break;
            }
            return result;
        }
        String field1 = queryArr[1];
        String field2 = queryArr[3];
        String value1 = queryArr[5].replaceAll("\"","");
        Date date1 = null;
        Date date2 = null;

        /**
         * Here's the general format for a query:
         * get field1 for field2 = "value1" and date between "after" and "before"
         */

        if (value1.contains("and date")) {
            String parse = value1;
            value1 = parse.substring(0, parse.indexOf(" and")).replaceAll("\"","");
            //Some hard stuff with parsing whole query - we end up with array with first two elements date/time1 and last two elements date/time2
            String[] dates = parse.substring(parse.indexOf("between "))
                    .replace("between ", "")
                    .replaceAll("\"","")
                    .split(" ");
            date1 = parseDate(dates[0] + " " + dates[1]);
            date2 = parseDate(dates[3] + " " + dates[4]);
        }

        switch (field1) {
            case "ip":
                switch (field2) {
                    case "user":
                        result.addAll(getIPsForUser(value1, date1, date2));
                        break;
                    case "event":
                        result.addAll(getIPsForEvent(Event.valueOf(value1), date1, date2));
                        break;
                    case "date":
                        result.addAll(getIPsForDate(parseDate(value1), date1, date2));
                        break;
                    case "status":
                        result.addAll(getIPsForStatus(Status.valueOf(value1), date1, date2));
                        break;
                }
                break;
            case "user":
                switch (field2) {
                    case "ip":
                        result.addAll(getUsersForIP(value1, date1, date2));
                        break;
                    case "date":
                        result.addAll(getUsersForDate(parseDate(value1), date1, date2));
                        break;
                    case "event":
                        result.addAll(getUsersForEvent(Event.valueOf(value1), date1, date2));
                        break;
                    case "status":
                        result.addAll(getUsersForStatus(Status.valueOf(value1), date1, date2));
                        break;
                }
                break;
            case "date":
                switch (field2) {
                    case "ip":
                        result.addAll(getDatesForIP(value1, date1, date2));
                        break;
                    case "user":
                        result.addAll(getDatesForUser(value1, date1, date2));
                        break;
                    case "event":
                        result.addAll(getDatesForEvent(Event.valueOf(value1), date1, date2));
                        break;
                    case "status":
                        result.addAll(getDatesForStatus(Status.valueOf(value1), date1, date2));
                        break;
                }
                break;
            case "event":
                switch (field2) {
                    case "ip":
                        result.addAll(getEventsForIP(value1, date1, date2));
                        break;
                    case "user":
                        result.addAll(getEventsForUser(value1, date1, date2));
                        break;
                    case "date":
                        result.addAll(getEventsForDate(parseDate(value1), date1, date2));
                        break;
                    case "status":
                        result.addAll(getEventsForStatus(Status.valueOf(value1), date1, date2));
                        break;
                }
                break;
            case "status":
                switch (field2) {
                    case "ip":
                        result.addAll(getAllStatusForIp(value1, date1, date2));
                        break;
                    case "user":
                        result.addAll(getAllStatusForUser(value1, date1, date2));
                        break;
                    case "date":
                        result.addAll(getAllStatusForDate(parseDate(value1), date1, date2));
                        break;
                    case "event":
                        result.addAll(getAllStatusForEvent(Event.valueOf(value1), date1, date2));
                        break;
                }
                break;
        }
        return result;
    }

    /**
     * Util methods
     */
    private Date parseDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }
}
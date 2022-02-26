package main;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Helper class to LogParser, will split log entry into fields below
 * taskNumber -1 indicates no task number (only Event.ATTEMPT_TASK and Event.COMPLETE_TASK) have task numbers assigned
 */
public class LogEntry {
    private String ip;
    private String user;
    private Date date;
    private Event event;
    private int taskNumber = -1;
    private Status status;

    public String getIp() {
        return ip;
    }

    public String getUser() {
        return user;
    }

    public Date getDate() {
        return date;
    }

    public Event getEvent() {
        return event;
    }

    public int getTaskNumber() {
        return taskNumber;
    }

    public Status getStatus() {
        return status;
    }

    public LogEntry(String entry) {
        //We will convert single line in the log into an entry, work will be done in the constructor
        parseEntry(entry);
    }

    private void parseEntry(String entry) {
        //Entry has both spaces and tabs as separators, first we replace tabs with spaces
        String e = entry.replaceAll("\t", " ");

        //Split entry by spaces, convert to ArrayList
        List<String> entryList = new ArrayList<>(Arrays.asList(e.split(" ")));

        //IP is first entry
        ip = entryList.remove(0);

        //name is built from all following entries until first numeric met afterwards
        StringBuilder userName = new StringBuilder();
        for (int i = 0; i < entryList.size(); i++) {
            String s = entryList.get(0);
            //Check if first character is a number
            if (s.substring(0,1).matches("[0-9]")) break;
            userName.append(s).append(" ");
            entryList.remove(0);
        }
        user = userName.toString().trim();

        //Next two elements are the date, we need to parse it
        String dateDate = entryList.remove(0);
        String dateTime = entryList.remove(0);
        String dateString = dateDate + " " + dateTime;
        date = parseDate(dateString);

        //Next element is the event
        event = Event.valueOf(entryList.remove(0));
        //if event is ATTEMPT_TASK or COMPLETE_TASK, next element will be task number
        if (event.equals(Event.ATTEMPT_TASK) || event.equals(Event.COMPLETE_TASK)) {
            taskNumber = Integer.parseInt(entryList.remove(0));
        }
        //Last element is status message
        status = Status.valueOf(entryList.remove(0));
    }

    private Date parseDate(String dateString) {
        //Date is provided in given format
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }
}
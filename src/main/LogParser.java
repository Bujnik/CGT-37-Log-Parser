package main;

import main.query.IPQuery;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class LogParser implements IPQuery {
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
}

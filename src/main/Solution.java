package main;

import java.nio.file.Paths;
import java.util.Date;

public class Solution {
    public static void main(String[] args) {
        LogParser logParser = new LogParser(Paths.get("F:\\nauka\\CodeGymTasks\\4.JavaCollections\\src\\com\\codegym\\task\\task39\\task3913\\logs"));
        System.out.println(logParser.getNumberOfUniqueIPs(null, new Date()));
    }
}

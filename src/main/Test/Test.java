package main.Test;

import main.LogEntry;

public class Test {
    public static void main(String[] args) {
        String input = "120.120.120.122\tAmigo\t29.2.2028 5:4:7\tATTEMPT_TASK 18\tOK";
        LogEntry entry = new LogEntry(input);

        System.out.println(entry.getIp());
        System.out.println(entry.getUser());
        System.out.println(entry.getDate());
        System.out.println(entry.getEvent());
        System.out.println(entry.getTaskNumber());
        System.out.println(entry.getStatus());

    }

}

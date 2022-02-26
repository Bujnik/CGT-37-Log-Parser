# CodeGymTasks

 CodeGym mini project - level 37 - Log Parser
 
Today we're going to write a log parser.

The log file has the following format:
ip username date event status

where:
ip is the IP address from which the user generated the event,
user is the username (one or more words separated by spaces),
date is the date of the event formatted as <day.month.year hour:minute:second>,
event is one of the following events:
LOGIN - a user logged in,
DOWNLOAD_PLUGIN - a user downloaded the plugin,
SEND_MESSAGE - a user sent a message,
ATTEMPT_TASK - a user attempted to complete a task,
COMPLETE_TASK - a user has completed a task.
The ATTEMPT_TASK and COMPLETE_TASK events have one additional parameter, separated from the others by a space: the task number.
status is one of the following event statuses:
OK - the event succeeded,
FAILED - the event failed,
ERROR - an error occurred.

Example of a line from a log file:
"146.34.15.5 Eduard Bentley 05.01.2021 20:22:55 COMPLETE_TASK 48 FAILED".
The log file entries are not necessarily ordered by date: events could be logged in a different order than they occur.

__________________________________________________________
My takeaways: <br>
-> 

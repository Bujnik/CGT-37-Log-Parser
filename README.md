# CodeGymTasks

 CodeGym mini project - level 37 - Log Parser
 
Today we're going to write a log parser.

The log file has the following format:
ip username date event status

where:<br>
ip is the IP address from which the user generated the event,<br>
user is the username (one or more words separated by spaces),<br>
date is the date of the event formatted as <day.month.year hour:minute:second>,<br>
event is one of the following events:<br>
<b>LOGIN</b> - a user logged in,<br>
<b>DOWNLOAD_PLUGIN</b> - a user downloaded the plugin,<br>
<b>SEND_MESSAGE</b> - a user sent a message,<br>
<b>ATTEMPT_TASK</b> - a user attempted to complete a task,<br>
<b>COMPLETE_TASK</b> - a user has completed a task.<br>
The <b>ATTEMPT_TASK</b> and <b>COMPLETE_TASK</b> events have one additional parameter, separated from the others by a space: the task number.<br>
status is one of the following event statuses:<br>
<b>OK</b> - the event succeeded,<br>
<b>FAILED</b> - the event failed,<br>
<b>ERROR</b> - an error occurred.<br>

Example of a line from a log file:<br>
"146.34.15.5 Eduard Bentley 05.01.2021 20:22:55 COMPLETE_TASK 48 FAILED".<br>
The log file entries are not necessarily ordered by date: events could be logged in a different order than they occur.<br>

__________________________________________________________
My takeaways: <br>
-> Working with helper class - coming up with idea to delegate part of the work outside given class<br>
-> Practice with parsing strings

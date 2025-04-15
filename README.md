Mobile App to Manage Task in a super easy way 
## Release Notes - New Features and Improvements

This update includes significant enhancements to help you manage your tasks effectively:

**Key Highlights:**

* **Intelligent Date Validation:** Prevents past due dates and ensures logical start and end date relationships.
* **Time Picker Integration:** Set precise times for your tasks using the new time picker.
* **Real-time Remaining Days:** View the countdown to your deadlines directly in the task list.
* **Proactive Daily Reminders:** Receive timely notifications for your tasks every day.
* **Easy Password Recovery:** Recover your account with the new "Forgot Password?" option.

**Detailed Changes:**

* **"Add Task" Screen:**
    * Implemented validation to prevent selecting due dates in the past or before the start date.
    * Integrated a `TimePickerDialog` to allow users to specify the exact time for a task.
* **Task List:**
    * Now displays the number of remaining days until the due date for each active task.
    * Indicates overdue tasks with a "Overdue" status and distinct styling.
    * Shows "Completed" for finished tasks.
* **Notifications:**
    * Schedules daily notifications for each active task at the specified time using `AlarmManager` and a `BroadcastReceiver`.
    * Notifications are stopped when a task is completed or the due date expires.
* **Login Screen:**
    * Added a "Forgot Password?" link that triggers a dialog to send a password reset email to the user's registered email address.

We believe these updates will significantly improve your experience with the application. Thank you for using our To-Do app!

## 👤Chandrakanth S

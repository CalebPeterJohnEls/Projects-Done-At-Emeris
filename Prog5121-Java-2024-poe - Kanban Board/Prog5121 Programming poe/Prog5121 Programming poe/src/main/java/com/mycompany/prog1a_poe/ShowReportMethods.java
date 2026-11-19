/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prog1a_poe;
//These imports call the data from the arrays sorted on the Add Task Option
import static com.mycompany.prog1a_poe.PROG1A_POE.developerDetails;
import static com.mycompany.prog1a_poe.PROG1A_POE.i;
import static com.mycompany.prog1a_poe.PROG1A_POE.numberOfTasks;
import static com.mycompany.prog1a_poe.PROG1A_POE.taskDescription;
import static com.mycompany.prog1a_poe.PROG1A_POE.taskDuration;
import static com.mycompany.prog1a_poe.PROG1A_POE.taskID;
import static com.mycompany.prog1a_poe.PROG1A_POE.taskName;
import static com.mycompany.prog1a_poe.PROG1A_POE.taskNumber;
import static com.mycompany.prog1a_poe.PROG1A_POE.taskStatus;
import static com.mycompany.prog1a_poe.PROG1A_POE.taskStatusString;
import static com.mycompany.prog1a_poe.PROG1A_POE.totalHours;
import javax.swing.JOptionPane;

/**
 *
 * @author lab_services_student
 */
public class ShowReportMethods {
    //This method gives users a list of options and each option will perform a specific method
   public static void showReport() {
       //(Farrel, J. 2018)
        while (true) {
            int choiceOption2 = Integer.parseInt(JOptionPane.showInputDialog(null, "Choose an option by entering the number:\n" +
                    "1. Display all 'done' tasks\n" +
                    "2. Display task with the longest duration\n" +
                    "3. Search for a task by name\n" +
                    "4. Search for tasks by developer\n" +
                    "5. Delete a task by name\n" +
                    "6. Display all tasks\n" +
                    "7. Add a new task\n" +
                    "8. Back"));
//(Farrel, J. 2018)
            switch (choiceOption2) {
                case 1:
                    displayDoneTasks();//Calls the method required
                    break;
                case 2:
                    displayLongestTaskDetails();
                    break;
                case 3:
                    String searchName = JOptionPane.showInputDialog("Enter task name to search:");
                    searchTaskByName(searchName);
                    break;
                case 4:
                    String developerName = JOptionPane.showInputDialog("Enter developer name to search:");
                    searchTasksByDeveloper(developerName);
                    break;
                case 5:
                    String deleteName = JOptionPane.showInputDialog("Enter task name to delete:");
                    deleteTaskByName(deleteName);
                    break;
                case 6:
                    displayAllTasks();
                    break;
                case 7:
                    addNewTask();
                    break;
                case 8:
                    return; //Returns user back to the main menu
                default:
                    JOptionPane.showMessageDialog(null, "Invalid choice. Please try again.");//Prompts error message
            }
        }
    }
   //This method displays all tasks in the array that are done
   //(Stackflow. 2020)
   public static void displayDoneTasks() {
        for (i = 0; i < numberOfTasks; i++) {
            if (taskStatusString[i].equals("Done")) {
                JOptionPane.showMessageDialog(null,"\n Task number " + (i + 1)
                        + "\nTask Name: " + taskName[i]
                        + "\nDeveloper Details: " + developerDetails[i]
                        + "\nTask Duration: " + taskDuration[i]
                        + "\nTask Status: " + taskStatusString[i]);
            }//(Stackflow. 2024)
        }
    }
   
 //This method displays the longest task in the array
 //(PrepBytes. 2023) 
    public static String taskWithLongestDuration() {
    int longestTimeI = 0;
    for (int i = 1; i < taskDuration.length; i++) {
        if (taskDuration[i] > taskDuration[longestTimeI]) {
            longestTimeI = i;
        }
    }
        //Displays infomation on the longest task
        return "Task with the longest duration: " + taskName[longestTimeI]
            + "\nDeveloper Details: " + developerDetails[longestTimeI]
            + "\nTask Duration: " + taskDuration[longestTimeI] + " hours"
            + "\nTask Status: " + taskStatusString[longestTimeI];
}//(Stackflow. 2024)
    
    public static void displayLongestTaskDetails() {
    String message = taskWithLongestDuration();
    JOptionPane.showMessageDialog(null, message);
}//I called the method back into a void message this so that the JUnit testing can work and so that the code cna run properly
    
 //This method allows a user to search for a tak based on the Task Name
public static void searchTaskByName(String taskNameSearch) {
    boolean found = false; // Displays information if the task is found 
    for (int i = 0; i < numberOfTasks; i++) {
        
        if (taskName[i].equalsIgnoreCase(taskNameSearch)) {
            JOptionPane.showMessageDialog(null, "\nTask number " + (i + 1)
                    + "\nTask Name: " + taskName[i]
                    + "\nDeveloper Details: " + developerDetails[i]
                    + "\nTask Duration: " + taskDuration[i]
                    + "\nTask Status: " + taskStatusString[i]);
         found = true; 
            break; 
        }
    }
    //If it is not found, it will display this error message
    if (!found) {
        JOptionPane.showMessageDialog(null, "No task by that name");
}
}
//(Stackflow. 2013)
//(Stackflow. 2024)

 //This method allows a user to search for a task based on the Developer Name
   public static void searchTasksByDeveloper(String developerName) {
       boolean found = false; 
        for (i = 0; i < numberOfTasks; i++) {
            if (developerDetails[i].equalsIgnoreCase(developerName)) {
                JOptionPane.showMessageDialog(null,"\n Task number " + (i + 1)
                        + "\nTask Name: " + taskName[i]
                        + "\nDeveloper Details: " + developerDetails[i]
                        + "\nTask Duration: " + taskDuration[i]
                        + "\nTask Status: " + taskStatusString[i]);
             found = true; 
            break; 
        }
    }
    
    if (!found) {
        JOptionPane.showMessageDialog(null, "No task by that name");
    }
}//(Stackflow. 2013)
//(Stackflow. 2024)
   
   //This method allows the user delete a method from the array based on the Task Name
   public static void deleteTaskByName(String taskNameToDelete) {
        for (i = 0; i < numberOfTasks; i++) {
            if (taskName[i].equalsIgnoreCase(taskNameToDelete)) {
                 // Displays a message indicating the task is being deleted
                JOptionPane.showMessageDialog(null, "Deleting Task: " + taskName[i]);

                // This will shift elements in arrays to delete the task at index i
                for (int j = i; j < numberOfTasks - 1; j++) {
                    taskName[j] = taskName[j + 1];
                    taskDescription[j] = taskDescription[j + 1];
                    developerDetails[j] = developerDetails[j + 1];
                    taskDuration[j] = taskDuration[j + 1];
                    taskID[j] = taskID[j + 1];
                    taskStatusString[j] = taskStatusString[j + 1];
                }
                numberOfTasks--;//This will decrease the number of tasks
                return;// This will exit the method after deleting the task
            }
        }
        JOptionPane.showMessageDialog(null, "Task not found: " + taskNameToDelete);//Error message if task is not found
    }//(Software Testing Help. 2024)
//(Stackflow. 2024)
   
   //This method displays the infomation of all the task in the array
   public static void displayAllTasks() {
        for (i = 0; i < numberOfTasks; i++) {
            JOptionPane.showMessageDialog(null,"\n Task number " + (i + 1)
                    + "\nTask Name: " + taskName[i]
                    + "\nDeveloper Details: " + developerDetails[i]
                    + "\nTask Duration: " + taskDuration[i]
                    + "\nTask Status: " + taskStatusString[i]);
        }
    }//(Stackflow. 2024)
   
   //This method allows the user delete a method from the array based on the Task Name
   //(Farrel, J. 2018)
   public static void addNewTask()
   {
       numberOfTasks =Integer.parseInt(JOptionPane.showInputDialog(null,"Enter Number of tasks:"));

                 //(Stackflow. 2024)(Farrel. 2018)

                 //A for loop used to loop the prompts for the details of each task baed on the number of tasks there are.
                for (i = 0; i < numberOfTasks; i++){//(Code Knowledge. 2024)
                    taskNumber = i +1;//This adds one to the taskNumber already set to display the task number of each task.
                    
                    // This displays the number of the task
                    JOptionPane.showMessageDialog(null,"Details of Task " + taskNumber);
                    // This prompts the user to enter the task name.
                   taskName[i] = JOptionPane.showInputDialog(null,"Enter Task Name:");

                   //Thsi while loop is used state if the task description is correct or not.
                    while (true) {
                       taskDescription[i] = JOptionPane.showInputDialog(null,"Enter Task Description:");
                       
                        if (DescriptionCheck.checkTaskDescription(taskDescription[i])) {//Refers back to checkTaskDescription method.
                            JOptionPane.showMessageDialog(null,"Task Description successfully captured");//Message displayed if description is correct 
                            break; //Used to break loop if description is valid.
                        } else {
                            JOptionPane.showMessageDialog(null,"Task Description is not correctly formatted, please ensure that it is between 1 and 50 characters long."); 
                        //Message displayed if description is incorrect. 
                        }
                    }
                    //This prompts the user to enter the developer details.
                    developerDetails[i] =JOptionPane.showInputDialog(null,"Enter Developer Details:");

                    //This prompts the user to enter the task duration.
                    taskDuration[i] =Integer.parseInt(JOptionPane.showInputDialog(null,"Enter Task Duration:"));
                    
                    

                    //This prompts the user option menu to enter task status.
                    int statusOption =Integer.parseInt(JOptionPane.showInputDialog(null,"Enter task Status (1, 2, or 3):\n"
                            + "Option 1) To Do\n"
                            + "Option 2) Done\n"
                            + "Option 3) Doing"));
                    
                    //This assigns the task status based on user input.
                    taskStatusString[i] = taskStatus[statusOption - 1]; 
                     taskID[i] = DataAutoGen.createTaskID(i, developerDetails, taskName);//calls back to createTaskId method.
                    
                     totalHours=DataAutoGen.returnTotalHours(taskDuration, i, totalHours);//This is used to call back to the returmTotalHours method and store it as a variable.

                }
   } 
}
//(Farrel, J. 2018)
/*REFERENCE LIST:
·         BeginnersBook. 2022. Java String charAt() Method example [Online]. Available at: https://beginnersbook.com/2013/12/java-string-charat-method-example/#:~:text=The%20Java%20String%20charAt(int,string%20represented%20by%20instance%20s. [Accessed 2 May 2024]
·         Code Knowledge. 2024. For loop in Java: counting loop that repeats the code a specific number of times. [Online]. Available at: https://code-knowledge.com/java-for-loop/#:~:text=In%20Java%2C%20you%20use%20the,a%20predetermined%20number%20of%20times. [Accessed 7 May 2024]
·         DANIWEB. 2013. How can I count the number of user inputs?. [Online]. Available at: https://www.daniweb.com/programming/software-development/threads/349058/how-can-i-count-the-number-of-user-inputs. [Accessed 2 May 2024]
·         Farrel, J. 2018.Java™ Programming.9th edt. Boston: Cengage.
·         GeeksforGeeks. 2020. How to validate a Username using Regular Expressions in Java. [Online]. Available at: https://www.geeksforgeeks.org/how-to-validate-a-username-using-regular-expressions-in-java/. [Accessed 1 April 2024]
·         GeeksforGeeks. 2022. How to find the first and last character of a string in Java. [Online]. Available at: https://www.geeksforgeeks.org/how-to-find-the-first-and-last-character-of-a-string-in-java/. [Accessed 7 May 2024]
·         GeeksforGeeks. 2023. Regex Tutorial – How to write Regular Expressions?. [Online]. Available at: https://www.geeksforgeeks.org/write-regular-expressions/. [Accessed 1 April 2024]
·         GeeksforGeeks. 2023. static Keyword in Java. [Online]. Available at: https://www.geeksforgeeks.org/static-keyword-java/. [Accessed 1 April 2024]
·         northCoder. 2022. Passing Java Functions in Variables . [Online]. Available at: https://northcoder.com/post/passing-java-functions-in-variables/[Accessed 7 April 2024]
·         PrepBytes. 2023. Java Program for Finding the Largest Element of the Array. [Online]. Available at: https://www.prepbytes.com/blog/java/java-program-for-finding-the-largest-element-of-the-array/. [Accessed 4 June 2024]
·         Sanfoundry.2024. Java Program to Illustrate how User Authentication is Done. [Online]. Available at: https://www.sanfoundry.com/java-program-illustrate-how-user-authentication-done/#:~:text=Enter%20username%20and%20password%20as,how%20User%20Authentication%20is%20Done. [Accessed 1 April 2024]
·         Show a drop down list in Java JOptionPane prompt. 2016. YouTube video, added by Brandan Jones. [Online]. Available at: https://www.youtube.com/watch?v=A-R9SrKQmGY [Accessed 2 May 2024].
·         Show a drop down list in Java JOptionPane prompt. 2016. YouTube video, added by Brandan Jones. [Online]. Available at: https://www.youtube.com/watch?v=A-R9SrKQmGY. [Accessed 2 May 2024].
·         Software Testing Help. 2024. Remove/Delete An Element From An Array In Java. [Online]. Available at: https://www.softwaretestinghelp.com/remove-element-from-array-java/#:~:text=To%20remove%20an%20element%20from,ArrayList%20back%20to%20the%20array.[Accessed 4 June 2024]
·         Stackflow. 2013. How to produce error for item not being in array list. [Online]. Available at: https://stackoverflow.com/questions/15883028/how-to-produce-error-for-item-not-being-in-array-list. [Accessed 5 June 2024]
·         Stackflow. 2013. JAVA: Creating a Menu Loop. [Online]. Available at: https://stackoverflow.com/questions/20681616/java-creating-a-menu-loop. [Accessed  1 May 2024]
·         Stackflow. 2014. Sum of previous two numbers entered [Online]. Available at: https://stackoverflow.com/questions/26956242/sum-of-previous-two-numbers-entered. [Accessed  2 May 2024]
·         Stackflow. 2017. Show JOptionPane (with dropdown menu) on the top of other windows [Online]. Available at: https://stackoverflow.com/questions/43658679/show-joptionpane-with-dropdown-menu-on-the-top-of-other-windows. [Accessed  2 May 2024]
·         Stackflow. 2020. Searching a Array for unique name using method. [Online]. Available at: https://stackoverflow.com/questions/58490694/searching-a-array-for-unique-name-using-method. [Accessed 4 June 2024]
·         Stackflow. 2022. Can I store a method in a variable in Java 8?. [Online]. Available at: https://stackoverflow.com/questions/29219984/can-i-store-a-method-in-a-variable-in-java-8 [Accessed 7 April 2024]
·         Stackflow. 2022. Valid Username without Regex. [Online]. Available at: https://stackoverflow.com/questions/70642136/valid-username-without-regex#:~:text=If%20the%20username%20consists%20of,digits%20%5B0-9%5D. [Accessed 1 April 2024]
·         Stackflow. 2023. Java: break statement in "if else". [Online]. Available at: https://stackoverflow.com/questions/20670824/java-break-statement-in-if-else. [Accessed 1 April 2024]
·         Stackflow. 2024. ArrayList input java [Online]. Available at: https://stackoverflow.com/questions/12056220/arraylist-input-java. [Accessed  7 May 2024]
·         W3Schools. 2024. Java String toUpperCase() Method. [Online]. Available at: https://www.w3schools.com/java/ref_string_touppercase.asp#:~:text=The%20toUpperCase()%20method%20converts,string%20to%20lower%20case%20letters. [Accessed  6 May 2024]
·         W3Schools. 2024. The JavaScript call() Method. [Online]. Available at: https://www.w3schools.com/js/js_function_call.asp#:~:text=The%20call()%20method%20is,method%20belonging%20to%20another%20object. [Accessed  6 April 2024]
·         Write additional unit tests and update your .yaml file. 2022. YouTube video, added by IIEVC School of Computer Science. [Online]. Available at: https://www.youtube.com/watch?v=DmL4gG9vG0A&list=PL480DYS-b_kfHSYf2yzLgto_mwDr_U-Q6&index=8 [Accessed 10 April 2024].

*/
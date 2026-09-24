/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.prog1a_poe;

/**
 *
 * @author lab_services_student
 */

import static com.mycompany.prog1a_poe.RegistrationMethods.checkUserName;
import static com.mycompany.prog1a_poe.DataAutoGen.returnTotalHours;

import static com.mycompany.prog1a_poe.outputDetails.printTaskDetails;
import com.mycompany.prog1a_poe.ShowReportMethods;


import javax.swing.JOptionPane;

public class PROG1A_POE {
    static String userName; 
    static String password; 
    static String firstName; 
    static String lastName;
     static int numberOfTasks = 0;//The string is set to 0//(DANIWEB. 2013)
    static int i;
    static int taskNumber = 0;
    static String[] taskName;
    static String[] taskDescription;
    static String[] developerDetails;
    static int[] taskDuration;//(Stackflow. 2014)
    static int totalHours = 0;
    static String[] taskID;
    static int n;
    static String[] taskStatus = {"To Do", "Done", "Doing"};//(Stackflow. 2017)//An Array for the user to choose the status of the task.
    static String[] taskStatusString;

    /*These static variables are declared outside of the main method to store
    inputed data of the user and to insure that this data can be used through out
    code*/
    //(GeeksforGeeks. 2023)
    //(Farrel. 2018)

    
    public static void main(String[] args) {
        
        
        JOptionPane.showMessageDialog(null,"Welcome to Kanban Boards Registration! "); // An output message at the start of the program to display a welcome message.
        
        firstName = JOptionPane.showInputDialog(null, "\nEnter your first name"); 
        lastName  = JOptionPane.showInputDialog(null,"\nEnter your last name "); // Prompt the user a message to enter their first and last name and store it.
 

        // A while loop to prompt the user to enter a username until a valid username is entered.
        while (true) {
            userName = JOptionPane.showInputDialog(null, "Enter your username:");// Prompt the user a message to enter a username and store it.

            // An if statement to check if the entered username is valid using the checkUserName method
            if (RegistrationMethods.checkUserName(userName)) {
                JOptionPane.showMessageDialog(null, "Username successfully captured"); // Displays a message stating that the username has been accepted and meets the right critrea
                break; // To exit the loop and to avoid an infinte loop 
                //(Stackflow. 2023)
            } else {
                JOptionPane.showMessageDialog(null, "Username is not correctly formatted, please ensure that your username contains:\n" +
                        "• no more than 5 characters.\n" +
                        "• an underscore (_)"); 
                        // Displays an error message stating that the username does not fit the right critrea and states what is need in the username
            }
            //(Stackflow. 2022)
        }

        // A while loop to prompt the user to enter a password until a valid password is entered.
        while (true) {
            password = JOptionPane.showInputDialog(null, "Enter your password:");

            // An if statement to check if the entered password is valid using the checkPasswordComplexity method.
            if (RegistrationMethods.checkPasswordComplexity(password)) {
                JOptionPane.showMessageDialog(null, "Password successfully captured"); // Displays a message stating that the password has been accepted and meets the right critrea
                break; 
            } else {
                JOptionPane.showMessageDialog(null, "Password is not correctly formatted, please ensure that the password contains at least:\n" +
                        "• 8 characters long.\n" +
                        "• a capital letter.\n" +
                        "• a number.\n" +
                        "• a special character"); 
                        // Displays an error message stating that the password does not fit the right critrea and states what is need in the password.
            }
            //(Stackflow. 2022)
        }

        registerUser(userName, password); /* I called the registerUser() method so that the usrname and password 
                                           can be correlate back into the method and so that the messages can 
                                           pop up for the user after they finish their registration.*/
     
        returnLoginStatus(); /* I called the registerUser() method so that the usrname and password 
                               can be correlate back into the method and so that the messages can 
                                 pop up for the user after they finish their registration.*/
        //(W3Schools. 2023)
        JOptionPane.showMessageDialog(null,"Welcome to Kanban Boards, " + userName + "!"); // An output welcome message.
        
        int optionNumber = 0;//This variable is used to store user input for menu option.

        //A while loop that loops until the user chooses to quit (option 3).
        while (optionNumber != 3) {//(Stackflow. 2013)
            // This prompts the user a option menu to enter an option and convert the input to integer
             optionNumber =Integer.parseInt(JOptionPane.showInputDialog(null,"Enter option (1, 2, or 3):\n"
                    + "Option 1) Add tasks\n"
                    + "Option 2) Show report \n"
                    + "Option 3) Quit"));
           
            //This is a if statment. If user chooses option 1, it will allow the user to add tasks.
            if (optionNumber == 1) { //(Stackflow. 2013)
               // This prompts user to enter a number of tasks and initialize arrays to store task details.
               numberOfTasks =Integer.parseInt(JOptionPane.showInputDialog(null,"Enter Number of tasks:"));

                taskName = new String[numberOfTasks];
                taskDescription = new String[numberOfTasks];
                developerDetails = new String[numberOfTasks];
                taskStatusString = new String[numberOfTasks];
                taskID = new String[numberOfTasks];
                taskDuration = new int[numberOfTasks];
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
                JOptionPane.showMessageDialog(null,"\n Number of tasks: " + numberOfTasks
                                              + "\n Total Hours: " + totalHours);//Displays the number of tasks entered.
                outputDetails.printTaskDetails(taskNumber, numberOfTasks, i, taskName, taskDescription, developerDetails, taskDuration, taskID, taskStatusString);
                
                //Calls printDetails method.
                
            

           
     } else if (optionNumber == 2) {    
                 ShowReportMethods.showReport();//Prompts the Show Report Option
        
             } else if (optionNumber == 3) {
                JOptionPane.showMessageDialog(null,"Goodbye");//Propmts exit message.
            }
    }
    
    }
    // A method to register the user and state the successfullness of the registration.
    public static void registerUser(String userName, String password) {
        // An if statement to check if both the username and password meet the criteria.
        if (RegistrationMethods.checkUserName(userName) && RegistrationMethods.checkPasswordComplexity(password)) {
            JOptionPane.showMessageDialog(null, "Registration successful"); // Displays a registration success message.
        } else {
             JOptionPane.showMessageDialog(null, "Registration failed. Please check if your username and password fit the criteria"); // Displays a registration failure message.
        }
    }
    
    

    // A method used to as a login page and to check the login status of the user.
    public static void returnLoginStatus() {
        String loginUserName;
        String loginPassword;
        //A while loop used to loop the login procces until the login username and password is the same as ones from the ones inputed in registration.
        while (true) {
           loginUserName = JOptionPane.showInputDialog(null, "Enter your username:");
           loginPassword = JOptionPane.showInputDialog(null, "Enter your password:");// Prompt the user a message to enter their login username and password.

            boolean loginStatus = LoginMethods.loginUser(loginUserName, loginPassword, userName, password);
            //This boolean is used to call back to the loginUser method and store it as a variable.(Stackflow. 2022)

            //An if statement used to display a message stating the result of the loginUser method.
            if (loginStatus) {
                JOptionPane.showMessageDialog(null, "Welcome " + firstName + " " + lastName + ", it is great to see you again."); // Display a login success message.
                break;
            } else {
                JOptionPane.showMessageDialog(null, "Login failed. Username or password incorrect, please try again"); // Display a login failure message.
            }
        
        }
        
    }  
    
    
 
     
    
    
}
//(Farrel. 2018)





/*REFERENCE LIST:
·         BeginnersBook. 2022. Java String charAt() Method example [Online]. Available at: https://beginnersbook.com/2013/12/java-string-charat-method-example/#:~:text=The%20Java%20String%20charAt(int,string%20represented%20by%20instance%20s. [Accessed 2 May 2024]
·         Code Knowledge. 2024. For loop in Java: counting loop that repeats the code a specific number of times. [Online]. Available at: https://code-knowledge.com/java-for-loop/#:~:text=In%20Java%2C%20you%20use%20the,a%20predetermined%20number%20of%20times. [Accessed 7 May 2024]
·         DANIWEB. 2013. How can I count the number of user inputs?. [Online]. Available at: https://www.daniweb.com/programming/software-development/threads/349058/how-can-i-count-the-number-of-user-inputs. [Accessed 2 May 2024]
·         Farrel, J. 2018.Java™ Programming.9th edt. Boston: Cengage.
·         GeeksforGeeks. 2020. How to validate a Username using Regular Expressions in Java. [Online]. Available at: https://www.geeksforgeeks.org/how-to-validate-a-username-using-regular-expressions-in-java/. [Accessed 1 April 2024]
·         GeeksforGeeks. 2022. How to find the first and last character of a string in Java. [Online]. Available at: https://www.geeksforgeeks.org/how-to-find-the-first-and-last-character-of-a-string-in-java/. [Accessed 7 May 2024]
·         GeeksForGeeks. 2023. How to add an element to an Array in Java?. [Online]. Available at: https://www.geeksforgeeks.org/how-to-add-an-element-to-an-array-in-java/ . [Accessed 4 June 2024]
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

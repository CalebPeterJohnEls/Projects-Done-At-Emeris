/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.mycompany.prog1a_poe;

import static com.mycompany.prog1a_poe.PROG1A_POE.developerDetails;
import static com.mycompany.prog1a_poe.PROG1A_POE.i;
import static com.mycompany.prog1a_poe.PROG1A_POE.numberOfTasks;
import static com.mycompany.prog1a_poe.PROG1A_POE.taskDescription;
import static com.mycompany.prog1a_poe.PROG1A_POE.taskDuration;
import static com.mycompany.prog1a_poe.PROG1A_POE.taskID;
import static com.mycompany.prog1a_poe.PROG1A_POE.taskName;
import static com.mycompany.prog1a_poe.PROG1A_POE.taskStatusString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/**
 *
 * @author lab_services_student
 */
public class PROG1A_POETest {
    
    public PROG1A_POETest() {
    }

    /*AssertEquals test of checkPasswordComplexity method a correct username .*/
@Test
public void testCheckUserNameEqualsTrue() {
    System.out.println("checkUserName");
    String userName = "kyl_1"; 
    boolean strExpected = true; 
    boolean strActual = RegistrationMethods.checkUserName(userName);
    Assertions.assertEquals(strExpected, strActual);
}

/*AssertEquals test of checkPasswordComplexity method a incorrect username .*/
@Test
public void testCheckUserNameEqualsFalse() {
    System.out.println("checkUserName");
    String userName = "kyle!!!!!!!"; 
    boolean strExpected = false; 
    boolean strActual = RegistrationMethods.checkUserName(userName);
    Assertions.assertEquals(strExpected, strActual);
}

  /*AssertEquals test of checkPasswordComplexity method a correct password.*/
    @Test
    public void testCheckPasswordComplexityTrue() {
        System.out.println("checkPasswordComplexity");
        String password = "Ch&&sec@ke99!";
        boolean strExpected = true;
        boolean strActual = RegistrationMethods.checkPasswordComplexity(password);
        assertEquals(strExpected, strActual);
    }

    /*AssertEquals test of checkPasswordComplexity method for an incoorect password.*/
    @Test
    public void testCheckPasswordEqualsFalse() {
        System.out.println("checkPasswordComplexity");
        String password = "password";
        boolean strExpected = false;
        boolean strActual = RegistrationMethods.checkPasswordComplexity(password);
        assertEquals(strExpected, strActual);
    }
    
    /*AssertTrue test of checkPasswordComplexity method.*/
    @Test
    public void testUserNameAssertTrue(){
        String userName = "kyl_1";
        boolean strActual = RegistrationMethods.checkUserName(userName);
        assertTrue(strActual);
    }
    
     /*AssertFalse test of checkPasswordComplexity method.*/
    @Test
    public void testUserNameAssertFalse(){
        String userName = "kyle!!!!!!!";
        boolean strActual = RegistrationMethods.checkUserName(userName);
        assertFalse(strActual);
    }
    
    /*AssertTrue test of checkPasswordComplexity method.*/
    @Test
    public void testCheckPasswordAssertTrue(){
        String password = "Ch&&sec@ke99!";
        boolean strActual = RegistrationMethods.checkPasswordComplexity(password);
        assertTrue(strActual);
    }
    
    /*AssertFalse test of checkPasswordComplexity method.*/
    @Test
    public void testCheckPasswordAssertFalse(){
        String password = "password!";
        boolean strActual = RegistrationMethods.checkPasswordComplexity(password);
        assertFalse(strActual);
    }

    /*AssertTrue test of loginUser method.*/
    @Test
    public void testLoginUserTrue() {
        System.out.println("loginUser");
        String loginUserName = "kyl_1";
        String loginPassword = "Ch&&sec@ke99!";
        String storedUserName = "kyl_1";
        String storedPassword = "Ch&&sec@ke99!";
        boolean strActual = LoginMethods.loginUser(loginUserName, loginPassword, storedUserName, storedPassword);
        assertTrue(strActual);
    }

    /*AssertFalse test of loginUser method.*/
    @Test
    public void testLoginUserFalse() {
        System.out.println("loginUser");
        String loginUserName = "kyl_1";
        String loginPassword = "Ch&&sec@ke99!";
        String storedUserName = "kyle!!!!!!!";
        String storedPassword = "password";
        boolean strActual = LoginMethods.loginUser(loginUserName, loginPassword, storedUserName, storedPassword);
        assertFalse(strActual);
    }
    
    //Part 2 Testing
     /*AssertEquals test of checkPasswordComplexity method for an correct description length.*/
    @Test
    public void testCheckTaskDescriptionTrue() {
        System.out.println("checkTaskDescription");
        String taskDescription = "Create Login to authenticate users";
        boolean strExpected = true; // Assuming this is what you expect
        boolean strActual = DescriptionCheck.checkTaskDescription(taskDescription);
        assertEquals(strExpected, strActual);
}
      /*AssertEquals test of checkPasswordComplexity method for an incorrect description length.*/
    @Test
    public void testCheckTaskDescriptionFalse() {
        System.out.println("checkTaskDescription");
        String taskDescription = "";
        boolean strExpected = false; // Assuming this is what you expect
        boolean strActual = DescriptionCheck.checkTaskDescription(taskDescription);
        assertEquals(strExpected, strActual);
}
    @Test
    public void testPrintTaskDetails() {
        System.out.println("printTaskDetails");
        int taskNumber = 0;
        int numberOfTasks = 0;
        int i = 0;
        String[] taskName = null;
        String[] taskDescription = null;
        String[] developerDetails = null;
        int[] taskDuration = null;
        String[] taskID = null;
        String[] taskStatusString = null;
        String expResult = "";
        String result = outputDetails.printTaskDetails(taskNumber, numberOfTasks, i, taskName, taskDescription, developerDetails, taskDuration, taskID, taskStatusString);
        assertEquals(expResult, result);
        
    }
@Test
    public void testCreateTaskID() {
        System.out.println("createTaskID");
        int i = 0;
        String[] developerDetails = {"Eren, Yeager"};
        String[] taskName = {"Rumbling"};
        String expResult = "RU:1:GER";
        String result = DataAutoGen.createTaskID(i, developerDetails, taskName);
        assertEquals(expResult, result);
       
    }

    /**
     * Test of returnTotalHours method, of class DataAutoGen.
     */
    @Test
    public void testReturnTotalHours() {
        System.out.println("returnTotalHours");
        int[] taskDuration = {2,3};
        int i = 0;
        int totalHours = 5;
        int expResult = 7;
        int result = DataAutoGen.returnTotalHours(taskDuration, i, totalHours);
        assertEquals(expResult, result);
       
    }
    //(Farrel. 2028)
     //(Write additional unit tests and update your .yaml file. 2022)
    
public void setUp(){
    PROG1A_POE.taskName = new String[4];
    PROG1A_POE.developerDetails = new String[4];
    PROG1A_POE.taskStatusString = new String[4];
    PROG1A_POE.taskDuration = new int[4];

    PROG1A_POE.taskName[0] = "Create Login";
    PROG1A_POE.taskName[1] = "Create Add Features";
    PROG1A_POE.taskName[2] = "Create Reports";
    PROG1A_POE.taskName[3] = "Add Arrays";

    PROG1A_POE.developerDetails[0] = "Mike Smith";
    PROG1A_POE.developerDetails[1] = "Edward Harrington";
    PROG1A_POE.developerDetails[2] = "Samantha Paulson";
    PROG1A_POE.developerDetails[3] = "Glenda Oberholzer";

    PROG1A_POE.taskDuration[0] = 5;
    PROG1A_POE.taskDuration[1] = 8;
    PROG1A_POE.taskDuration[2] = 2;
    PROG1A_POE.taskDuration[3] = 11;

    PROG1A_POE.taskStatusString[0] = "To Do";
    PROG1A_POE.taskStatusString[1] = "Doing";
    PROG1A_POE.taskStatusString[2] = "Done";
    PROG1A_POE.taskStatusString[3] = "To Do";
}
   @Test
    public void testTaskWithLongestDuration() {
        String expResult = "Glenda Oberholzer" ;
        String result = ShowReportMethods.taskWithLongestDuration();
        assertEquals(expResult, result);
    }

    @Test
    public void testSearchTaskByName() {
        String expResult = "Mike Smith, Create Login";
        String result = ShowReportMethods.searchTaskByName(taskName[2]);
        assertEquals(expResult, result);
    }

    @Test
    public void testSearchTasksByDeveloper() {
        String[] developerDetails= {"Samantha Paulson"};
        String expResult = "Create Report";
        String result = ShowReportMethods.searchTasksByDeveloper(developerDetails[2]);
        assertEquals(expResult, result);
    }

    @Test
    public void testDeleteTaskByName() {
        String[] taskName= {"Create Reports"};
        String expResult = "'Create Reports' successfully deleted";
        String result = ShowReportMethods.deleteTaskByName(taskName[0]);
        assertEquals(expResult, result);
    }

}




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
·         Sanfoundry.2024. Java Program to Illustrate how User Authentication is Done. [Online]. Available at: https://www.sanfoundry.com/java-program-illustrate-how-user-authentication-done/#:~:text=Enter%20username%20and%20password%20as,how%20User%20Authentication%20is%20Done. [Accessed 1 April 2024]
·         Show a drop down list in Java JOptionPane prompt. 2016. YouTube video, added by Brandan Jones. [Online]. Available at: https://www.youtube.com/watch?v=A-R9SrKQmGY [Accessed 2 May 2024].
·         Show a drop down list in Java JOptionPane prompt. 2016. YouTube video, added by Brandan Jones. [Online]. Available at: https://www.youtube.com/watch?v=A-R9SrKQmGY. [Accessed 2 May 2024].
·         Stackflow. 2013. JAVA: Creating a Menu Loop. [Online]. Available at: https://stackoverflow.com/questions/20681616/java-creating-a-menu-loop. [Accessed  1 May 2024]
·         Stackflow. 2014. Sum of previous two numbers entered [Online]. Available at: https://stackoverflow.com/questions/26956242/sum-of-previous-two-numbers-entered. [Accessed  2 May 2024]
·         Stackflow. 2017. java - Show JOptionPane (with dropdown menu) on the top of other windows [Online]. Available at: https://stackoverflow.com/questions/43658679/show-joptionpane-with-dropdown-menu-on-the-top-of-other-windows. [Accessed  2 May 2024]
·         Stackflow. 2022. Can I store a method in a variable in Java 8?. [Online]. Available at: https://stackoverflow.com/questions/29219984/can-i-store-a-method-in-a-variable-in-java-8 [Accessed 7 April 2024]
·         Stackflow. 2022. Valid Username without Regex. [Online]. Available at: https://stackoverflow.com/questions/70642136/valid-username-without-regex#:~:text=If%20the%20username%20consists%20of,digits%20%5B0-9%5D. [Accessed 1 April 2024]
·         Stackflow. 2023. Java: break statement in "if else". [Online]. Available at: https://stackoverflow.com/questions/20670824/java-break-statement-in-if-else. [Accessed 1 April 2024]
·         Stackflow. 2024. ArrayList input java [Online]. Available at: https://stackoverflow.com/questions/12056220/arraylist-input-java. [Accessed  7 May 2024]
·         W3Schools. 2024. Java String toUpperCase() Method. [Online]. Available at: https://www.w3schools.com/java/ref_string_touppercase.asp#:~:text=The%20toUpperCase()%20method%20converts,string%20to%20lower%20case%20letters. [Accessed  6 May 2024]
·         W3Schools. 2024. The JavaScript call() Method. [Online]. Available at: https://www.w3schools.com/js/js_function_call.asp#:~:text=The%20call()%20method%20is,method%20belonging%20to%20another%20object. [Accessed  6 April 2024]
·         Write additional unit tests and update your .yaml file. 2022. YouTube video, added by IIEVC School of Computer Science. [Online]. Available at: https://www.youtube.com/watch?v=DmL4gG9vG0A&list=PL480DYS-b_kfHSYf2yzLgto_mwDr_U-Q6&index=8 [Accessed 10 April 2024].

*/




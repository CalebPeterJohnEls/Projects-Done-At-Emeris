/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prog1b_assignq1;

import static com.mycompany.prog1b_assignq1.PROG1B_AssignQ1.age;
import static com.mycompany.prog1b_assignq1.PROG1B_AssignQ1.course;
import static com.mycompany.prog1b_assignq1.PROG1B_AssignQ1.email;
import static com.mycompany.prog1b_assignq1.PROG1B_AssignQ1.exitApplication;
import static com.mycompany.prog1b_assignq1.PROG1B_AssignQ1.exitOption;
import static com.mycompany.prog1b_assignq1.PROG1B_AssignQ1.menu;
import static com.mycompany.prog1b_assignq1.PROG1B_AssignQ1.name;
import static com.mycompany.prog1b_assignq1.PROG1B_AssignQ1.numberOfStudents;
import static com.mycompany.prog1b_assignq1.PROG1B_AssignQ1.studentId;
import java.util.Scanner;

/**
 *
 * @author lab_services_student
 */
public class CaptureAndSearch {
    //Save student method where the user adds student infomation to the array//(Farrel, J. 2018)
    public static void saveStudents(Scanner input) 
    {
        System.out.println("CAPTURE STUDENT");//Title
        System.out.println("*******************************************"); 
        
        System.out.println("Enter the number of students:");//User enters number of students they want to add
        numberOfStudents = Integer.parseInt(input.nextLine());

        studentId = new String[numberOfStudents];
        name = new String[numberOfStudents];
        email = new String[numberOfStudents];
        age = new int[numberOfStudents];
        course = new String[numberOfStudents];

         //A for loop used to loop the prompts for the details of each student based on the number of students there are.//(Farrel, J. 2018)
        for (int i = 0; i < numberOfStudents; i++) 
        {
            int studentNumber = i +1;
            System.out.println("STUDENT NUMBER " + studentNumber + ": \n"); 
              System.out.println("--------------------------------------\n");
            System.out.println("Enter student ID:");//User enters the student ID
            studentId[i] = input.nextLine();

            System.out.println("Enter student name:");//User enters student name
            name[i] = input.nextLine();

            System.out.println("Enter student email:");//User enters student email
            email[i] = input.nextLine();

            //a while loop that loops until the user enters the correct age//(Farrel, J. 2018)
            while (true) 
            {
                System.out.println("Enter student age:");//User enters student age(Stackoverflow. 2015)
                try 
                {
                    age[i] = Integer.parseInt(input.nextLine());
                    if (age[i] <= 16)//If the age is less than 16, it is incorrect
                    {
                        System.out.println("You have entered a incorrect student age!!!!!");//error message
                         System.out.println("Please re-enter student age>> ");//user must re-enter
                    } 
                    else
                    {
                        break;//moves to the next prompt if age is correct
                    }
                } 
                catch (NumberFormatException e) //Checks if the ser enters a string instead of an interger(Stackoverflow. 2015)
                {
                       System.out.println("You have entered a incorrect student age!!!!!");//if it is an string, the user must re-enter the correct age
                       System.out.println("Please re-enter student age>> ");
                }
            }

            System.out.println("Enter student course:");//User enters course
            course[i] = input.nextLine();
             System.out.println("--------------------------------------"); 
        }

        System.out.println("Enter 1 to return back to launch screen or any other key to exit:");//Option to exit program or go back to the launch menu
        exitOption = input.nextLine();
        if (exitOption.equals("1"))//if the enters 1, it takes them back to the menu//(Farrel, J. 2018)
        {
             menu(input);  
        }
        else 
        {
            exitApplication(input);//Else, it will exit the program//(Farrel, J. 2018)
        }
    }

//Search student method(Stackflow. 2020)
    public static void searchStudents(String searchId, Scanner input) 
    {
        System.out.println("--------------------------------------\n"); 
        //Allows user to search for student based on student id//(Farrel, J. 2018)(Stackflow. 2020)
        
        for (int i = 0; i < numberOfStudents; i++) 
        {
            if (studentId[i].equalsIgnoreCase(searchId))//if the student is found, the student's infomaton will be prompted //(Farrel, J. 2018)
            {
                System.out.println("Student ID: " + studentId[i]);
                System.out.println("Student Name: " + name[i]);
                System.out.println("Student Email: " + email[i]);
                System.out.println("Student Age: " + age[i]);
                System.out.println("Student Course: " + course[i]);   
               
               
                 break;
                
            }
            else 
        {
            //If not found, it will display an error//(Farrel, J. 2018)
            System.out.println("Student with Student ID: " + searchId + " is not found.");
        }

        }

        
        System.out.println("--------------------------------------"); 
        
        System.out.println("Enter 1 to return back to launch screen or any other key to exit:");
        exitOption = input.nextLine();
        if (exitOption.equals("1")) 
        {
           menu(input);   
        } 
        else 
        {
            exitApplication(input);
        }
    }

   //(Farrel, J. 2018)
  
}
/*Reference List:
•	Farrel, J. 2018.Java™ Programming.9th edt. Boston: Cengage.
•	Software Testing Help. 2024. Remove/Delete An Element From An Array In Java. [Online]. Available at: https://www.softwaretestinghelp.com/remove-element-from-array-java/#:~:text=To%20remove%20an%20element%20from,ArrayList%20back%20to%20the%20array.[Accessed 4 June 2024]
•	Baeldung. 2024. Stop Executing Further Code in Java. [Online]. Available at: https://www.baeldung.com/java-stop-running-code#:~:text=To%20stop%20the%20execution%20of%20further%20code%2C%20we%20can%20use,an%20exit%20status%20of%200.&text=We%20terminate%20the%20program%20using%20System. [Accessed 28  August 2024]
•	GeeksForGeeks. 2023. How to add an element to an Array in Java?. [Online]. Available at: https://www.geeksforgeeks.org/how-to-add-an-element-to-an-array-in-java/ . [Accessed 4 June 2024]
•	GeeksforGeeks. 2023. static Keyword in Java. [Online]. Available at: https://www.geeksforgeeks.org/static-keyword-java/. [Accessed 1 April 2024]
•	JUnit. 2024. JUnit 5 User Guide. [Online]. Available at: https://junit.org/junit5/docs/current/user-guide/. [Accessed 30 August 2024]
•	Stackflow. 2020. Searching a Array for unique name using method. [Online]. Available at: https://stackoverflow.com/questions/58490694/searching-a-array-for-unique-name-using-method. [Accessed 4 June 2024]
•	Stackoverflow. 2015. How to check the input is an integer or not in Java? [duplicate]. [Online]. Available at: https://stackoverflow.com/questions/19925047/how-to-check-the-input-is-an-integer-or-not-in-java. [Accessed 19 August 2024]

*/



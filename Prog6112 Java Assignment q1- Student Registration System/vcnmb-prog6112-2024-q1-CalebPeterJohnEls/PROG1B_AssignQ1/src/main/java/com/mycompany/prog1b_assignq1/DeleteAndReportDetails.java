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
public class DeleteAndReportDetails {
    //Delete Student method//(Farrel, J. 2018)(Software Testing Help. 2024)
    public static void deleteStudent(String deleteId, Scanner input) 
    {
         System.out.println("--------------------------------------"); 
         //Allows user to delete a user from the array based on their student id//(Farrel, J. 2018)
  
        for (int i = 0; i < numberOfStudents; i++) 
        {
            if (studentId[i].equalsIgnoreCase(deleteId)) //If the student id is found , then the user can choose to delete it
            {
                //asks user if the want to delete the student
                System.out.println("Are you sure you want to delete: " + deleteId + "? Enter 'y' to confirm:");
                String deleteOption = input.nextLine();

                if (deleteOption.equalsIgnoreCase("y")) //if "y" is entered, then the student's infomation is removed from the array//(Farrel, J. 2018)            
                {
                    System.out.println("Deleting student: " + studentId[i]);//(Software Testing Help. 2024)(Farrel, J. 2018)
                    for (int j = i; j < numberOfStudents - 1; j++) 
                    {
                        studentId[j] = studentId[j + 1];
                        name[j] = name[j + 1];
                        email[j] = email[j + 1];
                        age[j] = age[j + 1];
                        course[j] = course[j + 1];
                    }
                    numberOfStudents--; 
                     break;
                     }
                
             System.out.println("--------------------------------------");
        //if not, the user is given the option to go back to the launch screen or exit the program//(Farrel, J. 2018)
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
            else //if the student id is not found, an error message will appear
        {
            
            System.out.println("Student with Student ID: " + deleteId + " is not found.");
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
    }//(Farrel, J. 2018)
//Student Report Method
    public static void studentReport(Scanner input) 
    {
       //This will show a full report of all the students that were added to the array
        for (int i = 0; i < numberOfStudents; i++) 
        {
             System.out.println("--------------------------------------"); 
            System.out.println("\nSTUDENT " + (i + 1)
                    + "\n--------------------------------------"
                    + "\nStudent ID: " + studentId[i]
                    + "\nStudent Name: " + name[i]
                    + "\nStudent Email: " + email[i]
                    + "\nStudent Age: " + age[i]
                    + "\nStudent Course: " + course[i]);
            System.out.println("--------------------------------------"); 
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
•	GeeksForGeeks. 2023. How to add an element to an Array in Java?. [Online]. Available at: https://www.geeksforgeeks.org/how-to-add-an-element-to-an-array-in-java/ . [Accessed 4 June 2024]
•	GeeksforGeeks. 2023. static Keyword in Java. [Online]. Available at: https://www.geeksforgeeks.org/static-keyword-java/. [Accessed 1 April 2024]
•	JUnit. 2024. JUnit 5 User Guide. [Online]. Available at: https://junit.org/junit5/docs/current/user-guide/. [Accessed 30 August 2024]
•	Stackflow. 2020. Searching a Array for unique name using method. [Online]. Available at: https://stackoverflow.com/questions/58490694/searching-a-array-for-unique-name-using-method. [Accessed 4 June 2024]
•	Stackoverflow. 2015. How to check the input is an integer or not in Java? [duplicate]. [Online]. Available at: https://stackoverflow.com/questions/19925047/how-to-check-the-input-is-an-integer-or-not-in-java. [Accessed 19 August 2024]

*/
 


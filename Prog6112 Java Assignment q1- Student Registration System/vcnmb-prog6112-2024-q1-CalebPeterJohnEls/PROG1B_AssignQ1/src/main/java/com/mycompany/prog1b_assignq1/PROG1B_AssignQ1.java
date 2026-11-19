/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.prog1b_assignq1;

import java.util.Scanner;

/**
 *
 * @author lab_services_student
 */
public class PROG1B_AssignQ1 {

    static String[] studentId;
    static String[] name;
    static String[] email;
    static int[] age;
    static String[] course;
    static int i = 0;
    static String exitOption;
    static int numberOfStudents;
/*These static variables are declared outside of the main method to store
    inputed data of the user and to insure that this data can be used through out
    code*/
    //(GeeksforGeeks. 2023)
    //(Farrel. 2018)
    public static void main(String[] args) 
    {
        Scanner input = new Scanner(System.in);

        System.out.println("STUDENT MANGEMENT AGPPLCATION");
        System.out.println("*******************************************");   
           //A welcome page
        System.out.println("Enter 1 to go to the launch screen or any other key to exit:");//asks if the user want to go to the lauch screen or exit
        exitOption = input.nextLine();
        if (exitOption.equals("1")) //If the user enters"1" they will be taken to the lauch screen//(Farrel, J. 2018)
        {
          menu(input);  
        } 
        else 
        {
            //If not, they will exit the program//(Farrel, J. 2018)
            System.out.println("Thank you for using the program");
          
        }
    }
     //This method gives users a list of options and each option will perform a specific method //(Farrel, J. 2018)  
        public static void menu(Scanner input)
        {
        //Loops choices to user till they choose an option//(Farrel, J. 2018)
        while (true) 
        {
            System.out.print("\n1. Capture a new student\n" +
                    "2. Search for a student\n" +
                    "3. Delete a student\n" +
                    "4. Print student report\n" +
                    "5. Exit Application\n");
            int choiceOption2 = Integer.parseInt(input.nextLine());
            switch (choiceOption2) 
            {
                case 1:
                    CaptureAndSearch.saveStudents(input);//Class to each method based on user's choice//(Farrel, J. 2018)
                    break;
                case 2:
                    System.out.println("Enter student ID to search:");
                    String searchID = input.nextLine();
                    CaptureAndSearch.searchStudents(searchID, input);
                    break;
                case 3:
                    System.out.println("Enter student ID to delete:");
                    String deleteID = input.nextLine();
                    DeleteAndReportDetails.deleteStudent(deleteID, input);
                    break;
                case 4:
                    DeleteAndReportDetails.studentReport(input);
                    break;
                case 5:
                     exitApplication(input);
                    
                default:
                    System.out.println("Invalid choice. Please try again.");//Error message if the user enters an invlaid choice//(Farrel, J. 2018)
                  
            }
        }
    }
//(Farrel, J. 2018)

   
    
    //Allows the user to exit the program(Baeldung. 2024)
    public static void exitApplication(Scanner input) 
    {
         System.out.println("Thank you for using the program");
         System.exit(0);
    }
}
/*REFERENCE LIST:
•	Farrel, J. 2018.Java™ Programming.9th edt. Boston: Cengage.
•	Software Testing Help. 2024. Remove/Delete An Element From An Array In Java. [Online]. Available at: https://www.softwaretestinghelp.com/remove-element-from-array-java/#:~:text=To%20remove%20an%20element%20from,ArrayList%20back%20to%20the%20array.[Accessed 4 June 2024]
•	Baeldung. 2024. Stop Executing Further Code in Java. [Online]. Available at: https://www.baeldung.com/java-stop-running-code#:~:text=To%20stop%20the%20execution%20of%20further%20code%2C%20we%20can%20use,an%20exit%20status%20of%200.&text=We%20terminate%20the%20program%20using%20System. [Accessed 28  August 2024]
•	GeeksForGeeks. 2023. How to add an element to an Array in Java?. [Online]. Available at: https://www.geeksforgeeks.org/how-to-add-an-element-to-an-array-in-java/ . [Accessed 4 June 2024]
•	GeeksforGeeks. 2023. static Keyword in Java. [Online]. Available at: https://www.geeksforgeeks.org/static-keyword-java/. [Accessed 1 April 2024]
•	JUnit. 2024. JUnit 5 User Guide. [Online]. Available at: https://junit.org/junit5/docs/current/user-guide/. [Accessed 30 August 2024]
•	Stackflow. 2020. Searching a Array for unique name using method. [Online]. Available at: https://stackoverflow.com/questions/58490694/searching-a-array-for-unique-name-using-method. [Accessed 4 June 2024]
•	Stackoverflow. 2015. How to check the input is an integer or not in Java? [duplicate]. [Online]. Available at: https://stackoverflow.com/questions/19925047/how-to-check-the-input-is-an-integer-or-not-in-java. [Accessed 19 August 2024]

*/


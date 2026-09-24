/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */



import com.mycompany.prog1b_assignq1.StudentsInfomation1;
import java.util.Scanner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author lab_services_student
 */

import org.junit.jupiter.api.Test;

public class TestJUnitTest {

    // This test method is used to verify that a student is saved with correct details//(Farrel, J. 2018)(JUnit. 2024.)
    @Test
    public void testSaveStudent() {
        StudentsInfomation1 student = new StudentsInfomation1(20, "Name", "Email", "Course", "1234"); //This creates a new student object with the given details

        //This asserts that the student infomation is correctly set(JUnit. 2024.)
        assertEquals("1234", student.getStudentID());
        assertEquals("Name", student.getName());
        assertEquals(20, student.getAge());
        assertEquals("Email", student.getEmail());
        assertEquals("Course", student.getCourse());
    }//(Farrel, J. 2018)(JUnit. 2024.)

     //This test method is used to verify that the student's age is valid//(Farrel, J. 2018)(JUnit. 2024.)
    @Test
    public void TestStudentAge_VaildAge() {
        StudentsInfomation1 student = new StudentsInfomation1(18, "Name", "Email", "Course", "1234");
        int age = 18;
        int strActual = student.getAge();
        assertEquals(age, strActual);
    }
    
    //This test method is used to verify that the student's age is not valid based on a string being entered//(Farrel, J. 2018)(JUnit. 2024.)
 @Test
    public void TestStudentAge_VaildAgeCharacter() {
        StudentsInfomation1 student = new StudentsInfomation1(18, "Name", "Email", "Course", "1234");
        String age = "C";
        int strActual = student.getAge();
        assertNotEquals(age, strActual);
    }
    
    //This test method is used to verify that the student's age is not valid based on the incorrect age entered//(Farrel, J. 2018)(JUnit. 2024.)
    @Test
    public void TestStudentAge_InvaildAge() {
        StudentsInfomation1 student = new StudentsInfomation1(18, "Name", "Email", "Course", "1234");
        int age = 12;
        int strActual = student.getAge();
        assertNotEquals(age, strActual);
    }

    // This test  method to verify that a student can be found based on their ID//(Farrel, J. 2018)(JUnit. 2024.)
    @Test
    public void testSearchStudentFound() {
        StudentsInfomation1 student = new StudentsInfomation1(20, "Name", "Email", "Course", "1234");
        boolean studentFound = student.getStudentID().equals("1234");

        assertTrue(studentFound, "Student should be found");
    }

     // This test  method to verify that a student can be not found based on their ID//(Farrel, J. 2018)(JUnit. 2024.)
    @Test
    public void testSearchStudentNotFound() {
        StudentsInfomation1 student = new StudentsInfomation1(20, "Name", "Email", "Course", "5678");
        boolean studentFound = student.getStudentID().equals("1234");

        assertFalse(studentFound, "Student should not be found");
    }
}//(Farrel, J. 2018)(JUnit. 2024.)
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

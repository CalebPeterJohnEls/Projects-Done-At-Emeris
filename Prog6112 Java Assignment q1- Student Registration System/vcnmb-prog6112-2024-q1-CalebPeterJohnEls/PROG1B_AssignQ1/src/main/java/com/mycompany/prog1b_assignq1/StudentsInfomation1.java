/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prog1b_assignq1;

/**
 *
 * @author lab_services_student
 */
public class StudentsInfomation1 {
   //This is used to capture the infomation form the methods and store it here so that it can be used in the unit tests//(Farrel, J. 2018)
    private String  studentId;
    private String name ;
    private String email;
     private int age ;
    private String course ;
    
    public StudentsInfomation1(int age, String name, String email, String course, String studentId)
            
    { 
        // These initialize the infomation varibles fields with the provided name value//(Farrel, J. 2018)
   this.studentId = studentId;
      this.name=name;
      this.email=email;
      this.course=course;
       this.age=age;
    }
    

    public String getStudentID()
    {
        return studentId;//Returns the infomation to be used in the Unit Test//(Farrel, J. 2018)
    }
    public String getName()
    {
        return name;
    }
    public String getEmail()
    {
        return email;
    }
    public int getAge()
    {
        return age;
    }
    public String getCourse()
    {
        return course;
    }
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


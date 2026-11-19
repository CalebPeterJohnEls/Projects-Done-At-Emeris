/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */

import com.mycompany.prog1b_a1part2.AttackChoiceResult;
import com.mycompany.prog1b_a1part2.GiveChoiceResult;
import com.mycompany.prog1b_a1part2.IgnoreChoiceResult;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author lab_services_student
 */
public class UnitTestQ2 
{
   // This test method to verify the scenario where the attack result is good.//(Farrel, J. 2018)(JUnit. 2024.)
   @Test
   public void testAttackResultGood() 
   { 
        AttackChoiceResult action = new AttackChoiceResult(1); // This creates an instance of ChoiceResult with the result set to 1 by also creating a new object.
        assertEquals(1, action.getResult());// This asserts that the result of the action is 1, confirming it is correct.
        action.proceed();//calls the action abstract method//(Farrel, J. 2018)(JUnit. 2024.)(Stackoverflow. 2016)
    }
   
    // This test method to verify the scenario where the attack result is bad.//(Farrel, J. 2018)(JUnit. 2024.)
@Test
   public void testAttackResultBad() 
   {
        AttackChoiceResult action = new AttackChoiceResult(2); // This creates an instance of ChoiceResult with the result set to 2.//(Farrel, J. 2018)(JUnit. 2024.)
        assertEquals(2, action.getResult());// This asserts that the result of the action is 2, confirming it is correct.//(Farrel, J. 2018)(JUnit. 2024.)(Stackoverflow. 2016)
        action.proceed();
    }
   
    // This test method to verify the scenario where the give result is good.//(Farrel, J. 2018)(JUnit. 2024.)(Stackoverflow. 2016)
   @Test
   public void testGiveResultGood() 
   {
        GiveChoiceResult action = new GiveChoiceResult(1); 
        assertEquals(1, action.getResult());
        action.proceed();
    }
   
    // This test method to verify the scenario where the give result is bad.//(Farrel, J. 2018)(JUnit. 2024.)(Stackoverflow. 2016)
@Test
   public void testGiveResultBad() 
   {
        GiveChoiceResult action = new GiveChoiceResult(2); 
        assertEquals(2, action.getResult());
        action.proceed();
    }
   
    // This test method to verify the scenario where the ignore result is good.//(Farrel, J. 2018)(JUnit. 2024.)(Stackoverflow. 2016)
   @Test
   public void testIgnoreResultGood() 
   {
        IgnoreChoiceResult action = new IgnoreChoiceResult(1); 
        assertEquals(1, action.getResult());
        action.proceed();
    }
   
    // This test method to verify the scenario where the ignore result is bad.//(Farrel, J. 2018)(JUnit. 2024.)
@Test
   public void testIgnoreResultBad() 
   {
        IgnoreChoiceResult action = new IgnoreChoiceResult(2); 
        assertEquals(2, action.getResult());
        action.proceed();
    }
   
    // This test method to verify the scenario where the rob result is good.//(Farrel, J. 2018)(JUnit. 2024.)(Stackoverflow. 2016)
@Test
   public void testRobResultGood() 
   {
        IgnoreChoiceResult action = new IgnoreChoiceResult(1); 
        assertEquals(1, action.getResult());
        action.proceed();
    }
   
    // This test method to verify the scenario where the rob result is good.//(Farrel, J. 2018)(JUnit. 2024.)(Stackoverflow. 2016)
@Test
   public void testRobResultBad() 
   {
        IgnoreChoiceResult action = new IgnoreChoiceResult(2); 
        assertEquals(2, action.getResult());
        action.proceed();
    }
  //(Farrel, J. 2018) (JUnit. 2024.)(Stackoverflow. 2016)

    
}
/*Reference List:
•	Farrel, J. 2018.Java™ Programming.9th edt. Boston: Cengage.
•	JavaTPoint. 2018. Java Random nextInt() Method. [Online]. Available at: https://www.javatpoint.com/post/java-random-nextint-method. [Accessed 29 August 2024]
•	JUnit. 2024. JUnit 5 User Guide. [Online]. Available at: https://junit.org/junit5/docs/current/user-guide/. [Accessed 30 August 2024]
•	Stackoverflow. 2014. Obtaining a collection of constructed subclassed types using reflection. [Online]. Available at: https://stackoverflow.com/questions/134161/obtaining-a-collection-of-constructed-subclassed-types-using-reflection/12979726#12979726. [Accessed 30 August 2024]
•	Stackoverflow. 2016 How to use JOptionPane with many options? java. [Online]. Available at: https://stackoverflow.com/questions/21957696/how-to-use-joptionpane-with-many-options-java. [Accessed 29 August 2024]

*/

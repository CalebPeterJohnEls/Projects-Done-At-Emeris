/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prog1b_a1part2;

import java.util.Random;
import javax.swing.JOptionPane;

/**
 *
 * @author lab_services_student
 */
//Detetimes and outputs the result of the Give choice//(Farrel, J. 2018)
//This class extends the abstract Action class.
public class GiveChoiceResult extends Action 
{
        private int result;// Private field to store the result of the give choice.//(Farrel, J. 2018)

    public GiveChoiceResult(int result)
 {
     // This assigns the provided result to the class's result field.//(Farrel, J. 2018)
        this.result = result;
    }
    public GiveChoiceResult() 
{
     //This randomly gerenates a number between 1 and 2. 1 being the good result and 2 being a bad one
    //this helps create a random outcome. Like the flip of a coin.(JavaTPoint. 2018.)
        Random random = new Random();
        this.result = random.nextInt(2) + 1;//this assigns a value of 1 or 2 to the result field.
    }
 // This overrides the abstract proceed method from the Give class.
    @Override
    public void proceed() //(Farrel, J. 2018)
    {
        
        JOptionPane.showMessageDialog(null, "Deciding fate....");
        
        if (result == 1)
 {//if the number generated is 1, the result of the users choice will be good
JOptionPane.showMessageDialog(null,"You give the man some coin."
                 + "\nHe thanks you kindly with a smile on his face and blesses you."
                 + "You then continue with your journey");        } else 
{//if the number generated is 2, the result of the users choice will be bad
JOptionPane.showMessageDialog(null,"As soon as you reach for your bag to give him some coin, "
                  + "\n he grabs your whole bag and runs off like a hooligan "
                  + "'SAYONARA SUCKER!!!' yells the old loon as he runs off with all your valuables."
                  + "Your jounery comes to a despressing end");
        }
    }
// This getter method to retrieve the value of the result field so it can be used in the main class and the unit testing//(Farrel, J. 2018)
    public int getResult()
 {
        return result;
    }
}
/*Reference List:
•	Farrel, J. 2018.Java™ Programming.9th edt. Boston: Cengage.
•	JavaTPoint. 2018. Java Random nextInt() Method. [Online]. Available at: https://www.javatpoint.com/post/java-random-nextint-method. [Accessed 29 August 2024]
•	JUnit. 2024. JUnit 5 User Guide. [Online]. Available at: https://junit.org/junit5/docs/current/user-guide/. [Accessed 30 August 2024]
•	Stackoverflow. 2014. Obtaining a collection of constructed subclassed types using reflection. [Online]. Available at: https://stackoverflow.com/questions/134161/obtaining-a-collection-of-constructed-subclassed-types-using-reflection/12979726#12979726. [Accessed 30 August 2024]
•	Stackoverflow. 2016 How to use JOptionPane with many options? java. [Online]. Available at: https://stackoverflow.com/questions/21957696/how-to-use-joptionpane-with-many-options-java. [Accessed 29 August 2024]

*/


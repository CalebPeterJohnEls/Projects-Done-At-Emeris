/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.prog1b_a1part2;

import java.util.Random;
import javax.swing.JOptionPane;

/**
 *
 * @author lab_services_student
 */
public class PROG1B_A1Part2 {
    static String startOption;

    public static void main(String[] args) 
    {
   
      
   JOptionPane.showMessageDialog(null,"Welome to the Midland Saga: Stranger on the path");
   startOption = JOptionPane.showInputDialog(null,"Enter Y to start or any other key to exit:");
         
        if ( startOption.equals("y"))//If the user enter "y', the user will proceed //(Farrel, J. 2018)
        {
          
         JOptionPane.showMessageDialog(null,"You are a lone mercenary on a jounery to the great city of Port Grifith."
                + "\n As you carry your axe on side, and satchel on the other,  "
                + "\n you have had little trouble dealing with treats throughout your journey,thanks to your cunning wit and strength."
                + "\n Halfway through your journey, you walk across a deserted battlefield, "
                + "\n you come across a old man in a cloak, sitting under a tree."
                + "\n 'Good day. Could you spare a poor old veteran some change?' he asks you"
);
        
//This while loop gives users a list of options and each option will perform a specific method //(Farrel, J. 2018)
       while (true) {
            String[] options = {"1. Attack him with your axe", 
                                "2. Give him coin", 
                                "3. Try to rob him", 
                                "4. Ignore him and walk on", 
                                "5. End story"};//An array of options given to the user as buttons(Stackoverflow. 2016)
            
            int choice = JOptionPane.showOptionDialog(null, "How do you respond to him?", "Choose an action",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
             Action action = null;
            switch (choice) {
                case 0:
                     action = new AttackChoiceResult();//Calls the method required based on user choice //(Farrel, J. 2018)
                    break;
                case 1:
                    action = new GiveChoiceResult();
                    break;
                case 2:
                    action = new RobChoiceResult();
                    break;
                case 3:
                    action = new IgnoreChoiceResult();
                    break;
                case 4:
                  JOptionPane.showMessageDialog(null,"Thats unfortunate......."
                            + "\n Well, hopefully you will return soon to finish your jounrny");//If user chooses to exit program

                 return;
                default:
                     JOptionPane.showMessageDialog(null,"Incorrect choice dummy!");//Error if user enters wrong choices

            } 
            action.proceed(); //calls to the abstract class //(Farrel, J. 2018)
        }
         
        }
        else
        {
             JOptionPane.showMessageDialog(null,"See you agin soon traveler");// if user chooses not to proceed they will exit the program
        }
    }   
  //(Farrel, J. 2018)
}
/*REFERENCE LIST:
•	Farrel, J. 2018.Java™ Programming.9th edt. Boston: Cengage.
•	JavaTPoint. 2018. Java Random nextInt() Method. [Online]. Available at: https://www.javatpoint.com/post/java-random-nextint-method. [Accessed 29 August 2024]
•	JUnit. 2024. JUnit 5 User Guide. [Online]. Available at: https://junit.org/junit5/docs/current/user-guide/. [Accessed 30 August 2024]
•	Stackoverflow. 2014. Obtaining a collection of constructed subclassed types using reflection. [Online]. Available at: https://stackoverflow.com/questions/134161/obtaining-a-collection-of-constructed-subclassed-types-using-reflection/12979726#12979726. [Accessed 30 August 2024]
•	Stackoverflow. 2016. How to use JOptionPane with many options? java. [Online]. Available at: https://stackoverflow.com/questions/21957696/how-to-use-joptionpane-with-many-options-java. [Accessed 29 August 2024]

*/

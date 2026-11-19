/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.prog1a_poe;

/**
 *
 * @author lab_services_student
 */
public class RegistrationMethods {
     // A method with an if statemant to check if the username is vaild or not.
    
    public static boolean checkUserName(String userName) {
        if (userName.length() < 1 || userName.length() > 5)
            return false; // To check if the username's length is between 1 and 5 characters long.

        if (!userName.matches("[a-zA-Z0-9]*_+[a-zA-Z0-9]*"))//(GeeksforGeeks. 2020)(GeeksforGeeks. 2023)
            return false; /* To check if the username has an underscore (_) and 
                            to have it contain any type of letter, number and special character.*/

        return true; // To state that the username is valid if it has what is needed(the true will also go into loop that outputs the messages).
    }

    // A method with an if statemant to check if the username is vaild or not.
    public static boolean checkPasswordComplexity(String password) {
        if (password.length() < 8)
            return false; // To check if the password's length is at least 8 characters long.

        if (!password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[~!@#$%^&*()_-]).+$"))//(GeeksforGeeks. 2020)(GeeksforGeeks. 2023)
            return false; /* To check if the password contains at least one uppercase letter, 
                            one digit, and one special character.*/

        return true; // To state that the password is valid if it has what is needed.
    }
    
}

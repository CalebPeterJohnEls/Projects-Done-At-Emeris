package com.example.task1

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.util.Log
import kotlin.math.sqrt

//(IIE, 2023)

class MainActivity: AppCompatActivity() {
    private val TAG = "error checking"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        calculator()

    }//(IIE, 2023)

    @SuppressLint("SuspiciousIndentation")
    fun calculator(){

        var editNumberOne = findViewById<EditText>(R.id.editNumberOne) //where the first number will be entered

        var editNumberTwo = findViewById<EditText>(R.id.editNumberTwo) //where the second number will be entered

        var buttonAdd = findViewById<Button>(R.id.buttonAdd) //addition button

        var buttonSub = findViewById<Button>(R.id.buttonSub) //subtraction button

        var buttonMulti = findViewById<Button>(R.id.buttonMulti) //multiplication button

        var buttonDiv = findViewById<Button>(R.id.buttonDiv) //division button

        var buttonSquare = findViewById<Button>(R.id.buttonSquare) //square button

        var buttonPower = findViewById<Button>(R.id.buttonPower) //power button

        var buttonStats = findViewById<Button>(R.id.buttonStats)

        var answerResult = findViewById<TextView>(R.id.answerResult) // where the result will display
        //(IIE, 2023)

        buttonAdd.setOnClickListener{

        var addOne =editNumberOne.text.toString().toInt()
        var addTwo =editNumberTwo.text.toString().toInt()
        val answer =addOne + addTwo //the calculation
            answerResult.setTextColor(Color.WHITE)
            answerResult.text = "${addOne.toString()} + ${addTwo.toString()} = ${answer.toString()} " //the calculation  with result displaying for the user
            Log.v(TAG,"addOne + addTwo = ${answer.toString()} ")
    }
        buttonSub.setOnClickListener{

            var subOne =editNumberOne.text.toString().toInt()
            var subTwo =editNumberTwo.text.toString().toInt()
            val answer =subOne - subTwo //the calculation
            answerResult.setTextColor(Color.WHITE)
            answerResult.text = "${subOne.toString()} - ${subTwo.toString()} = ${answer.toString()} "//the calculation  with result displaying for the user
            Log.v(TAG,"subOne - subTwo = ${answer.toString()} ")
        }
        buttonMulti.setOnClickListener{

            var multiOne =editNumberOne.text.toString().toInt()
            var multiTwo =editNumberTwo.text.toString().toInt()
            val answer =multiOne * multiTwo //the calculation
            answerResult.setTextColor(Color.WHITE)
            answerResult.text = "${multiOne.toString()} * ${multiTwo.toString()} = ${answer.toString()} "//the calculation  with result displaying for the user
            Log.v(TAG,"multiOne * multiTwo = ${answer.toString()} ")
        }
        buttonDiv.setOnClickListener{

            var divOne =editNumberOne.text.toString().toInt()
            var divTwo =editNumberTwo.text.toString().toInt()


            if (divTwo==0){
                answerResult.text = "ERROR!                       NO NUMBERS         BELOW 0!!!"
                answerResult.setTextColor(Color.RED)
            }
            else{
                val answer =divOne / divTwo //the calculation
                val remainder = divOne % divTwo //the remainder
                answerResult.setTextColor(Color.WHITE)
            answerResult.text = "${divOne.toString()} / ${divTwo.toString()} = ${answer.toString()} . ${remainder.toString()}"//the calculation  with result and remainder displaying for the user
            Log.v(TAG,"divOne / divTwo= ${answer.toString()} remainder ${remainder.toString()}")}
        }//if the second number enter is 0, an error message will appear.


        buttonSquare.setOnClickListener{

            var squareOne = editNumberOne.text.toString().toDouble()// Declaring the first number


            if (squareOne < 0) {
                val result = sqrt(-squareOne) // Calculate the square root of the absolute value
                answerResult.text = "sqrt(${squareOne.toString()}) = ${result.toString()}i"
                answerResult.setTextColor(Color.WHITE)
                Log.v(TAG, "sqrt($squareOne) = ${result.toString()}i")
            } else {
                val result = sqrt(squareOne)

                answerResult.text = "sqrt(${squareOne.toString()}) = ${result.toString()}"
                Log.v(TAG, "sqrt($squareOne) = ${result.toString()}")
            }//If the number entered is less than 0(a negative number), the answer will appear with the letter 'i' next to it

        }
        buttonPower.setOnClickListener{

            var powerOneBase =editNumberOne.text.toString().toInt()
            var powerTwoExponent =editNumberTwo.text.toString().toInt()
            val tempExponent= editNumberTwo.text.toString().toInt()
            var result : Long = 1// the long is specifying the variable type
            while (powerTwoExponent != 0) { // using a while loop to 
                result *= powerOneBase.toLong()
                --powerTwoExponent} //the calculations
            answerResult.text = "${powerOneBase.toString()} ^ ${tempExponent.toString()} = ${result.toString()}" //the calculation  with result and remainder displaying for the user
            answerResult.setTextColor(Color.WHITE)
            Log.v(TAG,"powerOneBase^powerTwoExponent= ${result.toString()}")
            //(Programiz, 2023)
        }

        buttonStats.setOnClickListener {
            val intent = Intent( this, StatisticsPage::class.java)
            startActivity(intent)
        }
    }

}




//(IIE, 2023)

//Reference:
//The IIE. 2023. INTRODUCTION TO MOBILE APPLICATION DEVELOPMENT IMAD5112 [Module Manual]. The Independent Institute of Education: Unpublished.
//Programiz. 2023. Kotlin Program to Calculate the Power of a Number, 2023. [Online]. Available at: https://www.programiz.com/kotlin-programming/examples/power-number . [Accessed 12 September 2023]
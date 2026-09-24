package com.example.task1

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class StatisticsPage : AppCompatActivity() {
    // Initialise an array to store numbers
    var numbers = arrayListOf<Int>(0, 0, 0, 0, 0, 0, 0, 0 ,0 ,0)
    var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics_page)

        var buttonAvr = findViewById<Button>(R.id.buttonAvr)
        var resultTitle = findViewById<TextView>(R.id.resultTitle)
        var buttonMinMax = findViewById<Button>(R.id.buttonMinMax)


        buttonAvr.setOnClickListener {//Calculates the average of the values in the array

            var count = 0
             var average = 0
             var final = 0
             while (count != counter) {
                 average += numbers[count]
                 count++
             }
                  final = average / counter
            resultTitle.text = "Average = $final"


        }
//(IIE, 2023)

        buttonMinMax.setOnClickListener {//Calculates the largest and smallest value of the array
            if (numbers.isNotEmpty()) {
                var largest = numbers[0]
                var smallest = numbers[0]

                for (num in numbers) {
                    if (num > largest) {
                        largest = num
                    }
                    if (num < smallest) {
                        smallest = num
                    }
                }

                resultTitle.text = "Max = $largest, Min = $smallest"
            }
            //(Programiz, 2023)
        }

//(IIE, 2023)

        var storedResult = findViewById<TextView>(R.id.storedResult)
        var textViewError = findViewById<TextView>(R.id.textViewError)


        var buttonClear = findViewById<Button>(R.id.buttonClear)
        buttonClear.setOnClickListener {
            counter = 0 // To reset the counter
            for (i in 0 until 10) {
                numbers[i] = 0 // Reset the array
            }
            textViewError.text = "" // To clear the error message
            storedResult.text = "" // To clear all the numbers in the array
            resultTitle.text = "" // To clear the answer
        }

        calculator()
    }
//(IIE, 2023)

    fun calculator() {
        var editNumber = findViewById<EditText>(R.id.editNumber)
        var buttonAddStat = findViewById<Button>(R.id.buttonAddStat)
        var storedResult = findViewById<TextView>(R.id.storedResult)
        var textViewError = findViewById<TextView>(R.id.textViewError)


        buttonAddStat.setOnClickListener { // Add numbers entered to array
            val inputNumber = editNumber.text.toString()

            if (counter < numbers.size) {
                try {
                    val number = inputNumber.toInt()
                    numbers[counter] = number
                    counter++
                    val numberDisplay = numbers.joinToString()
                    storedResult.text = numberDisplay
                } catch (e: NumberFormatException) {
                    textViewError.text = "Invalid input. Please enter a number." //Error for invalid inout
                    textViewError.setTextColor(Color.RED)
                }
            } else {
                textViewError.text = "Array is full, cannot store more values."//FError for full array
                textViewError.setTextColor(Color.RED)
            }

        }




    }
}

//(IIE, 2023)


//Reference:
//The IIE. 2023. INTRODUCTION TO MOBILE APPLICATION DEVELOPMENT IMAD5112 [Module Manual]. The Independent Institute of Education: Unpublished.
//Programiz. 2023. Kotlin Program to Find Largest Element in an Array, 2023. [Online]. Available at: https://www.programiz.com/kotlin-programming/examples/largest-array. [Accessed 7 November 2023]
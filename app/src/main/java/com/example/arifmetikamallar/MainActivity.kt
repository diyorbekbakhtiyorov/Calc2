package com.example.arifmetikamallar

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.arifmetikamallar.data.CalculatorDatabaseHelper
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tvExpression: TextView
    private lateinit var tvResult: TextView
    private lateinit var dbHelper: CalculatorDatabaseHelper

    private var firstOperand: Double? = null
    private var pendingOperator: String? = null
    private var currentInput: String = ""
    private var isNewInput: Boolean = true

    private val historyLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedResult = result.data?.getStringExtra(HistoryActivity.EXTRA_SELECTED_RESULT)
            if (!selectedResult.isNullOrEmpty()) {
                currentInput = selectedResult
                tvResult.text = currentInput
                tvExpression.text = ""
                firstOperand = selectedResult.toDoubleOrNull()
                pendingOperator = null
                isNewInput = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = CalculatorDatabaseHelper(this)

        drawerLayout = findViewById(R.id.drawerLayout)
        tvExpression = findViewById(R.id.tvExpression)
        tvResult = findViewById(R.id.tvResult)

        val btnMenu: ImageButton = findViewById(R.id.btnMenu)
        val btnQuickHistory: ImageButton = findViewById(R.id.btnQuickHistory)
        val navItemHistory: View = findViewById(R.id.navItemHistory)
        val navItemAbout: View = findViewById(R.id.navItemAbout)

        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        btnQuickHistory.setOnClickListener {
            openHistory()
        }

        navItemHistory.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            openHistory()
        }

        navItemAbout.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, AboutActivity::class.java))
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        setupKeypad()
    }

    private fun openHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        historyLauncher.launch(intent)
    }

    private fun setupKeypad() {
        val digitButtonIds = mapOf(
            R.id.btn0 to "0",
            R.id.btn1 to "1",
            R.id.btn2 to "2",
            R.id.btn3 to "3",
            R.id.btn4 to "4",
            R.id.btn5 to "5",
            R.id.btn6 to "6",
            R.id.btn7 to "7",
            R.id.btn8 to "8",
            R.id.btn9 to "9"
        )

        for ((id, digit) in digitButtonIds) {
            findViewById<Button>(id).setOnClickListener {
                onDigitPressed(digit)
            }
        }

        findViewById<Button>(R.id.btnDot).setOnClickListener { onDotPressed() }
        findViewById<Button>(R.id.btnPlusMinus).setOnClickListener { onPlusMinusPressed() }
        findViewById<Button>(R.id.btnPercent).setOnClickListener { onPercentPressed() }

        findViewById<Button>(R.id.btnAdd).setOnClickListener { onOperatorPressed("+") }
        findViewById<Button>(R.id.btnMinus).setOnClickListener { onOperatorPressed("−") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { onOperatorPressed("×") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { onOperatorPressed("÷") }

        findViewById<Button>(R.id.btnEquals).setOnClickListener { onEqualsPressed() }
        findViewById<Button>(R.id.btnAC).setOnClickListener { onClearPressed() }
        findViewById<Button>(R.id.btnBackspace).setOnClickListener { onBackspacePressed() }
    }

    private fun onDigitPressed(digit: String) {
        if (isNewInput) {
            currentInput = digit
            isNewInput = false
        } else {
            if (currentInput == "0") {
                currentInput = digit
            } else {
                currentInput += digit
            }
        }
        tvResult.text = currentInput
    }

    private fun onDotPressed() {
        if (isNewInput) {
            currentInput = "0."
            isNewInput = false
        } else if (!currentInput.contains(".")) {
            currentInput += "."
        }
        tvResult.text = currentInput
    }

    private fun onPlusMinusPressed() {
        if (currentInput.isEmpty() || currentInput == "0") return

        currentInput = if (currentInput.startsWith("-")) {
            currentInput.substring(1)
        } else {
            "-$currentInput"
        }
        tvResult.text = currentInput
    }

    private fun onPercentPressed() {
        val value = currentInput.toDoubleOrNull() ?: return
        val result = value / 100.0
        currentInput = formatNumber(result)
        tvResult.text = currentInput
    }

    private fun onOperatorPressed(op: String) {
        val inputValue = currentInput.toDoubleOrNull()

        if (inputValue != null) {
            if (firstOperand == null) {
                firstOperand = inputValue
            } else if (!isNewInput && pendingOperator != null) {
                val calculated = calculate(firstOperand!!, inputValue, pendingOperator!!)
                if (calculated == null) return
                firstOperand = calculated
            }
        }

        pendingOperator = op
        isNewInput = true
        if (firstOperand != null) {
            tvExpression.text = "${formatNumber(firstOperand)} $op"
            tvResult.text = formatNumber(firstOperand)
        }
    }

    private fun onEqualsPressed() {
        val secondOperand = currentInput.toDoubleOrNull()

        if (firstOperand != null && pendingOperator != null && secondOperand != null) {
            val result = calculate(firstOperand!!, secondOperand, pendingOperator!!)
            if (result == null) return

            val expressionText = "${formatNumber(firstOperand)} $pendingOperator ${formatNumber(secondOperand)}"
            val resultText = formatNumber(result)

            tvExpression.text = "$expressionText ="
            currentInput = resultText
            tvResult.text = currentInput

            // Hisob-kitobni SQLite ma'lumotlar bazasiga saqlaymiz!
            dbHelper.insert(expressionText, resultText)

            firstOperand = result
            pendingOperator = null
            isNewInput = true
        }
    }

    private fun calculate(op1: Double, op2: Double, op: String): Double? {
        if (op == "÷" && op2 == 0.0) {
            Toast.makeText(this, "0 ga bo‘lish mumkin emas!", Toast.LENGTH_SHORT).show()
            tvResult.text = "Xatolik"
            return null
        }

        return when (op) {
            "+" -> op1 + op2
            "−" -> op1 - op2
            "×" -> op1 * op2
            "÷" -> op1 / op2
            else -> op2
        }
    }

    private fun onClearPressed() {
        firstOperand = null
        pendingOperator = null
        currentInput = ""
        isNewInput = true
        tvExpression.text = ""
        tvResult.text = "0"
    }

    private fun onBackspacePressed() {
        if (!isNewInput && currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
            if (currentInput.isEmpty() || currentInput == "-") {
                currentInput = "0"
                isNewInput = true
            }
            tvResult.text = currentInput
        }
    }

    private fun formatNumber(number: Double?): String {
        if (number == null) return ""
        val decimalFormat = DecimalFormat("#.##########")
        return decimalFormat.format(number)
    }
}

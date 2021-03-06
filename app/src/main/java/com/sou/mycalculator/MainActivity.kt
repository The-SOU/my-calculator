package com.sou.mycalculator

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.room.Room
import com.sou.mycalculator.databinding.ActivityMainBinding
import com.sou.mycalculator.model.History
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var isOperator = false
    private var hasOperator = false

    lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "historyDB"
        ).build()




    }

    fun buttonClicked(v: View) {
        when (v.id) {
            R.id.buttton0 -> numberButtonClicked("0")
            R.id.buttton1 -> numberButtonClicked("1")
            R.id.buttton2 -> numberButtonClicked("2")
            R.id.buttton3 -> numberButtonClicked("3")
            R.id.buttton4 -> numberButtonClicked("4")
            R.id.buttton5 -> numberButtonClicked("5")
            R.id.buttton6 -> numberButtonClicked("6")
            R.id.buttton7 -> numberButtonClicked("7")
            R.id.buttton8 -> numberButtonClicked("8")
            R.id.buttton9 -> numberButtonClicked("9")
            R.id.butttonPlus -> operatorButtonClicked("+")
            R.id.butttonMinus -> operatorButtonClicked("-")
            R.id.butttonMulti -> operatorButtonClicked("X")
            R.id.butttonDivider -> operatorButtonClicked("/")
            R.id.buttonModulo -> operatorButtonClicked("%")
        }
    }

    private fun numberButtonClicked(number: String) {

        if (isOperator) {
            binding.expressionTextView.append(" ")
        }

        isOperator = false

        val expressionText = binding.expressionTextView.text.split(" ")
        if (expressionText.isNotEmpty() && expressionText.last().length >= 15) {
            Toast.makeText(this, "15??????????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show()
            return
        } else if (expressionText.last().isEmpty() && number == "0") {
            Toast.makeText(this, "0??? ?????? ?????? ??? ??? ????????????.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.expressionTextView.append(number)
        binding.resultTextView.text = calculateExpression()
    }

    private fun operatorButtonClicked(operator: String) {
        if (binding.expressionTextView.text.isEmpty()) {
            return
        }

        when {
            //????????? ????????????
            isOperator -> {
                val text = binding.expressionTextView.text.toString()
                binding.expressionTextView.text = text.dropLast(1) + operator
            }
            hasOperator -> {
                Toast.makeText(this, "???????????? ??? ?????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show()
                return
            }
            else -> {
                binding.expressionTextView.append(" $operator")
            }
        }

        val ssb = SpannableStringBuilder(binding.expressionTextView.text)
        ssb.setSpan(
            ForegroundColorSpan(getColor(R.color.green)),
            binding.expressionTextView.text.length - 1,
            binding.expressionTextView.text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.expressionTextView.text = ssb

        isOperator = true
        hasOperator = true

    }

    fun resultButtonClicked(v: View) {
        val expressionTexts = binding.expressionTextView.text.split(" ")

        //????????????
        if (binding.expressionTextView.text.isEmpty() || expressionTexts.size == 1) {
            return
        }

        if(expressionTexts.size != 3 && hasOperator) {
            Toast.makeText(this, "?????? ???????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show()
            return
        }

        if (expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()) {
            Toast.makeText(this, "????????? ??????????????????.", Toast.LENGTH_SHORT).show()
            return
        }

        val expressionText = binding.expressionTextView.text.toString()
        val resultText = calculateExpression()

        //????????? ??????
        Thread(Runnable {
            db.historyDao().insertHistory(History(null, expressionText, resultText))
        }).start()



        binding.resultTextView.text = ""
        binding.expressionTextView.text = resultText

        isOperator = false
        hasOperator = false
    }

    private fun calculateExpression(): String {
        val expressionTexts = binding.expressionTextView.text.split(" ")

        //???????????? ??????
        if (hasOperator.not() || expressionTexts.size != 3) {
            return ""
        } else if (expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()) {
            return ""
        }

        val exp1 = expressionTexts[0].toBigInteger()
        val exp2 = expressionTexts[2].toBigInteger()
        val op = expressionTexts[1]

        return when (op) {
            "+" -> (exp1 + exp2).toString()
            "-" -> (exp1 - exp2).toString()
            "X" -> (exp1 * exp2).toString()
            "/" -> (exp1 / exp2).toString()
            "%" -> (exp1 % exp2).toString()
            else -> ""
        }
    }

    fun historyButtonClicked(v: View) = with(binding) {
        historyLayout.isVisible = true
        //?????? ?????? ??? ??????
        historyLinearLayout.removeAllViews()

        //todo ???????????? ????????? ????????????
        //todo ?????? ????????? ????????????
        Thread(Runnable {
            db.historyDao().getAll().reversed().forEach {
                runOnUiThread {
                    val historyView = LayoutInflater
                        .from(this@MainActivity)
                        .inflate(R.layout.history_row, null, false)
                    historyView.findViewById<TextView>(R.id.expressionTextView).text = it.expression
                    historyView.findViewById<TextView>(R.id.resultTextView).text = "= ${it.result}"

                    historyLinearLayout.addView(historyView)
                }
            }
        }).start()

    }

    fun closeHistoryButtonClicked(v: View) = with(binding) {
        historyLayout.isVisible = false

    }

    fun historyClearButtonClicked(v: View) = with(binding) {
        historyLinearLayout.removeAllViews()

        Thread(Runnable {
            db.historyDao().deleteAll()
        }).start()

        //todo ???????????? ????????????
        //todo ????????? ????????????
    }



    fun clearButtonClicked(v: View) {
        binding.expressionTextView.text = ""
        binding.resultTextView.text = ""
        isOperator = false
        hasOperator = false
    }
}

fun String.isNumber(): Boolean {
    return try {
        this.toBigInteger()
        true
    } catch (e: NumberFormatException) {
        false
    }
}
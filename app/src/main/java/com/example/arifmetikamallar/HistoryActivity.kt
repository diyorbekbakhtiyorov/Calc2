package com.example.arifmetikamallar
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.arifmetikamallar.data.CalculatorDatabaseHelper
import com.example.arifmetikamallar.data.HistoryItem

class HistoryActivity : AppCompatActivity() {

    private lateinit var dbHelper: CalculatorDatabaseHelper
    private lateinit var rvHistory: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var adapter: HistoryAdapter
    private val historyList = mutableListOf<HistoryItem>()

    companion object {
        const val EXTRA_SELECTED_RESULT = "extra_selected_result"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        dbHelper = CalculatorDatabaseHelper(this)

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        val btnClearAll: ImageButton = findViewById(R.id.btnClearAll)
        rvHistory = findViewById(R.id.rvHistory)
        layoutEmpty = findViewById(R.id.layoutEmpty)

        btnBack.setOnClickListener {
            finish()
        }

        btnClearAll.setOnClickListener {
            showClearAllDialog()
        }

        setupRecyclerView()
        loadHistory()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            items = historyList,
            onItemClick = { item ->
                // Foydalanuvchi bosganda natijani qaytarish (agar kerak bo'lsa)
                val intent = Intent().apply {
                    putExtra(EXTRA_SELECTED_RESULT, item.result)
                }
                setResult(RESULT_OK, intent)
                Toast.makeText(this, "${item.result} kalkulyatorga yuklandi", Toast.LENGTH_SHORT).show()
                finish()
            },
            onCopyClick = { item ->
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Calculator Result", "${item.expression} = ${item.result}")
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { item, position ->
                if (dbHelper.deleteById(item.id)) {
                    adapter.removeItem(position)
                    checkEmptyState()
                    Toast.makeText(this, getString(R.string.history_item_deleted), Toast.LENGTH_SHORT).show()
                }
            }
        )

        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = adapter
    }

    private fun loadHistory() {
        val items = dbHelper.getAllHistory()
        historyList.clear()
        historyList.addAll(items)
        adapter.notifyDataSetChanged()
        checkEmptyState()
    }

    private fun checkEmptyState() {
        if (historyList.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvHistory.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvHistory.visibility = View.VISIBLE
        }
    }

    private fun showClearAllDialog() {
        if (historyList.isEmpty()) {
            Toast.makeText(this, getString(R.string.history_empty_title), Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.history_clear_dialog_title))
            .setMessage(getString(R.string.history_clear_dialog_msg))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                dbHelper.deleteAll()
                historyList.clear()
                adapter.notifyDataSetChanged()
                checkEmptyState()
                Toast.makeText(this, getString(R.string.history_all_cleared), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
}

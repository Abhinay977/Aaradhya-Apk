package com.aaradhya.assistant.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aaradhya.assistant.R
import com.aaradhya.assistant.service.AccessibilityHelperService
import com.aaradhya.assistant.viewmodel.MainViewModel
import androidx.activity.viewModels
import org.json.JSONArray
import org.json.JSONObject

class SettingsActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    // ── Views ──────────────────────────────────────────────────────────────────
    private lateinit var apiKeyInput:     EditText
    private lateinit var secondaryApiKeyInput: EditText
    private lateinit var serpstackApiKeyInput: EditText
    private lateinit var userNameInput:   EditText
    private lateinit var modelSpinner:    Spinner
    private lateinit var voiceSpinner:    Spinner
    private lateinit var personalityGF:   RadioButton
    private lateinit var personalityPro:  RadioButton
    private lateinit var personalityAss:  RadioButton
    private lateinit var primeRecycler:   RecyclerView
    private lateinit var addPrimeBtn:     Button
    private lateinit var accessStatus:    TextView
    private lateinit var saveBtn:         Button

    private lateinit var primeAdapter: PrimeContactAdapter
    private val primeContacts = mutableListOf<Pair<String, String>>()

    // ── Model options ──────────────────────────────────────────────────────────
    private val models = listOf(
        Pair("Gemini 2.0 Flash (Fastest, Live)", "models/gemini-2.0-flash"),
        Pair("Gemini 1.5 Flash (Stable)",        "models/gemini-1.5-flash"),
        Pair("Gemini 1.5 Pro (High Quality)",    "models/gemini-1.5-pro")
    )

    // ── Voice options ──────────────────────────────────────────────────────────
    private val voices = listOf(
        "Aoede", "Charon", "Kore", "Fenrir", "Puck", "Leda", "Orus", "Zephyr"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initViews()
        loadSettings()
        setupSpinners()
        setupPrimeContacts()
        setupAccessibilityStatus()
        setupSaveButton()
    }

    override fun onResume() {
        super.onResume()
        updateAccessibilityStatus()
    }

    // ── Init ───────────────────────────────────────────────────────────────────

    private fun initViews() {
        apiKeyInput    = findViewById(R.id.apiKeyInput)
        secondaryApiKeyInput = findViewById(R.id.secondaryApiKeyInput)
        serpstackApiKeyInput = findViewById(R.id.serpstackApiKeyInput)
        userNameInput  = findViewById(R.id.userNameInput)
        modelSpinner   = findViewById(R.id.modelSpinner)
        voiceSpinner   = findViewById(R.id.voiceSpinner)
        personalityGF  = findViewById(R.id.personalityGF)
        personalityPro = findViewById(R.id.personalityPro)
        personalityAss = findViewById(R.id.personalityAss)
        primeRecycler  = findViewById(R.id.primeRecycler)
        addPrimeBtn    = findViewById(R.id.addPrimeBtn)
        accessStatus   = findViewById(R.id.accessibilityStatus)
        saveBtn        = findViewById(R.id.saveBtn)
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("aaradhya_prefs", Context.MODE_PRIVATE)
        apiKeyInput.setText(prefs.getString("api_key", ""))
        secondaryApiKeyInput.setText(prefs.getString("api_key_secondary", ""))
        serpstackApiKeyInput.setText(prefs.getString("serpstack_api_key", ""))
        userNameInput.setText(prefs.getString("user_name", ""))

        when (prefs.getString("personality_mode", "gf")) {
            "professional" -> personalityPro.isChecked = true
            "assistant"    -> personalityAss.isChecked = true
            else           -> personalityGF.isChecked  = true
        }

        primeContacts.addAll(viewModel.getPrimeContacts())
    }

    // ── Spinners ───────────────────────────────────────────────────────────────

    private fun setupSpinners() {
        // Model spinner
        val modelNames = models.map { it.first }
        val modelAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, modelNames).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        modelSpinner.adapter = modelAdapter

        val prefs = getSharedPreferences("aaradhya_prefs", Context.MODE_PRIVATE)
        val savedModel = prefs.getString("gemini_model", models[0].second)
        val modelIdx = models.indexOfFirst { it.second == savedModel }.coerceAtLeast(0)
        modelSpinner.setSelection(modelIdx)

        // Voice spinner
        val voiceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
            voices.map { it + (if (it.contains("Aoede") || it.contains("Kore") || it.contains("Leda") || it.contains("Zephyr")) " (Female)" else " (Male)") }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        voiceSpinner.adapter = voiceAdapter

        val savedVoice = prefs.getString("gemini_voice", "Aoede")
        val voiceIdx = voices.indexOf(savedVoice).coerceAtLeast(0)
        voiceSpinner.setSelection(voiceIdx)
    }

    // ── Prime Contacts ─────────────────────────────────────────────────────────

    private fun setupPrimeContacts() {
        primeAdapter = PrimeContactAdapter(primeContacts) { idx ->
            primeContacts.removeAt(idx)
            primeAdapter.notifyItemRemoved(idx)
        }
        primeRecycler.adapter = primeAdapter
        primeRecycler.layoutManager = LinearLayoutManager(this)

        addPrimeBtn.setOnClickListener { showAddContactDialog() }
    }

    private fun showAddContactDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_prime_contact, null)
        val nameInput   = dialogView.findViewById<EditText>(R.id.dialogNameInput)
        val numberInput = dialogView.findViewById<EditText>(R.id.dialogNumberInput)

        AlertDialog.Builder(this)
            .setTitle("Prime Contact Add Karo")
            .setView(dialogView)
            .setPositiveButton("ADD") { _, _ ->
                val name   = nameInput.text.toString().trim()
                val number = numberInput.text.toString().trim()
                if (name.isNotEmpty() && number.isNotEmpty()) {
                    primeContacts.add(Pair(name, number))
                    primeAdapter.notifyItemInserted(primeContacts.size - 1)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Accessibility ──────────────────────────────────────────────────────────

    private fun setupAccessibilityStatus() {
        accessStatus.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        updateAccessibilityStatus()
    }

    private fun updateAccessibilityStatus() {
        val enabled = AccessibilityHelperService.isEnabled(this)
        accessStatus.text = if (enabled) "✅ Accessibility: ON (tap to manage)" else "❌ Accessibility: OFF (tap to enable)"
        accessStatus.setTextColor(if (enabled) android.graphics.Color.parseColor("#00E676") else android.graphics.Color.parseColor("#FF1744"))
    }

    // ── Save ───────────────────────────────────────────────────────────────────

    private fun setupSaveButton() {
        saveBtn.setOnClickListener {
            val prefs = getSharedPreferences("aaradhya_prefs", Context.MODE_PRIVATE)
            val personality = when {
                personalityPro.isChecked -> "professional"
                personalityAss.isChecked -> "assistant"
                else -> "gf"
            }

            prefs.edit()
                .putString("api_key",         apiKeyInput.text.toString().trim())
                .putString("api_key_secondary", secondaryApiKeyInput.text.toString().trim())
                .putString("serpstack_api_key", serpstackApiKeyInput.text.toString().trim())
                .putString("user_name",       userNameInput.text.toString().trim())
                .putString("personality_mode", personality)
                .putString("gemini_model",    models[modelSpinner.selectedItemPosition].second)
                .putString("gemini_voice",    voices[voiceSpinner.selectedItemPosition])
                .apply()

            viewModel.savePrimeContacts(primeContacts)

            Toast.makeText(this, "Saved! Restart the app to apply changes. ✅", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PrimeContactAdapter
// ══════════════════════════════════════════════════════════════════════════════

class PrimeContactAdapter(
    private val contacts: MutableList<Pair<String, String>>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<PrimeContactAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText:   TextView    = view.findViewById(R.id.primeItemName)
        val numberText: TextView    = view.findViewById(R.id.primeItemNumber)
        val deleteBtn:  ImageButton = view.findViewById(R.id.primeItemDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_prime_contact, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (name, number) = contacts[position]
        holder.nameText.text   = name
        holder.numberText.text = number
        holder.deleteBtn.setOnClickListener { onDelete(holder.adapterPosition) }
    }

    override fun getItemCount() = contacts.size
}

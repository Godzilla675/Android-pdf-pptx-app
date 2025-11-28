package com.officesuite.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.officesuite.app.R
import com.officesuite.app.data.repository.FontSize
import com.officesuite.app.data.repository.PreferencesRepository
import com.officesuite.app.databinding.FragmentSettingsBinding
import com.officesuite.app.utils.AccessibilityManager
import com.officesuite.app.utils.FocusModeManager

/**
 * Settings Fragment for managing app preferences.
 * Implements Nice-to-Have Feature #14: Customization & Themes
 * Updated to include Medium Priority Features: Accessibility, Focus Mode, Word Count Goals
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var prefsRepository: PreferencesRepository
    private lateinit var accessibilityManager: AccessibilityManager
    private lateinit var focusModeManager: FocusModeManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        prefsRepository = PreferencesRepository(requireContext())
        accessibilityManager = AccessibilityManager(requireContext())
        focusModeManager = FocusModeManager(requireContext())
        
        setupToolbar()
        loadCurrentSettings()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                @Suppress("DEPRECATION")
                requireActivity().onBackPressed()
            }
            title = getString(R.string.settings)
        }
    }

    private fun loadCurrentSettings() {
        // Dark mode
        binding.switchDarkMode.isChecked = prefsRepository.isDarkMode
        
        // Reading mode
        binding.switchReadingMode.isChecked = prefsRepository.isReadingMode
        
        // Auto-save
        binding.switchAutoSave.isChecked = prefsRepository.isAutoSaveEnabled
        
        // Font size
        updateFontSizeSelection(prefsRepository.fontSize)
        
        // Accessibility settings
        binding.switchDyslexiaFont.isChecked = accessibilityManager.isDyslexiaFontEnabled
        binding.switchReadingRuler.isChecked = accessibilityManager.isReadingRulerEnabled
        binding.switchHighContrast.isChecked = accessibilityManager.isHighContrastEnabled
        
        // Focus mode & Goals
        binding.switchFocusMode.isChecked = focusModeManager.isFocusModeEnabled
        updateDailyGoalText()
    }

    private fun setupListeners() {
        // Dark mode toggle
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefsRepository.isDarkMode = isChecked
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        
        // Reading mode toggle
        binding.switchReadingMode.setOnCheckedChangeListener { _, isChecked ->
            prefsRepository.isReadingMode = isChecked
        }
        
        // Auto-save toggle
        binding.switchAutoSave.setOnCheckedChangeListener { _, isChecked ->
            prefsRepository.isAutoSaveEnabled = isChecked
        }
        
        // Font size buttons
        binding.btnFontSmall.setOnClickListener {
            setFontSize(FontSize.SMALL)
        }
        binding.btnFontMedium.setOnClickListener {
            setFontSize(FontSize.MEDIUM)
        }
        binding.btnFontLarge.setOnClickListener {
            setFontSize(FontSize.LARGE)
        }
        binding.btnFontExtraLarge.setOnClickListener {
            setFontSize(FontSize.EXTRA_LARGE)
        }
        
        // Clear recent files
        binding.btnClearRecent.setOnClickListener {
            prefsRepository.clearRecentFiles()
            android.widget.Toast.makeText(context, "Recent files cleared", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        // Accessibility settings
        binding.switchDyslexiaFont.setOnCheckedChangeListener { _, isChecked ->
            accessibilityManager.isDyslexiaFontEnabled = isChecked
            android.widget.Toast.makeText(
                context,
                if (isChecked) "Dyslexia font enabled" else "Dyslexia font disabled",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        
        binding.switchReadingRuler.setOnCheckedChangeListener { _, isChecked ->
            accessibilityManager.isReadingRulerEnabled = isChecked
            android.widget.Toast.makeText(
                context,
                if (isChecked) "Reading ruler enabled" else "Reading ruler disabled",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        
        binding.switchHighContrast.setOnCheckedChangeListener { _, isChecked ->
            accessibilityManager.isHighContrastEnabled = isChecked
        }
        
        // Focus mode toggle
        binding.switchFocusMode.setOnCheckedChangeListener { _, isChecked ->
            focusModeManager.isFocusModeEnabled = isChecked
            android.widget.Toast.makeText(
                context,
                if (isChecked) getString(R.string.focus_mode_enabled) else getString(R.string.focus_mode_disabled),
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
        
        // Daily goal button
        binding.btnSetDailyGoal.setOnClickListener {
            showSetDailyGoalDialog()
        }
    }
    
    private fun showSetDailyGoalDialog() {
        val editText = EditText(requireContext()).apply {
            hint = "Enter word count goal"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            val currentGoal = focusModeManager.dailyWordGoal
            if (currentGoal > 0) {
                setText(currentGoal.toString())
            }
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.daily_goal)
            .setView(editText)
            .setPositiveButton(R.string.save) { _, _ ->
                val goalText = editText.text.toString()
                val goal = goalText.toIntOrNull() ?: 0
                focusModeManager.dailyWordGoal = goal
                updateDailyGoalText()
                android.widget.Toast.makeText(
                    context,
                    if (goal > 0) "Daily goal set to $goal words" else "Daily goal cleared",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton("Clear") { _, _ ->
                focusModeManager.dailyWordGoal = 0
                updateDailyGoalText()
            }
            .show()
    }
    
    private fun updateDailyGoalText() {
        val goal = focusModeManager.dailyWordGoal
        binding.textDailyGoal.text = if (goal > 0) {
            val progress = focusModeManager.getDailyGoalProgress()
            "${progress.currentWords} / $goal words (${progress.percentComplete.toInt()}%)"
        } else {
            "Not set"
        }
    }

    private fun setFontSize(size: FontSize) {
        prefsRepository.fontSize = size
        updateFontSizeSelection(size)
    }

    private fun updateFontSizeSelection(selectedSize: FontSize) {
        val buttons = listOf(
            binding.btnFontSmall to FontSize.SMALL,
            binding.btnFontMedium to FontSize.MEDIUM,
            binding.btnFontLarge to FontSize.LARGE,
            binding.btnFontExtraLarge to FontSize.EXTRA_LARGE
        )
        
        buttons.forEach { (button, size) ->
            button.isSelected = size == selectedSize
            button.alpha = if (size == selectedSize) 1f else 0.6f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

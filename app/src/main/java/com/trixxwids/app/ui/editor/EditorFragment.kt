package com.trixxwids.app.ui.editor

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.trixxwids.app.R
import com.trixxwids.app.data.ElementType
import com.trixxwids.app.data.ShapeType
import com.trixxwids.app.data.WidgetConfig
import com.trixxwids.app.data.WidgetElement
import com.trixxwids.app.databinding.FragmentEditorBinding
import com.trixxwids.app.sensor.GyroEffectManager
import com.trixxwids.app.util.BitmapUtil
import com.trixxwids.app.util.WidgetSizeUtil
import com.trixxwids.app.viewmodel.EditorViewModel

class EditorFragment : Fragment() {

    private var _binding: FragmentEditorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditorViewModel by activityViewModels()
    private lateinit var gyroManager: GyroEffectManager

    private var currentSaveName: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val imagePath = uri.toString()
                viewModel.addElement(
                    WidgetElement(
                        type = ElementType.IMAGE,
                        width = 200f,
                        height = 200f,
                        x = 100f,
                        y = 100f,
                        imageUri = imagePath
                    )
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gyroManager = GyroEffectManager(requireContext())

        setupCanvas()
        setupSizeChips()
        setupAddButtons()
        setupPropertiesPanel()
        setupFabButtons()
        setupObservers()
    }

    fun loadWidgetConfig(config: WidgetConfig) {
        viewModel.widgetConfig.value = config

        val sizeTagIndex = when (config.widgetSizeTag) {
            "2x1" -> 0
            "2x2" -> 1
            "4x1" -> 2
            "4x2" -> 3
            "4x4" -> 4
            else -> 3
        }
        val chip = binding.sizeChipGroup.getChildAt(sizeTagIndex)
        if (chip is com.google.android.material.chip.Chip) {
            chip.isChecked = true
        }

        binding.canvasView.widgetConfig = config
        binding.canvasView.invalidate()
    }

    private fun setupCanvas() {
        binding.canvasView.onElementSelected = { element ->
            viewModel.selectElement(element)
        }
        binding.canvasView.onElementMoved = { element ->
            viewModel.updateElementProperty { element }
        }
        binding.canvasView.onElementResized = { element ->
            viewModel.updateElementProperty { element }
        }
    }

    private fun setupSizeChips() {
        binding.sizeChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val tag = when (checkedIds[0]) {
                    R.id.size_2x1 -> "2x1"
                    R.id.size_2x2 -> "2x2"
                    R.id.size_4x1 -> "4x1"
                    R.id.size_4x2 -> "4x2"
                    R.id.size_4x4 -> "4x4"
                    else -> "4x2"
                }
                viewModel.setWidgetSize(tag)
                binding.canvasView.widgetConfig = viewModel.widgetConfig.value
                binding.canvasView.invalidate()
            }
        }
        binding.sizeChipGroup.check(R.id.size_4x2)
    }

    private fun setupAddButtons() {
        binding.btnAddText.setOnClickListener {
            viewModel.addElement(
                WidgetElement(
                    type = ElementType.TEXT,
                    text = "Sample Text",
                    x = 50f, y = 50f, width = 300f, height = 80f,
                    fontSize = 32f, fontColor = Color.BLACK
                )
            )
        }
        binding.btnAddClock.setOnClickListener {
            viewModel.addElement(
                WidgetElement(
                    type = ElementType.CLOCK,
                    text = "12:30",
                    x = 50f, y = 50f, width = 250f, height = 120f,
                    fontSize = 56f, fontColor = Color.BLACK, fontType = android.graphics.Typeface.BOLD
                )
            )
        }
        binding.btnAddDate.setOnClickListener {
            viewModel.addElement(
                WidgetElement(
                    type = ElementType.DATE,
                    text = "Mon, Jan 1",
                    x = 50f, y = 50f, width = 300f, height = 80f,
                    fontSize = 28f, fontColor = Color.GRAY
                )
            )
        }
        binding.btnAddWeather.setOnClickListener {
            viewModel.addElement(
                WidgetElement(
                    type = ElementType.WEATHER,
                    text = "72°F",
                    x = 50f, y = 50f, width = 200f, height = 80f,
                    fontSize = 28f, fontColor = Color.BLACK
                )
            )
        }
        binding.btnAddShape.setOnClickListener {
            val shapes = arrayOf("Rectangle", "Circle")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Shape")
                .setItems(shapes) { _, which ->
                    viewModel.addElement(
                        WidgetElement(
                            type = ElementType.SHAPE,
                            shapeType = if (which == 0) ShapeType.RECTANGLE else ShapeType.CIRCLE,
                            x = 50f, y = 50f, width = 200f, height = 200f,
                            backgroundColor = Color.parseColor("#FFE0E0E0"),
                            borderRadius = if (which == 0) 16f else 0f
                        )
                    )
                }
                .show()
        }
        binding.btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            imagePickerLauncher.launch(intent)
        }
    }

    private fun setupPropertiesPanel() {
        binding.btnDeleteElement.setOnClickListener {
            viewModel.removeSelectedElement()
            binding.propertiesPanel.visibility = View.GONE
        }

        binding.sliderFontSize.addOnChangeListener { _, value, _ ->
            viewModel.updateElementProperty { it.copy(fontSize = value) }
        }

        binding.sliderOpacity.addOnChangeListener { _, value, _ ->
            viewModel.updateElementProperty { it.copy(opacity = value.toInt()) }
        }

        binding.sliderBorderRadius.addOnChangeListener { _, value, _ ->
            viewModel.updateElementProperty { it.copy(borderRadius = value) }
        }

        binding.sliderBorderWidth.addOnChangeListener { _, value, _ ->
            viewModel.updateElementProperty { it.copy(borderWidth = value) }
        }

        binding.btnFontColor.setOnClickListener {
            showColorPicker { color ->
                viewModel.updateElementProperty { it.copy(fontColor = color) }
            }
        }

        binding.btnBorderColor.setOnClickListener {
            showColorPicker { color ->
                viewModel.updateElementProperty { it.copy(borderColor = color) }
            }
        }

        binding.btnBgColor.setOnClickListener {
            showColorPicker { color ->
                viewModel.updateElementProperty { it.copy(backgroundColor = color) }
            }
        }
    }

    private fun showColorPicker(onColorSelected: (Int) -> Unit) {
        val colors = listOf(
            Color.BLACK, Color.WHITE, Color.RED, Color.GREEN, Color.BLUE,
            Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.GRAY, Color.DKGRAY,
            Color.parseColor("#FF007AFF"), Color.parseColor("#FF34C759"),
            Color.parseColor("#FFFF3B30"), Color.parseColor("#FFFF9500"),
            Color.parseColor("#FF5E5CE6"), Color.parseColor("#FFFFD60A")
        )
        val colorNames = arrayOf(
            "Black", "White", "Red", "Green", "Blue",
            "Yellow", "Cyan", "Magenta", "Gray", "Dark Gray",
            "Blue (iOS)", "Green (iOS)", "Red (iOS)", "Orange (iOS)",
            "Purple (iOS)", "Yellow (iOS)"
        )

        val items = colorNames.zip(colors).map { (name, _) -> name }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Pick a Color")
            .setItems(items) { _, which ->
                onColorSelected(colors[which])
            }
            .show()
    }

    private fun setupFabButtons() {
        binding.fabSave.setOnClickListener {
            showSaveDialog()
        }

        binding.fabGyro.setOnClickListener {
            if (gyroManager.checkAvailability()) {
                val isNowEnabled = gyroManager.toggle()
                viewModel.isGyroEnabled.value = isNowEnabled
                if (isNowEnabled) {
                    gyroManager.setTargetView(binding.canvasContainer)
                    Snackbar.make(binding.root, "Gyro effect enabled", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root, "Gyro effect disabled", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Snackbar.make(binding.root, "Gyroscope not available on this device", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun showSaveDialog() {
        val input = android.widget.EditText(requireContext()).apply {
            hint = "Widget name"
            setText(currentSaveName ?: "")
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Save Widget")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isEmpty()) {
                    Snackbar.make(binding.root, "Please enter a widget name", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                currentSaveName = name
                val config = viewModel.widgetConfig.value ?: return@setPositiveButton
                val thumbnail = BitmapUtil.renderWidgetToBitmap(
                    config,
                    400,
                    400 * WidgetSizeUtil.getAspectRatio(config.widgetSizeTag).toInt().coerceAtLeast(1)
                )
                viewModel.saveWidget(name, thumbnail)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupObservers() {
        viewModel.widgetConfig.observe(viewLifecycleOwner) { config ->
            binding.canvasView.widgetConfig = config
            binding.canvasView.invalidate()
        }

        viewModel.selectedElement.observe(viewLifecycleOwner) { element ->
            if (element != null) {
                binding.propertiesPanel.visibility = View.VISIBLE
                binding.sliderFontSize.value = element.fontSize
                binding.sliderOpacity.value = element.opacity.toFloat()
                binding.sliderBorderRadius.value = element.borderRadius
                binding.sliderBorderWidth.value = element.borderWidth
            } else {
                binding.propertiesPanel.visibility = View.GONE
            }
            binding.canvasView.selectedElement = element
            binding.canvasView.invalidate()
        }

        viewModel.widgetToLoad.observe(viewLifecycleOwner) { config ->
            config?.let {
                loadWidgetConfig(it)
                viewModel.widgetToLoad.value = null
            }
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearSnackbar()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gyroManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        gyroManager.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gyroManager.onDestroy()
        _binding = null
    }

    companion object {
        fun newInstance(widgetId: Long = -1): EditorFragment {
            val args = Bundle().apply {
                putLong("widgetId", widgetId)
            }
            val fragment = EditorFragment()
            fragment.arguments = args
            return fragment
        }
    }
}

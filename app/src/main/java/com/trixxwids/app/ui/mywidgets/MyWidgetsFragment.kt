package com.trixxwids.app.ui.mywidgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.trixxwids.app.R
import com.trixxwids.app.data.WidgetEntity
import com.trixxwids.app.databinding.FragmentMyWidgetsBinding
import com.trixxwids.app.util.WidgetSizeUtil
import com.trixxwids.app.viewmodel.MyWidgetsViewModel
import kotlinx.coroutines.launch

class MyWidgetsFragment : Fragment() {

    private var _binding: FragmentMyWidgetsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyWidgetsViewModel by viewModels()

    private lateinit var adapter: WidgetAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyWidgetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = WidgetAdapter(
            onEdit = { widget -> viewModel.editWidget(widget) },
            onDuplicate = { widget -> viewModel.duplicateWidget(widget.id) },
            onDelete = { widget -> confirmDelete(widget) },
            onApplyToHome = { widget -> applyToHomeScreen(widget) }
        )

        binding.recyclerWidgets.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerWidgets.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.widgets.collect { widgets ->
                    adapter.submitList(widgets)
                    binding.emptyState.visibility = if (widgets.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        viewModel.navigateToEditor.observe(viewLifecycleOwner) { widget ->
            widget?.let {
                val parent = requireActivity() as? com.trixxwids.app.ui.MainActivity
                parent?.navigateToEditorWithWidget(it)
                viewModel.onNavigated()
            }
        }

        viewModel.snackbarMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearSnackbar()
            }
        }
    }

    private fun confirmDelete(widget: WidgetEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Widget")
            .setMessage("Are you sure you want to delete \"${widget.name}\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteWidget(widget.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyToHomeScreen(widget: WidgetEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = requireContext().getSystemService(AppWidgetManager::class.java)
            val componentName = ComponentName(requireContext(), getWidgetProviderClass(widget.sizeTag))
            if (manager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (manager.isRequestPinAppWidgetSupported) {
                    val pinnedWidgetCallback = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_PICK
                    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, componentName)
                    }
                    manager.requestPinAppWidget(componentName, null, null)
                    Snackbar.make(binding.root, "Follow system prompts to place widget", Snackbar.LENGTH_LONG).show()
                } else {
                    Snackbar.make(binding.root, "Pinning not supported on this device", Snackbar.LENGTH_LONG).show()
                }
            }
        } else {
            Snackbar.make(binding.root, "Widget pinning requires Android 8.0+", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun getWidgetProviderClass(sizeTag: String): Class<*> {
        return when (sizeTag) {
            "2x1" -> com.trixxwids.app.widget.TrixxWidgetProvider2x1::class.java
            "2x2" -> com.trixxwids.app.widget.TrixxWidgetProvider2x2::class.java
            "4x1" -> com.trixxwids.app.widget.TrixxWidgetProvider4x1::class.java
            "4x2" -> com.trixxwids.app.widget.TrixxWidgetProvider4x2::class.java
            "4x4" -> com.trixxwids.app.widget.TrixxWidgetProvider4x4::class.java
            else -> com.trixxwids.app.widget.TrixxWidgetProvider::class.java
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

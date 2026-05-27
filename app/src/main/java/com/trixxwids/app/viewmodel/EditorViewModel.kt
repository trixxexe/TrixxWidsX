package com.trixxwids.app.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.trixxwids.app.data.WidgetConfig
import com.trixxwids.app.data.WidgetElement
import com.trixxwids.app.data.WidgetEntity
import com.trixxwids.app.data.WidgetRepository
import com.trixxwids.app.util.BitmapUtil
import kotlinx.coroutines.launch

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WidgetRepository(application)
    private val gson = Gson()

    val widgetConfig = MutableLiveData(WidgetConfig())
    val selectedElement = MutableLiveData<WidgetElement?>()

    private val _savedWidgetId = MutableLiveData<Long?>()
    val savedWidgetId: LiveData<Long?> = _savedWidgetId

    val isGyroEnabled = MutableLiveData(false)

    val widgetToLoad = MutableLiveData<WidgetConfig?>()

    private val _snackbarMessage = MutableLiveData<String?>()
    val snackbarMessage: LiveData<String?> = _snackbarMessage

    fun selectElement(element: WidgetElement?) {
        selectedElement.value = element
    }

    fun updateElementProperty(block: (WidgetElement) -> WidgetElement) {
        val current = selectedElement.value ?: return
        val updated = block(current)
        val config = widgetConfig.value ?: return
        val index = config.elements.indexOfFirst { it.id == current.id }
        if (index >= 0) {
            config.elements[index] = updated
            widgetConfig.value = config
            selectedElement.value = updated
        }
    }

    fun addElement(element: WidgetElement) {
        val config = widgetConfig.value ?: return
        config.elements.add(element)
        widgetConfig.value = config
        selectElement(element)
    }

    fun removeSelectedElement() {
        val current = selectedElement.value ?: return
        val config = widgetConfig.value ?: return
        config.elements.removeAll { it.id == current.id }
        widgetConfig.value = config
        selectElement(null)
    }

    fun setWidgetSize(sizeTag: String) {
        val config = widgetConfig.value ?: return
        config.widgetSizeTag = sizeTag
        widgetConfig.value = config
    }

    fun saveWidget(name: String, thumbnail: Bitmap?) {
        val config = widgetConfig.value ?: return
        val json = gson.toJson(config)
        viewModelScope.launch {
            val id = repository.saveWidget(
                name = name,
                configJson = json,
                sizeTag = config.widgetSizeTag,
                thumbnail = thumbnail
            )
            _savedWidgetId.value = id
            _snackbarMessage.value = "Widget saved successfully"
        }
    }

    fun loadWidget(widgetEntity: WidgetEntity) {
        try {
            val config = gson.fromJson(widgetEntity.configJson, WidgetConfig::class.java)
            widgetConfig.value = config
            _savedWidgetId.value = widgetEntity.id
        } catch (e: Exception) {
            _snackbarMessage.value = "Failed to load widget"
        }
    }

    fun resetForNew() {
        widgetConfig.value = WidgetConfig()
        selectedElement.value = null
        _savedWidgetId.value = null
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}

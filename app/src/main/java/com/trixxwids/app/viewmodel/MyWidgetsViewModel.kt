package com.trixxwids.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.trixxwids.app.data.WidgetEntity
import com.trixxwids.app.data.WidgetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyWidgetsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WidgetRepository(application)

    val widgets: StateFlow<List<WidgetEntity>> = repository.getAllWidgets()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _navigateToEditor = MutableLiveData<WidgetEntity?>()
    val navigateToEditor: LiveData<WidgetEntity?> = _navigateToEditor

    private val _snackbarMessage = MutableLiveData<String?>()
    val snackbarMessage: LiveData<String?> = _snackbarMessage

    fun deleteWidget(id: Long) {
        viewModelScope.launch {
            repository.deleteWidget(id)
            _snackbarMessage.value = "Widget deleted"
        }
    }

    fun duplicateWidget(id: Long) {
        viewModelScope.launch {
            repository.duplicateWidget(id)
            _snackbarMessage.value = "Widget duplicated"
        }
    }

    fun editWidget(widget: WidgetEntity) {
        _navigateToEditor.value = widget
    }

    fun onNavigated() {
        _navigateToEditor.value = null
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}

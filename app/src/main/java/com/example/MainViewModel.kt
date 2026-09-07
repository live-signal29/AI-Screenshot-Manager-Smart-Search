package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CategoryStat
import com.example.data.local.ScreenshotEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.ScanProgress
import com.example.data.repository.ScreenshotRepository
import com.example.domain.ai.AiInsight
import com.example.domain.duplicate.DuplicateGroup
import com.example.domain.search.SearchIntent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val intent: SearchIntent? = null,
    val results: List<Pair<ScreenshotEntity, Int>> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScreenshotRepository(application)
    private val preferences = UserPreferencesRepository(application)

    val allScreenshots: StateFlow<List<ScreenshotEntity>> = repository.allScreenshotsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<ScreenshotEntity>> = repository.favoritesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val duplicates: StateFlow<List<ScreenshotEntity>> = repository.duplicatesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCount: StateFlow<Int> = repository.totalCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalStorageBytes: StateFlow<Long> = repository.totalStorageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val duplicateCount: StateFlow<Int> = repository.duplicateCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val duplicateStorageBytes: StateFlow<Long> = repository.duplicateStorageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val categoryStats: StateFlow<List<CategoryStat>> = repository.categoryStatsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val largeScreenshots: StateFlow<List<ScreenshotEntity>> = repository.largeScreenshotsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val removableScreenshots: StateFlow<List<ScreenshotEntity>> = repository.removableScreenshotsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isOnboardingCompleted: StateFlow<Boolean> = preferences.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val themeMode: StateFlow<String> = preferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val isAutoScanEnabled: StateFlow<Boolean> = preferences.isAutoScanEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isOcrEnabled: StateFlow<Boolean> = preferences.isOcrEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isAiEnabled: StateFlow<Boolean> = preferences.isAiEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val aiApiKey: StateFlow<String> = preferences.aiApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val aiApiEndpoint: StateFlow<String> = preferences.aiApiEndpoint
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _scanProgress = MutableStateFlow(ScanProgress())
    val scanProgress: StateFlow<ScanProgress> = _scanProgress.asStateFlow()

    private val _duplicateClusters = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val duplicateClusters: StateFlow<List<DuplicateGroup>> = _duplicateClusters.asStateFlow()

    private val _searchUiState = MutableStateFlow(SearchUiState())
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    init {
        // Initial quick load and duplicate check
        viewModelScope.launch {
            refreshDuplicateClusters()
        }
    }

    fun startScan() {
        if (_scanProgress.value.isScanning) return
        viewModelScope.launch {
            repository.scanMediaLibrary { progress ->
                _scanProgress.value = progress
            }
            refreshDuplicateClusters()
        }
    }

    fun refreshDuplicateClusters() {
        viewModelScope.launch {
            _duplicateClusters.value = repository.getDuplicateClusters()
        }
    }

    fun toggleFavorite(id: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.setFavorite(id, !currentStatus)
        }
    }

    fun updateCategory(id: Long, category: String) {
        viewModelScope.launch {
            repository.updateCategory(id, category)
        }
    }

    fun deleteScreenshot(id: Long) {
        viewModelScope.launch {
            repository.deleteScreenshot(id)
            refreshDuplicateClusters()
        }
    }

    fun deleteScreenshots(ids: List<Long>) {
        viewModelScope.launch {
            repository.deleteScreenshots(ids)
            refreshDuplicateClusters()
        }
    }

    fun searchNaturalLanguage(query: String) {
        if (query.isBlank()) {
            _searchUiState.value = SearchUiState()
            return
        }
        viewModelScope.launch {
            _searchUiState.value = _searchUiState.value.copy(query = query, isSearching = true)
            val (intent, results) = repository.searchNaturalLanguage(query)
            _searchUiState.value = SearchUiState(
                query = query,
                isSearching = false,
                intent = intent,
                results = results
            )
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferences.setOnboardingCompleted(true)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferences.setThemeMode(mode)
        }
    }

    fun setAutoScan(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAutoScanEnabled(enabled)
        }
    }

    fun setOcr(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setOcrEnabled(enabled)
        }
    }

    fun setAiEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAiEnabled(enabled)
        }
    }

    fun setAiApiKey(key: String) {
        viewModelScope.launch {
            preferences.setAiApiKey(key)
        }
    }

    fun setAiApiEndpoint(endpoint: String) {
        viewModelScope.launch {
            preferences.setAiApiEndpoint(endpoint)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            refreshDuplicateClusters()
        }
    }

    suspend fun getAiInsight(screenshot: ScreenshotEntity): AiInsight {
        return repository.getAiInsight(screenshot)
    }

    suspend fun askAiQuestion(screenshot: ScreenshotEntity, question: String): String {
        return repository.askAiQuestion(screenshot, question)
    }
}

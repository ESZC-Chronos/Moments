package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Moment
import com.example.data.MomentsDatabase
import com.example.data.MomentsRepository
import com.example.data.Quest
import com.example.data.QuestPack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MomentsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: MomentsRepository
    private val prefs = application.getSharedPreferences("moments_prefs", Context.MODE_PRIVATE)

    private val _availablePacks = MutableStateFlow<List<String>>(listOf())
    val availablePacks: StateFlow<List<String>> = _availablePacks.asStateFlow()

    private val _installedPacks = MutableStateFlow<List<String>>(getSavedPacks())
    val installedPacks: StateFlow<List<String>> = _installedPacks.asStateFlow()

    private val _currentPack = MutableStateFlow<QuestPack?>(null)
    val currentPack: StateFlow<QuestPack?> = _currentPack.asStateFlow()
    
    private val _randomQuest = MutableStateFlow<Pair<Quest, String>?>(null)
    val randomQuest: StateFlow<Pair<Quest, String>?> = _randomQuest.asStateFlow()
    
    private val _dailyQuest = MutableStateFlow<Pair<Quest, String>?>(null)
    val dailyQuest: StateFlow<Pair<Quest, String>?> = _dailyQuest.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _useServer = MutableStateFlow(prefs.getBoolean("use_server", false))
    val useServer: StateFlow<Boolean> = _useServer.asStateFlow()

    fun toggleUseServer(use: Boolean) {
        prefs.edit().putBoolean("use_server", use).apply()
        _useServer.value = use
        fetchAvailablePacks()
    }

    private val _serverUrl = MutableStateFlow(prefs.getString("server_url", "https://moments.evah-tec.de/") ?: "https://moments.evah-tec.de/")
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()
    
    private val _username = MutableStateFlow(prefs.getString("username", "Traveler") ?: "Traveler")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _currentStreak = MutableStateFlow(prefs.getInt("current_streak", 0))
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()
    
    private val _showStreakAnimation = MutableStateFlow(false)
    val showStreakAnimation: StateFlow<Boolean> = _showStreakAnimation.asStateFlow()
    
    fun dismissStreakAnimation() {
        _showStreakAnimation.value = false
    }

    fun updateUsername(name: String) {
        prefs.edit().putString("username", name).apply()
        _username.value = name
    }

    fun createCustomPack(name: String, quests: List<Quest>) {
        val packId = "custom_${name.lowercase().replace(Regex("[^a-z0-9]"), "_")}_${System.currentTimeMillis()}"
        val pack = QuestPack(packName = name, version = 1, quests = quests)
        repository.saveCustomPack(packId, pack)
        installPack(packId)
        fetchAvailablePacks()
    }

    init {
        val database = MomentsDatabase.getDatabase(application)
        repository = MomentsRepository(application, database.momentsDao())
        fetchAvailablePacks()
        viewModelScope.launch {
            repository.allMoments.collect {
                fetchDailyQuest()
            }
        }
    }

    fun fetchDailyQuest() {
        viewModelScope.launch {
            val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val savedDate = prefs.getString("daily_quest_date", "")
            val savedPack = prefs.getString("daily_quest_pack", "")
            val savedQuestId = prefs.getString("daily_quest_id", "")
            
            val completedIds = allMoments.value.map { it.questId }.toSet()
            
            if (savedDate == todayDate && !savedPack.isNullOrEmpty() && !savedQuestId.isNullOrEmpty()) {
                if (!completedIds.contains(savedQuestId)) {
                    val pack = repository.getQuestPack(savedPack, if (_useServer.value) _serverUrl.value else null)
                    val quest = pack?.quests?.find { it.id == savedQuestId }
                    if (quest != null) {
                        _dailyQuest.value = Pair(quest, savedPack)
                        return@launch
                    }
                }
            }
            
            val skips = prefs.getStringSet("quest_skip_history", setOf()) ?: setOf()
            
            // Roll new daily quest
            var allQuests = mutableListOf<Pair<Quest, String>>()
            for (packName in _installedPacks.value) {
                val pack = repository.getQuestPack(packName, if (_useServer.value) _serverUrl.value else null)
                if (pack != null) {
                    for (quest in pack.quests) {
                        if (!completedIds.contains(quest.id)) {
                            allQuests.add(Pair(quest, packName))
                        }
                    }
                }
            }
            
            val withoutSkips = allQuests.filter { !skips.contains(it.first.id) }
            if (withoutSkips.isNotEmpty()) {
                allQuests = withoutSkips.toMutableList()
            }

            if (allQuests.isNotEmpty()) {
                val selected = allQuests.random()
                prefs.edit()
                    .putString("daily_quest_date", todayDate)
                    .putString("daily_quest_pack", selected.second)
                    .putString("daily_quest_id", selected.first.id)
                    .putString("widget_quest_text", selected.first.title)
                    .apply()
                _dailyQuest.value = selected
                
                // Update widget
                val intent = android.content.Intent(getApplication(), com.example.widget.DailyQuestWidget::class.java)
                intent.action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = android.appwidget.AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(
                    android.content.ComponentName(getApplication(), com.example.widget.DailyQuestWidget::class.java)
                )
                intent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                getApplication<android.app.Application>().sendBroadcast(intent)
            } else {
                _dailyQuest.value = null
            }
        }
    }

    fun forceNewDailyQuest() {
        val currentId = _dailyQuest.value?.first?.id
        if (currentId != null) {
            val history = prefs.getStringSet("quest_skip_history", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            history.add(currentId)
            prefs.edit().putStringSet("quest_skip_history", history).apply()
        }
        prefs.edit().remove("daily_quest_date").apply()
        fetchDailyQuest()
    }

    fun fetchAvailablePacks() {
        viewModelScope.launch {
            val packs = repository.getAvailablePacks(if (_useServer.value) _serverUrl.value else null)
            val finalPacks = if (packs.isNotEmpty()) packs else listOf("daily", "mindfulness", "walking", "couple", "urban_explore", "mega_pack")
            _availablePacks.value = finalPacks
            
            val currentInstalled = getSavedPacks()
            val validInstalled = currentInstalled.filter { finalPacks.contains(it) }
            _installedPacks.value = if (validInstalled.isNotEmpty()) validInstalled else listOf("daily")
        }
    }

    private fun getSavedPacks(): List<String> {
        return prefs.getStringSet("installed_packs", setOf("daily", "mindfulness"))?.toList() ?: listOf("daily", "mindfulness")
    }

    fun installPack(packName: String) {
        val updated = getSavedPacks().toMutableSet().apply { add(packName) }
        prefs.edit().putStringSet("installed_packs", updated).apply()
        _installedPacks.value = updated.toList()
    }

    fun removePack(packName: String) {
        val updated = getSavedPacks().toMutableSet().apply { remove(packName) }
        prefs.edit().putStringSet("installed_packs", updated).apply()
        _installedPacks.value = updated.toList()
    }

    fun addFriendFromQr(qrContent: String) {
        viewModelScope.launch {
            try {
                val json = org.json.JSONObject(qrContent)
                val friendName = json.getString("name")
                val packsArray = json.getJSONArray("packs")
                var addedCount = 0
                for (i in 0 until packsArray.length()) {
                    val packName = packsArray.getString(i)
                    if (!getSavedPacks().contains(packName)) {
                        installPack(packName)
                        addedCount++
                    }
                }
                val msg = if (addedCount > 0) "Added $friendName! Installed $addedCount new packs." else "Added $friendName! You already have their packs."
                android.widget.Toast.makeText(getApplication(), msg, android.widget.Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(getApplication(), "Invalid Profile QR Code", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.deleteAllMoments()
        }
    }
    
    fun toggleFavorite(moment: Moment) {
        viewModelScope.launch {
            repository.updateFavorite(moment.id, !moment.isFavorite)
        }
    }

    val allMoments: StateFlow<List<Moment>> = repository.allMoments
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    fun updateServerUrl(url: String) {
        prefs.edit().putString("server_url", url).apply()
        _serverUrl.value = url
        fetchAvailablePacks()
    }

    fun rollRandomQuest() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val allQuests = mutableListOf<Pair<Quest, String>>()
                val completedIds = allMoments.value.map { it.questId }.toSet()
                for (packName in _installedPacks.value) {
                    val pack = repository.getQuestPack(packName, if (_useServer.value) _serverUrl.value else null)
                    if (pack != null) {
                        for (quest in pack.quests) {
                            if (!completedIds.contains(quest.id)) {
                                allQuests.add(Pair(quest, packName))
                            }
                        }
                    }
                }
                if (allQuests.isNotEmpty()) {
                    _randomQuest.value = allQuests.random()
                } else {
                    _randomQuest.value = null
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getQuest(packName: String, questId: String): Quest? {
        val pack = repository.getQuestPack(packName, if (_useServer.value) _serverUrl.value else null)
        return pack?.quests?.find { it.id == questId }
    }

    fun loadPack(packName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val pack = repository.getQuestPack(packName, if (_useServer.value) _serverUrl.value else null)
                _currentPack.value = pack
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun saveMoment(quest: Quest, packName: String, photoUri: String, filterName: String = "none") {
        viewModelScope.launch {
            val moment = Moment(
                questId = quest.id,
                questTitle = quest.title,
                questDescription = quest.description,
                packName = packName,
                photoUri = photoUri,
                filterName = filterName
            )
            repository.insertMoment(moment)
            
            // Streak logic
            val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
            val todayDate = sdf.format(java.util.Date())
            val lastStreakDate = prefs.getString("last_streak_date", "")
            
            if (lastStreakDate != todayDate) {
                // If it's the next day or later, increment streak
                // Simplified: increment if it's not today. We should technically check if it's consecutive.
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
                val yesterdayDate = sdf.format(calendar.time)
                
                var newStreak = _currentStreak.value
                if (lastStreakDate == yesterdayDate) {
                    newStreak += 1
                } else if (lastStreakDate != todayDate) {
                    newStreak = 1 // Reset if missed a day
                }
                
                prefs.edit()
                    .putString("last_streak_date", todayDate)
                    .putInt("current_streak", newStreak)
                    .apply()
                    
                _currentStreak.value = newStreak
                _showStreakAnimation.value = true
                
                if (newStreak == 7) {
                    // Unlock secret pack logic could go here
                }
            }
        }
    }

    fun saveMomentById(questId: String, packName: String, photoUri: String, filterName: String = "none") {
        viewModelScope.launch {
            val quest = getQuest(packName, questId)
            if (quest != null) {
                saveMoment(quest, packName, photoUri, filterName)
            }
        }
    }
}

// Copyright 2025 Citra Project / Mandarine Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.features.settings.model

import android.text.TextUtils
import io.github.mandarine3ds.mandarine.MandarineApplication
import io.github.mandarine3ds.mandarine.R
import io.github.mandarine3ds.mandarine.features.settings.ui.SettingsActivityView
import io.github.mandarine3ds.mandarine.features.settings.utils.SettingsFile
import io.github.mandarine3ds.mandarine.features.settings.model.BooleanSetting
import io.github.mandarine3ds.mandarine.features.settings.model.FloatSetting
import io.github.mandarine3ds.mandarine.features.settings.model.IntSetting
import io.github.mandarine3ds.mandarine.features.settings.model.ScaledFloatSetting
import io.github.mandarine3ds.mandarine.features.settings.model.view.SettingsItem
import java.util.TreeMap

class Settings {
    private var gameId: String? = null

    var isLoaded = false

    /**
     * A HashMap<String></String>, SettingSection> that constructs a new SettingSection instead of returning null
     * when getting a key not already in the map
     */
    class SettingsSectionMap : HashMap<String, SettingSection?>() {
        override operator fun get(key: String): SettingSection? {
            if (!super.containsKey(key)) {
                val section = SettingSection(key)
                super.put(key, section)
                return section
            }
            return super.get(key)
        }
    }

    var sections: HashMap<String, SettingSection?> = SettingsSectionMap()

    fun getSection(sectionName: String): SettingSection? {
        return sections[sectionName]
    }

    val isEmpty: Boolean
        get() = sections.isEmpty()

    private fun clearMemorySettings() {
        BooleanSetting.clear()
        FloatSetting.clear()
        ScaledFloatSetting.clear()
        IntSetting.clear()
        StringSetting.clear()
    }

    fun loadSettings(view: SettingsActivityView? = null, titleId: String = "") {
        clearMemorySettings()
        sections = SettingsSectionMap()
        loadMandarineSettings(view)
        if (!TextUtils.isEmpty(gameId)) loadCustomGameSettings(gameId!!, view)
        if (!TextUtils.isEmpty(titleId)) loadCustomGameSettings(titleId!!, view)
        isLoaded = true
    }

    private fun loadMandarineSettings(view: SettingsActivityView?) {
        for ((fileName) in configFileSectionsMap) {
            val readResult = SettingsFile.readFile(fileName, view)
            sections.putAll(readResult)
        }
    }

    fun loadCustomGameSettings(gameId: String, view: SettingsActivityView?) {
        // Custom game settings
        mergeSections(SettingsFile.readCustomGameSettings(gameId, view))
    }

    private fun mergeSections(updatedSections: HashMap<String, SettingSection?>) {
        for ((key, updatedSection) in updatedSections) {
            val originalSection = sections[key] ?: SettingSection(key).also { sections[key] = it }

            if (updatedSection != null) {
                for (setting in updatedSection.settings.values) {
                    originalSection.putSetting(setting)
                }
            }
            if (updatedSection != null) {
                for (globalSetting in originalSection.settings.values) {
                    if (!updatedSection.settings.containsKey(globalSetting.key)) {
                        originalSection.putSetting(globalSetting, isGlobal = true)
                    }
                }
            }
        }
    }

    fun loadSettings(gameId: String, view: SettingsActivityView) {
        this.gameId = gameId
        loadSettings(view)
    }

    fun importSettings(titleId: String?, input: HashMap<String, SettingSection?>) {
        if (titleId == null) {
            for ((fileName, sectionNames) in configFileSectionsMap.entries) {
                val iniSections = TreeMap<String, SettingSection?>()
                for (section in sectionNames) {
                    iniSections[section] = input[section]
                }
                SettingsFile.saveFile(fileName, iniSections, null)
            }
        } else {
            val gameSections = HashMap<String, SettingSection?>()
            for ((key, section) in input) {
                if (section != null) {
                    gameSections[key] = section
                }
            }
            SettingsFile.saveCustomGameSettings(titleId!!, gameSections, null)
        }
        MandarineApplication.documentsTree.refreshDirectory("/config")
    }

    fun saveSettings(view: SettingsActivityView) {
        if (TextUtils.isEmpty(gameId)) {
            view.showToastMessage(
                MandarineApplication.appContext.getString(R.string.ini_saved),
                false
            )
            for ((fileName, sectionNames) in configFileSectionsMap.entries) {
                val iniSections = TreeMap<String, SettingSection?>()
                for (section in sectionNames) {
                    iniSections[section] = sections[section]
                }
                SettingsFile.saveFile(fileName, iniSections, view!!)
            }
        } else {
            val gameSections = HashMap<String, SettingSection?>()
            for ((key, section) in sections) {
                if (section != null) {
                    val filteredSection = SettingSection(key)
                    for (setting in section.settings.values) {
                        if (!section.isGlobalSetting(setting.key!!)) {
                            filteredSection.putSetting(setting)
                        }
                    }
                    gameSections[key] = filteredSection
                }
            }
            SettingsFile.saveCustomGameSettings(gameId!!, gameSections, view!!)
        }
        MandarineApplication.documentsTree.refreshDirectory("/config")
    }

    fun clearPerGameSetting(item: SettingsItem, view: SettingsActivityView) {
        if (TextUtils.isEmpty(gameId)) return
        sections[item.setting!!.section!!]?.putSetting(item.setting!!, isGlobal = true)
        saveSettings(view)
        loadSettings()
    }

    fun isSettingClearable(setting: AbstractSetting?): Boolean {
        if (TextUtils.isEmpty(gameId)) return false
        
        setting ?: return false
        setting!!.key ?: return false
        
        var foundNonGlobal = false

        for (section in sections.values) {
            val existingSetting = section?.getSetting(setting!!.key!!)
            if (existingSetting != null) {
                if (section.isGlobalSetting(setting!!.key!!)) {
                    return false
                } else {
                    foundNonGlobal = true
                }
            }
        }

        return foundNonGlobal
    }

    fun saveSetting(setting: AbstractSetting, filename: String) {
        SettingsFile.saveFile(filename, setting)
    }

    companion object {
        const val SECTION_CORE = "Core"
        const val SECTION_SYSTEM = "System"
        const val SECTION_CAMERA = "Camera"
        const val SECTION_CONTROLS = "Controls"
        const val SECTION_RENDERER = "Renderer"
        const val SECTION_LAYOUT = "Layout"
        const val SECTION_UTILITY = "Utility"
        const val SECTION_AUDIO = "Audio"
        const val SECTION_DEBUG = "Debugging"
        const val SECTION_THEME = "Theme"
        const val SECTION_CUSTOM_LANDSCAPE = "Custom Landscape"
        const val SECTION_CUSTOM_PORTRAIT = "Custom Portrait"
        const val SECTION_STATS_OVERLAY = "Stats Overlay"

        const val KEY_BUTTON_A = "button_a"
        const val KEY_BUTTON_B = "button_b"
        const val KEY_BUTTON_X = "button_x"
        const val KEY_BUTTON_Y = "button_y"
        const val KEY_BUTTON_SELECT = "button_select"
        const val KEY_BUTTON_START = "button_start"
        const val KEY_BUTTON_HOME = "button_home"
        const val KEY_BUTTON_UP = "button_up"
        const val KEY_BUTTON_DOWN = "button_down"
        const val KEY_BUTTON_LEFT = "button_left"
        const val KEY_BUTTON_RIGHT = "button_right"
        const val KEY_BUTTON_L = "button_l"
        const val KEY_BUTTON_R = "button_r"
        const val KEY_BUTTON_ZL = "button_zl"
        const val KEY_BUTTON_ZR = "button_zr"
        const val KEY_CIRCLEPAD_AXIS_VERTICAL = "circlepad_axis_vertical"
        const val KEY_CIRCLEPAD_AXIS_HORIZONTAL = "circlepad_axis_horizontal"
        const val KEY_CSTICK_AXIS_VERTICAL = "cstick_axis_vertical"
        const val KEY_CSTICK_AXIS_HORIZONTAL = "cstick_axis_horizontal"
        const val KEY_DPAD_AXIS_VERTICAL = "dpad_axis_vertical"
        const val KEY_DPAD_AXIS_HORIZONTAL = "dpad_axis_horizontal"

        const val HOTKEY_SCREEN_SWAP = "hotkey_screen_swap"
        const val HOTKEY_CYCLE_LAYOUT = "hotkey_toggle_layout"
        const val HOTKEY_CLOSE_GAME = "hotkey_close_game"
        const val HOTKEY_PAUSE_OR_RESUME = "hotkey_pause_or_resume_game"
        const val HOTKEY_QUICKSAVE = "hotkey_quickload"
        const val HOTKEY_QUICKlOAD = "hotkey_quickpause"

        val buttonKeys = listOf(
            KEY_BUTTON_A,
            KEY_BUTTON_B,
            KEY_BUTTON_X,
            KEY_BUTTON_Y,
            KEY_BUTTON_SELECT,
            KEY_BUTTON_START,
            KEY_BUTTON_HOME
        )
        val buttonTitles = listOf(
            R.string.button_a,
            R.string.button_b,
            R.string.button_x,
            R.string.button_y,
            R.string.button_select,
            R.string.button_start,
            R.string.button_home
        )
        val circlePadKeys = listOf(
            KEY_CIRCLEPAD_AXIS_VERTICAL,
            KEY_CIRCLEPAD_AXIS_HORIZONTAL
        )
        val cStickKeys = listOf(
            KEY_CSTICK_AXIS_VERTICAL,
            KEY_CSTICK_AXIS_HORIZONTAL
        )
        val dPadAxisKeys = listOf(
            KEY_DPAD_AXIS_VERTICAL,
            KEY_DPAD_AXIS_HORIZONTAL
        )
        val dPadButtonKeys = listOf(
            KEY_BUTTON_UP,
            KEY_BUTTON_DOWN,
            KEY_BUTTON_LEFT,
            KEY_BUTTON_RIGHT
        )
        val axisTitles = listOf(
            R.string.controller_axis_vertical,
            R.string.controller_axis_horizontal
        )
        val dpadTitles = listOf(
            R.string.direction_up,
            R.string.direction_down,
            R.string.direction_left,
            R.string.direction_right
        )
        val triggerKeys = listOf(
            KEY_BUTTON_L,
            KEY_BUTTON_R,
            KEY_BUTTON_ZL,
            KEY_BUTTON_ZR
        )
        val triggerTitles = listOf(
            R.string.button_l,
            R.string.button_r,
            R.string.button_zl,
            R.string.button_zr
        )
        val hotKeys = listOf(
            HOTKEY_SCREEN_SWAP,
            HOTKEY_CYCLE_LAYOUT,
            HOTKEY_CLOSE_GAME,
            HOTKEY_PAUSE_OR_RESUME,
            HOTKEY_QUICKSAVE,
            HOTKEY_QUICKlOAD
        )
        val hotkeyTitles = listOf(
            R.string.emulation_swap_screens,
            R.string.emulation_cycle_landscape_layouts,
            R.string.emulation_close_game,
            R.string.emulation_toggle_pause,
            R.string.emulation_quicksave,
            R.string.emulation_quickload,
        )

        const val PREF_FIRST_APP_LAUNCH = "FirstApplicationLaunch"
        const val PREF_MATERIAL_YOU = "MaterialYouTheme"
        const val PREF_THEME_MODE = "ThemeMode"
        const val PREF_BLACK_BACKGROUNDS = "BlackBackgrounds"
        const val PREF_SHOW_HOME_APPS = "ShowHomeApps"
        const val PREF_STATIC_THEME_COLOR = "StaticThemeColor"

        private val configFileSectionsMap: MutableMap<String, List<String>> = HashMap()

        init {
            configFileSectionsMap[SettingsFile.FILE_NAME_CONFIG] =
                listOf(
                    SECTION_CORE,
                    SECTION_SYSTEM,
                    SECTION_CAMERA,
                    SECTION_CONTROLS,
                    SECTION_RENDERER,
                    SECTION_LAYOUT,
                    SECTION_UTILITY,
                    SECTION_AUDIO,
                    SECTION_DEBUG
                )
        }
    }
}

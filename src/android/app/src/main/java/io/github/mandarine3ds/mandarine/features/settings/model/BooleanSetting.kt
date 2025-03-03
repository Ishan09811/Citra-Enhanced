// Copyright 2025 Citra Project / Mandarine Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package io.github.mandarine3ds.mandarine.features.settings.model

enum class BooleanSetting(
    override val key: String,
    override val section: String,
    override val defaultValue: Boolean
) : AbstractBooleanSetting {
    EXPAND_TO_CUTOUT_AREA("expand_to_cutout_area", Settings.SECTION_LAYOUT, false),
    SPIRV_SHADER_GEN("spirv_shader_gen", Settings.SECTION_RENDERER, true),
    ASYNC_SHADERS("async_shader_compilation", Settings.SECTION_RENDERER, false),
    ADRENO_GPU_BOOST("adreno_gpu_boost", Settings.SECTION_RENDERER, false),
    PLUGIN_LOADER("plugin_loader", Settings.SECTION_SYSTEM, false),
    ALLOW_PLUGIN_LOADER("allow_plugin_loader", Settings.SECTION_SYSTEM, true),
    SWAP_SCREEN("swap_screen", Settings.SECTION_LAYOUT, false),
    CUSTOM_LAYOUT("custom_layout",Settings.SECTION_LAYOUT,false),
    INSTANT_DEBUG_LOG("instant_debug_log", Settings.SECTION_DEBUG, true),
    SHOW_FPS("show_fps", Settings.SECTION_LAYOUT, true),
    SHOW_SPEED("show_speed", Settings.SECTION_LAYOUT, false),
    SHOW_APP_RAM_USAGE("show_app_ram_usage", Settings.SECTION_LAYOUT, false),
    SHOW_SYSTEM_RAM_USAGE("show_system_ram_usage", Settings.SECTION_LAYOUT, false),
    SHOW_BAT_TEMPERATURE("show_bat_temperature", Settings.SECTION_LAYOUT, false),
    OVERLAY_BACKGROUND("overlay_background", Settings.SECTION_LAYOUT, false),
    DEBUG_RENDERER("renderer_debug", Settings.SECTION_DEBUG, false),
    SHADERS_ACCURATE_MUL("shaders_accurate_mul", Settings.SECTION_RENDERER, false),
    DISK_SHADER_CACHE("use_disk_shader_cache", Settings.SECTION_RENDERER, true),
    DUMP_TEXTURES("dump_textures", Settings.SECTION_UTILITY, false),
    CUSTOM_TEXTURES("custom_textures", Settings.SECTION_UTILITY, false),
    ASYNC_CUSTOM_LOADING("async_custom_loading", Settings.SECTION_UTILITY, true),
    PRELOAD_TEXTURES("preload_textures", Settings.SECTION_UTILITY, false),
    ENABLE_AUDIO_STRETCHING("enable_audio_stretching", Settings.SECTION_AUDIO, true),
    ENABLE_REALTIME_AUDIO("enable_realtime_audio", Settings.SECTION_AUDIO, false),
    CPU_JIT("use_cpu_jit", Settings.SECTION_CORE, true),
    HW_SHADER("use_hw_shader", Settings.SECTION_RENDERER, true),
    VSYNC("use_vsync_new", Settings.SECTION_RENDERER, true),
    REDUCE_DOWNCOUNT_SLICE("reduce_downcount_slice", Settings.SECTION_CORE, false),
    PRIORITY_BOOST_STARVED_THREADS("priority_boost_starved_threads", Settings.SECTION_CORE, true),
    CUSTOM_CPU_TICKS("custom_cpu_ticks", Settings.SECTION_CORE, false),
    USE_FRAME_LIMIT("use_frame_limit", Settings.SECTION_RENDERER, true),
    LINEAR_FILTERING("filter_mode", Settings.SECTION_RENDERER, true),
    FORCE_HW_VERTEX_SHADERS("force_hw_vertex_shaders", Settings.SECTION_RENDERER, false),
    DISABLE_SURFACE_TEXTURE_COPY("disable_surface_texture_copy", Settings.SECTION_RENDERER, false),
    DISABLE_FLUSH_CPU_WRITE("disable_flush_cpu_write", Settings.SECTION_RENDERER, false),
    LLE_APPLETS("lle_applets", Settings.SECTION_SYSTEM, false),
    NEW_3DS("is_new_3ds", Settings.SECTION_SYSTEM, true),
    USE_ARTIC_BASE_CONTROLLER("use_artic_base_controller", Settings.SECTION_CONTROLS, false);

    override var boolean: Boolean = defaultValue

    override val valueAsString: String
        get() = boolean.toString()

    override val isRuntimeEditable: Boolean
        get() {
            for (setting in NOT_RUNTIME_EDITABLE) {
                if (setting == this) {
                    return false
                }
            }
            return true
        }

    companion object {
        private val NOT_RUNTIME_EDITABLE = listOf(
            ASYNC_SHADERS,
            ADRENO_GPU_BOOST,
            PLUGIN_LOADER,
            ALLOW_PLUGIN_LOADER,
            NEW_3DS,
            LLE_APPLETS,
            VSYNC,
            REDUCE_DOWNCOUNT_SLICE,
            DEBUG_RENDERER,
            CPU_JIT,
            ASYNC_CUSTOM_LOADING,
            USE_ARTIC_BASE_CONTROLLER
        )

        fun from(key: String): BooleanSetting? =
            BooleanSetting.values().firstOrNull { it.key == key }

        fun clear() = BooleanSetting.values().forEach { it.boolean = it.defaultValue }
    }
}

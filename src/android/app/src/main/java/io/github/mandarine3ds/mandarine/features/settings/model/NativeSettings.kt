
package io.github.mandarine3ds.mandarine.features.settings.model

import androidx.annotation.Keep

object NativeSettings {
  
    @Keep
    @JvmStatic
    fun getBooleanSetting(key: String): Boolean {
        return BooleanSetting.from(key)?.boolean ?: false
    }

    @Keep
    @JvmStatic
    fun getIntSetting(key: String): Int {
        return IntSetting.from(key)?.int ?: 0
    }

    @Keep
    @JvmStatic
    fun getScaledFloatSetting(key: String): Float {
        return ScaledFloatSetting.from(key)?.float ?: 0.0f
    }

    @Keep
    @JvmStatic
    fun getStringSetting(key: String): String {
        return StringSetting.from(key)?.string ?: ""
    }
}

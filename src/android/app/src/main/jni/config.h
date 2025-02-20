// Copyright 2014 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

#pragma once

#include <memory>
#include <string>
#include "common/settings.h"

class INIReader;

class Config {
private:
    void ReadValues();

public:
    Config();
    ~Config();

    void Reload();

private:
    /**
     * Applies a value read from the sdl2_config to a Setting.
     *
     * @param group The name of the INI group
     * @param setting The yuzu setting to modify
     */
    template <typename Type, bool ranged>
    void ReadSetting(const std::string& group, Settings::Setting<Type, ranged>& setting);
    bool GetBooleanSetting(const std::string& key, const bool placeholder = false);
    int GetIntegerSetting(const std::string& key, const int placeholder = 0);
    std::string GetStringSetting(const std::string& key, const std::string& placeholder = "");
    float GetFloatSetting(const std::string& key, const bool scaled, const float placeholder = 0.0f);
};

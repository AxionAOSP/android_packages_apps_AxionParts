/*
 * Copyright (C) 2025 AxionOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.axion.axionparts.tiles

import android.database.ContentObserver
import android.graphics.drawable.Icon
import android.os.Handler
import android.os.Looper
import android.os.SystemProperties
import android.os.UserHandle
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.android.axion.axionparts.R

class PerformanceModeTileService : TileService() {

    companion object {
        private const val POWER_MODE_KEY = "persist.sys.power_mode_perf"
        private const val POWER_MODE_BY_USER_KEY = "persist.sys.power_mode_perf_by_user"
    }

    private val settingsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            updateTileState()
        }
    }

    private fun isPerformanceModeEnabled(): Boolean {
        return try {
            Settings.System.getIntForUser(
                contentResolver,
                POWER_MODE_KEY,
                0,
                UserHandle.USER_CURRENT
            ) == 1
        } catch (e: Exception) {
            SystemProperties.getInt(POWER_MODE_KEY, 0) == 1
        }
    }

    private fun setPerformanceMode(enabled: Boolean) {
        val modeValue = if (enabled) 1 else 0
        SystemProperties.set(POWER_MODE_KEY, modeValue.toString())
        Settings.System.putIntForUser(
            contentResolver,
            POWER_MODE_KEY,
            modeValue,
            UserHandle.USER_CURRENT
        )
        Settings.System.putIntForUser(
            contentResolver,
            POWER_MODE_BY_USER_KEY,
            modeValue,
            UserHandle.USER_CURRENT
        )
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isEnabled = isPerformanceModeEnabled()
        
        tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.qs_tile_performance)
        tile.subtitle = if (isEnabled) getString(R.string.qs_tile_on) else getString(R.string.qs_tile_off)
        tile.icon = Icon.createWithResource(this, android.R.drawable.ic_menu_manage)
        tile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        contentResolver.registerContentObserver(
            Settings.System.getUriFor(POWER_MODE_KEY),
            false,
            settingsObserver
        )
        updateTileState()
    }

    override fun onStopListening() {
        super.onStopListening()
        contentResolver.unregisterContentObserver(settingsObserver)
    }

    override fun onClick() {
        super.onClick()
        val currentState = isPerformanceModeEnabled()
        setPerformanceMode(!currentState)
        updateTileState()
    }
}

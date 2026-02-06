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
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.android.axion.axionparts.R

class PcModeTileService : TileService() {

    companion object {
        private const val PC_MODE_KEY = "ax_pc_mode"
    }

    private val settingsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            updateTileState()
        }
    }

    private fun isPcModeEnabled(): Boolean {
        return Settings.Secure.getInt(contentResolver, PC_MODE_KEY, 0) == 1
    }

    private fun setPcMode(enabled: Boolean) {
        Settings.Secure.putInt(contentResolver, PC_MODE_KEY, if (enabled) 1 else 0)
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isEnabled = isPcModeEnabled()
        tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.qs_tile_pc_mode)
        tile.subtitle = if (isEnabled) getString(R.string.qs_tile_on) else getString(R.string.qs_tile_off)
        val iconRes = if (isEnabled) R.drawable.ic_qs_pc_mode else R.drawable.ic_qs_pc_mode_disabled
        tile.icon = Icon.createWithResource(this, iconRes)
        tile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(PC_MODE_KEY),
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
        val currentState = isPcModeEnabled()
        setPcMode(!currentState)
        updateTileState()
    }
}

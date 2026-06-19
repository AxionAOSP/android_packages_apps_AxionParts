/*
 * Copyright (C) 2026 AxionOS Project
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

package com.android.axion.axionparts.search

import android.database.Cursor
import android.database.MatrixCursor
import android.provider.SearchIndexablesProvider
import android.provider.SearchIndexablesContract.*
import com.android.axion.axionparts.R

class PartsSearchIndexablesProvider : SearchIndexablesProvider() {

    override fun onCreate(): Boolean = true

    override fun queryXmlResources(projection: Array<out String>?): Cursor {
        return MatrixCursor(INDEXABLES_XML_RES_COLUMNS)
    }

    override fun queryRawData(projection: Array<out String>?): Cursor {
        val cursor = MatrixCursor(projection ?: INDEXABLES_RAW_COLUMNS)
        val context = context ?: return cursor

        for (feature in FeatureRegistry.FEATURES) {
            val ref = arrayOfNulls<Any>(INDEXABLES_RAW_COLUMNS.size)
            ref[COLUMN_INDEX_RAW_TITLE] = context.getString(feature.titleRes)
            ref[COLUMN_INDEX_RAW_SUMMARY_ON] = context.getString(feature.summaryRes)
            ref[COLUMN_INDEX_RAW_SUMMARY_OFF] = context.getString(feature.summaryRes)
            ref[COLUMN_INDEX_RAW_KEYWORDS] = context.getString(feature.keywordsRes)
            ref[COLUMN_INDEX_RAW_SCREEN_TITLE] = context.getString(R.string.personalizations)
            ref[COLUMN_INDEX_RAW_INTENT_ACTION] = feature.action
            ref[COLUMN_INDEX_RAW_INTENT_TARGET_PACKAGE] = feature.targetPackage ?: context.packageName
            ref[COLUMN_INDEX_RAW_INTENT_TARGET_CLASS] = feature.targetClass ?: "com.android.axion.axionparts.DashboardActivity"
            ref[COLUMN_INDEX_RAW_KEY] = feature.key
            cursor.addRow(ref)
        }

        return cursor
    }

    override fun queryNonIndexableKeys(projection: Array<out String>?): Cursor {
        return MatrixCursor(NON_INDEXABLES_KEYS_COLUMNS)
    }
}

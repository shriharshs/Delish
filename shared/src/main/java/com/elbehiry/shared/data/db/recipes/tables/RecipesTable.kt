/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elbehiry.shared.data.db.recipes.tables

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.elbehiry.shared.data.db.recipes.entities.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface RecipesTable {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecipe(recipe: RecipeEntity)

    @Query(
        """
            SELECT * FROM ${RecipeEntity.Schema.TABLE_NAME}
            """
    )
    suspend fun getRecipes(): List<RecipeEntity>

    @Query(
        """
            SELECT * FROM ${RecipeEntity.Schema.TABLE_NAME}
            ORDER BY ${RecipeEntity.Schema.RECIPE_ID} DESC
            LIMIT 1
            """
    )
    fun observeOnLastAdded(): Flow<RecipeEntity>
}

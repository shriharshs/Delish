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

package com.elbehiry.shared.domain.recipes.random

import com.elbehiry.shared.domain.UseCase
import com.elbehiry.model.RecipesItem
import com.elbehiry.model.toUiModel
import com.elbehiry.shared.data.recipes.random.repository.RandomRecipesRepository
import com.elbehiry.shared.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

const val defaultRecipesNumber = 10

class GetRandomRecipesUseCase @Inject constructor(
    private val randomRecipesRepository: RandomRecipesRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher
) : UseCase<GetRandomRecipesUseCase.Params, List<RecipesItem>>(ioDispatcher) {

    override suspend fun execute(parameters: Params): List<RecipesItem> =
        randomRecipesRepository.getRandomRecipes(parameters.tags, parameters.number).map {
            it.toUiModel()
        }

    class Params private constructor(
        val tags: String?,
        val number: Int? = defaultRecipesNumber

    ) {

        companion object {
            @JvmStatic
            fun create(tags: String?, number: Int?): Params {
                return Params(tags, number)
            }
        }
    }
}

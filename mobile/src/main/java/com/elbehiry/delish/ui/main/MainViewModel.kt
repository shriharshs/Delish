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

package com.elbehiry.delish.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.elbehiry.delish.ui.util.IngredientListProvider
import com.elbehiry.model.CuisineItem
import com.elbehiry.model.IngredientItem
import com.elbehiry.model.RecipesItem
import com.elbehiry.shared.domain.recipes.bookmark.GetSavedRecipesUseCase
import com.elbehiry.shared.domain.recipes.bookmark.ObserveOnLastItemAddedUseCase
import com.elbehiry.shared.domain.recipes.bookmark.SaveRecipeUseCase
import com.elbehiry.shared.domain.recipes.cuisines.GetAvailableCuisinesUseCase
import com.elbehiry.shared.domain.recipes.random.GetRandomRecipesUseCase
import com.elbehiry.shared.result.Result
import com.elbehiry.shared.result.data
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getRandomRecipesUseCase: GetRandomRecipesUseCase,
    private val getAvailableCuisinesUseCase: GetAvailableCuisinesUseCase,
    private val saveRecipeUseCase: SaveRecipeUseCase,
    private val getSavedRecipesUseCase: GetSavedRecipesUseCase,
    private val observeOnLastItemAddedUseCase: ObserveOnLastItemAddedUseCase
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _hasError = MutableLiveData<String>()
    val hasError: LiveData<String> = _hasError

    private val _ingredientList = MutableLiveData<List<IngredientItem>>()
    val ingredientList: LiveData<List<IngredientItem>> = _ingredientList

    private val _cuisinesList = MutableLiveData<List<CuisineItem>>()
    val cuisinesList: LiveData<List<CuisineItem>> = _cuisinesList

    private val _randomRecipes = MutableLiveData<List<RecipesItem>>()
    val randomRecipes: LiveData<List<RecipesItem>> = _randomRecipes

    private val _savedRecipes = MutableLiveData<MutableList<RecipesItem>>()
    val savedRecipes: LiveData<MutableList<RecipesItem>> = _savedRecipes

    init {
        getHomeContent()
    }

    private fun getHomeContent() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                coroutineScope {
                    val ingredientListDeferred = async { IngredientListProvider.ingredientList }
                    val cuisinesListDeferred = async { getAvailableCuisinesUseCase(Unit) }
                    val randomRecipesDeferred = async {
                        getRandomRecipesUseCase(
                            GetRandomRecipesUseCase.Params.create(
                                null,
                                20
                            )
                        )
                    }

                    val savedRecipesDeferred = async {
                        getSavedRecipesUseCase(Unit)
                    }

                    val ingredientList = ingredientListDeferred.await()
                    val cuisinesList = cuisinesListDeferred.await()
                    val randomRecipes = randomRecipesDeferred.await()
                    val savedRecipes = savedRecipesDeferred.await()

                    if (cuisinesList is Result.Error) {
                        _hasError.postValue(cuisinesList.exception.message)
                    } else if (randomRecipes is Result.Error) {
                        _hasError.postValue(randomRecipes.exception.message)
                    }

                    _randomRecipes.postValue(randomRecipes.data ?: listOf())
                    _ingredientList.postValue(ingredientList)
                    _cuisinesList.postValue(cuisinesList.data ?: listOf())
                    _savedRecipes.postValue((savedRecipes.data ?: mutableListOf()).toMutableList())
                }
            } catch (e: Exception) {
                _hasError.postValue(e.message)
            } finally {
                _isLoading.value = false
                observeOnLastAdded()
            }
        }
    }

    fun saveRecipe(recipesItem: RecipesItem) {
        viewModelScope.launch {
            saveRecipeUseCase(recipesItem)
        }
    }

    private fun observeOnLastAdded() {
        viewModelScope.launch {
            observeOnLastItemAddedUseCase(Unit).collect {
                if (it.data != null) {
                    _savedRecipes.value?.add(it.data!!)
                }
            }
        }
    }

    fun deleteRecipe(recipe: RecipesItem) {
        _savedRecipes.value?.remove(recipe)
    }
}

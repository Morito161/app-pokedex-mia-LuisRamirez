package com.example.ciclovidaactivity

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PokemonViewModel : ViewModel() {
    private val BASE_URL = "https://pokeapi.co/api/v2/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val apiService: PokeApiService = retrofit.create(PokeApiService::class.java)

    private var fullPokemonList = listOf<PokemonDetail>() // << NUEVO: Lista completa

    val pokemonList = MutableLiveData<List<PokemonDetail>>()
    val selectedPokemon = MutableLiveData<PokemonDetail?>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()

    init {
        fetchPokemonList()
    }

    private fun fetchPokemonList() {
        isLoading.value = true
        error.value = null

        viewModelScope.launch {
            try {
                val response = apiService.getPokemonList()

                val detailedPokemonList = response.results.map {
                    async {
                        try {
                            val id = it.getId()
                            if (id != null) {
                                val detail = apiService.getPokemonDetail(id)
                                val species = apiService.getPokemonSpecies(id)
                                detail.description = species.getFlavorText().replace('\n', ' ')
                                detail
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }
                }.awaitAll().filterNotNull()

                fullPokemonList = detailedPokemonList // << NUEVO: Guardar lista completa
                pokemonList.value = fullPokemonList // << CAMBIO: Mostrar lista completa

            } catch (e: Exception) {
                Log.e("PokeViewModel", "Error al obtener lista: ${e.message}")
                error.value = "Error al cargar la lista de Pokémon: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    // << NUEVA FUNCIÓN DE BÚSQUEDA >>
    fun searchPokemon(query: String?) {
        if (query.isNullOrBlank()) {
            pokemonList.value = fullPokemonList // Si no hay búsqueda, mostrar todos
        } else {
            val filteredList = fullPokemonList.filter {
                it.name.contains(query, ignoreCase = true) || it.id.toString().contains(query)
            }
            pokemonList.value = filteredList
        }
    }

    fun selectPokemon(pokemon: PokemonDetail) {
        selectedPokemon.value = pokemon
    }

    fun clearSelection() {
        selectedPokemon.value = null
    }
}
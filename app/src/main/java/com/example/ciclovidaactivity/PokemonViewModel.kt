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

    // Cambiamos a PokemonDetail para tener toda la info en la lista
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
                // 1. Obtener la lista de referencias
                val response = apiService.getPokemonList()

                // 2. Para cada referencia, obtener los detalles y la descripción en paralelo
                val detailedPokemonList = response.results.map { pokemonRef ->
                    async { // 'async' permite que las llamadas se hagan en paralelo
                        try {
                            val id = pokemonRef.getId()
                            if (id != null) {
                                // Hacemos dos llamadas por Pokémon
                                val detail = apiService.getPokemonDetail(id)
                                val species = apiService.getPokemonSpecies(id)

                                // Combinamos los resultados
                                detail.description = species.getFlavorText().replace('\n', ' ')
                                detail // Devolvemos el objeto completo
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            Log.e("PokeViewModel", "Error al obtener detalle de ${pokemonRef.name}: ${e.message}")
                            null // Si algo falla para un Pokémon, no lo incluimos
                        }
                    }
                }.awaitAll().filterNotNull() // Esperamos a que todas las llamadas terminen y filtramos los nulos

                pokemonList.value = detailedPokemonList

            } catch (e: Exception) {
                Log.e("PokeViewModel", "Error al obtener lista: ${e.message}")
                error.value = "Error al cargar la lista de Pokémon: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    // Ahora recibe el objeto completo, no necesita hacer llamada a la red
    fun selectPokemon(pokemon: PokemonDetail) {
        selectedPokemon.value = pokemon
    }

    fun clearSelection() {
        selectedPokemon.value = null
    }
}
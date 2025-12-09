package com.example.ciclovidaactivity

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// =======================================================================================
// 1. ESTRUCTURAS DE DATOS (DATA CLASSES)
//    Definen cómo se deserializan los datos JSON de la PokeAPI.
// =======================================================================================

// Respuesta de la lista principal
data class PokemonListResponse(
    val results: List<PokemonRef>
)

// Referencia simple de un Pokémon en la lista
data class PokemonRef(
    val name: String,
    val url: String
) {
    // Función de ayuda para extraer el ID del Pokémon de la URL (ej: ".../pokemon/1/" -> 1)
    fun getId(): Int? {
        val parts = url.split('/')
        return parts.dropLast(1).lastOrNull()?.toIntOrNull()
    }
}

// Estructura de detalle de un Pokémon
data class PokemonDetail(
    val id: Int,
    val name: String,
    val height: Int, // En decímetros (dm)
    val weight: Int, // En hectogramos (hg)
    val sprites: Sprites,
    var description: String = "" // Añadimos un campo para la descripción
)

// Estructura para los sprites (imágenes)
data class Sprites(
    val other: OtherSprites
)

data class OtherSprites(
    @SerializedName("official-artwork")
    val officialArtwork: OfficialArtwork
)

data class OfficialArtwork(
    val front_default: String
)

// Estructura para la descripción del Pokémon (del endpoint de especies)
data class PokemonSpecies(
    @SerializedName("flavor_text_entries")
    val flavorTextEntries: List<FlavorTextEntry>
) {
    // Función para obtener la primera descripción en español o inglés
    fun getFlavorText(): String {
        return flavorTextEntries.firstOrNull { it.language.name == "es" }?.flavorText
            ?: flavorTextEntries.firstOrNull { it.language.name == "en" }?.flavorText
            ?: "No description available."
    }
}

data class FlavorTextEntry(
    @SerializedName("flavor_text")
    val flavorText: String,
    val language: Language
)

data class Language(
    val name: String
)


// =======================================================================================
// 2. INTERFAZ DE RETROFIT (PokeApiService)
//    Define los endpoints que vamos a consumir de la PokeAPI.
// =======================================================================================

interface PokeApiService {

    // Endpoint para obtener la lista principal de Pokémon (limitamos a 151)
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 151,
        @Query("offset") offset: Int = 0
    ): PokemonListResponse

    // Endpoint para obtener los detalles de un Pokémon específico por su ID
    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(@Path("id") pokemonId: Int): PokemonDetail

    // Endpoint para obtener la descripción de la especie de un Pokémon
    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(@Path("id") pokemonId: Int): PokemonSpecies
}
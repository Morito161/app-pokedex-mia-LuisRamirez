package com.example.ciclovidaactivity

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class PokemonAdapter(
    private var pokemonList: List<PokemonDetail>,
    private val onPokemonClick: (PokemonDetail) -> Unit
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    class PokemonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.pokemon_name_text)
        val idTextView: TextView = view.findViewById(R.id.pokemon_id_text)
        val cardView: CardView = view.findViewById(R.id.pokemon_card_view)
        val imageView: ImageView = view.findViewById(R.id.pokemon_image)
        val descriptionTextView: TextView = view.findViewById(R.id.pokemon_description_text)
        val playSoundButton: ImageButton = view.findViewById(R.id.play_sound_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemonList[position]

        holder.nameTextView.text = pokemon.name.replaceFirstChar { it.uppercase() }
        holder.idTextView.text = "#${pokemon.id}"
        holder.descriptionTextView.text = pokemon.description

        // Cargar imagen con Coil
        holder.imageView.load(pokemon.sprites.other.officialArtwork.front_default) {
            crossfade(true)
            placeholder(android.R.color.darker_gray)
            error(android.R.color.darker_gray)
        }

        holder.cardView.setOnClickListener {
            onPokemonClick(pokemon)
        }

        holder.playSoundButton.setOnClickListener {
            val audioUrl = "https://raw.githubusercontent.com/PokeAPI/cries/main/cries/pokemon/latest/${pokemon.id}.ogg"
            try {
                MediaPlayer().apply {
                    setDataSource(audioUrl)
                    prepareAsync()
                    setOnPreparedListener { start() }
                    setOnCompletionListener { release() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount() = pokemonList.size

    // Función para actualizar los datos del adaptador de forma eficiente
    fun updateData(newPokemonList: List<PokemonDetail>) {
        this.pokemonList = newPokemonList
        notifyDataSetChanged()
    }
}
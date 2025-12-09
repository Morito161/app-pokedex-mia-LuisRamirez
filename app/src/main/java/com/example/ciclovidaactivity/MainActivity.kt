package com.example.ciclovidaactivity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load

class MainActivity : ComponentActivity() {

    private val viewModel: PokemonViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var pokemonAdapter: PokemonAdapter
    private lateinit var detailView: View
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.pokemon_recycler_view)
        detailView = findViewById(R.id.pokemon_detail_container)
        loadingProgressBar = findViewById(R.id.loading_progress_bar)
        errorTextView = findViewById(R.id.error_text_view)
        toolbar = findViewById(R.id.toolbar)
        toolbarTitle = findViewById(R.id.toolbar_title)

        setupRecyclerView()
        setupToolbar()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        pokemonAdapter = PokemonAdapter(emptyList()) { pokemon ->
            viewModel.selectPokemon(pokemon)
        }
        recyclerView.adapter = pokemonAdapter
    }

    private fun setupToolbar() {
        toolbarTitle.text = "Lista de Pokémon"
        toolbar.setNavigationOnClickListener {
            if (viewModel.selectedPokemon.value != null) {
                viewModel.clearSelection()
            }
        }

        // Inflar el menú y configurar la búsqueda
        toolbar.inflateMenu(R.menu.main_menu)
        val searchItem = toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchPokemon(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchPokemon(newText)
                return true
            }
        })
    }

    private fun observeViewModel() {
        viewModel.pokemonList.observe(this, Observer { list ->
            pokemonAdapter.updateData(list)
        })

        viewModel.isLoading.observe(this, Observer { isLoading ->
            loadingProgressBar.isVisible = isLoading
            if (isLoading) {
                errorTextView.isVisible = false
            }
        })

        viewModel.error.observe(this, Observer { errorMsg ->
            if (errorMsg != null) {
                errorTextView.text = errorMsg
                errorTextView.isVisible = true
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            } else {
                errorTextView.isVisible = false
            }
        })

        viewModel.selectedPokemon.observe(this, Observer { detail ->
            if (detail == null) {
                recyclerView.isVisible = true
                detailView.isVisible = false
                toolbarTitle.text = "Lista de Pokémon"
                toolbar.navigationIcon = null
            } else {
                displayPokemonDetail(detail)
                recyclerView.isVisible = false
                detailView.isVisible = true
                toolbarTitle.text = detail.name.replaceFirstChar { it.uppercase() }
                toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            }
        })
    }

    private fun displayPokemonDetail(detail: PokemonDetail) {
        val nameDetail = findViewById<TextView>(R.id.detail_pokemon_name)
        val idDetail = findViewById<TextView>(R.id.detail_pokemon_id)
        val imageDetail = findViewById<ImageView>(R.id.detail_pokemon_image)
        val heightDetail = findViewById<TextView>(R.id.detail_height)
        val weightDetail = findViewById<TextView>(R.id.detail_weight)
        val descriptionDetail = findViewById<TextView>(R.id.detail_description)

        nameDetail.text = detail.name.replaceFirstChar { it.uppercase() }
        idDetail.text = "ID: #${detail.id}"
        heightDetail.text = "Altura: ${detail.height / 10.0} m"
        weightDetail.text = "Peso: ${detail.weight / 10.0} kg"
        descriptionDetail.text = detail.description

        imageDetail.load(detail.sprites.other.officialArtwork.front_default) {
            placeholder(android.R.color.darker_gray)
            error(android.R.color.holo_red_dark)
            crossfade(true)
        }
    }
}
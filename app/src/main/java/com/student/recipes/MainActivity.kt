package com.student.recipes

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private val allRecipes = mutableListOf<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.recipesContainer)
        val searchEditText = findViewById<EditText>(R.id.searchEditText)

        loadRecipesFromJSON()

        // Anlık arama ve filtreleme mantığı
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRecipes(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadRecipesFromJSON() {
        try {
            val jsonString = assets.open("recipes.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            allRecipes.clear()
            for (i in 0 until jsonArray.length()) {
                allRecipes.add(jsonArray.getJSONObject(i))
            }
            displayRecipes(allRecipes)
        } catch (e: Exception) {
            val errorView = TextView(this).apply {
                text = "Tarifler yüklenirken bir hata oluştu: ${e.message}"
                setTextColor(Color.RED)
            }
            container.addView(errorView)
        }
    }

    private fun filterRecipes(query: String) {
        if (query.isEmpty()) {
            displayRecipes(allRecipes)
            return
        }

        val filtered = allRecipes.filter { recipe ->
            val name = recipe.optString("name", "").lowercase()
            val category = recipe.optString("category", "").lowercase()
            val ingredients = recipe.optJSONArray("ingredients")?.toString()?.lowercase() ?: ""
            name.contains(query.lowercase()) || category.contains(query.lowercase()) || ingredients.contains(query.lowercase())
        }
        displayRecipes(filtered)
    }

    private fun displayRecipes(recipes: List<JSONObject>) {
        container.removeAllViews()

        if (recipes.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "Aradığınız kriterlere uygun tarif bulunamadı."
                textSize = 15f
                setTextColor(Color.GRAY)
                setPadding(16, 32, 16, 32)
            }
            container.addView(emptyView)
            return
        }

        for (recipe in recipes) {
            val name = recipe.optString("name", "Tarif")
            val category = recipe.optString("category", "Genel")
            val prepTime = recipe.optString("prepTime", "30 dk")
            val difficulty = recipe.optString("difficulty", "Orta")
            val calories = recipe.optInt("calories", 0)
            val isVegan = recipe.optBoolean("vegan", false)

            val ingredientsArray = recipe.optJSONArray("ingredients")
            val ingredientsList = mutableListOf<String>()
            if (ingredientsArray != null) {
                for (j in 0 until ingredientsArray.length()) {
                    ingredientsList.add(ingredientsArray.getString(j))
                }
            }

            val instructionsArray = recipe.optJSONArray("instructions")
            val instructionsList = mutableListOf<String>()
            if (instructionsArray != null) {
                for (j in 0 until instructionsArray.length()) {
                    instructionsList.add(instructionsArray.getString(j))
                }
            }

            // Gölge ve kenarlıklı MaterialCardView
            val card = MaterialCardView(this).apply {
                radius = 24f
                cardElevation = 6f
                setCardBackgroundColor(Color.WHITE)
                strokeWidth = 2
                strokeColor = Color.parseColor("#E2E8F0")

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 0, 24)
                layoutParams = params
            }

            val cardContent = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(28, 28, 28, 28)
            }

            // Rozetler Satırı (Kategori & Vegan)
            val badgeRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val categoryBadge = TextView(this).apply {
                text = category.uppercase()
                textSize = 11f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#0F766E"))
                setBackgroundColor(Color.parseColor("#CCFBF1"))
                setPadding(16, 6, 16, 6)
            }
            badgeRow.addView(categoryBadge)

            if (isVegan) {
                val veganBadge = TextView(this).apply {
                    text = "🌱 VEGAN"
                    textSize = 11f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.parseColor("#15803D"))
                    setBackgroundColor(Color.parseColor("#DCFCE7"))
                    setPadding(16, 6, 16, 6)
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(12, 0, 0, 0)
                    layoutParams = params
                }
                badgeRow.addView(veganBadge)
            }
            cardContent.addView(badgeRow)

            // Tarif Başlığı
            val titleView = TextView(this).apply {
                text = name
                textSize = 19f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#0F172A"))
                setPadding(0, 16, 0, 8)
            }
            cardContent.addView(titleView)

            // Bilgi Çubuğu (Süre, Zorluk, Kalori)
            val infoView = TextView(this).apply {
                text = "⏱️ $prepTime   •   👨‍🍳 $difficulty   •   🔥 $calories kcal"
                textSize = 13f
                setTextColor(Color.parseColor("#64748B"))
                setPadding(0, 0, 0, 16)
            }
            cardContent.addView(infoView)

            // Ayırıcı Çizgi
            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2
                ).apply { setMargins(0, 8, 0, 16) }
                setBackgroundColor(Color.parseColor("#F1F5F9"))
            }
            cardContent.addView(divider)

            // Malzemeler
            val ingredientsTitle = TextView(this).apply {
                text = "🛒 Malzemeler"
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#334155"))
            }
            cardContent.addView(ingredientsTitle)

            val ingredientsView = TextView(this).apply {
                text = ingredientsList.joinToString("\n") { "  • $it" }
                textSize = 13f
                setTextColor(Color.parseColor("#475569"))
                setPadding(0, 6, 0, 16)
            }
            cardContent.addView(ingredientsView)

            // Hazırlanışı (Adım Adım)
            if (instructionsList.isNotEmpty()) {
                val instructionsTitle = TextView(this).apply {
                    text = "👨‍🍳 Hazırlanışı"
                    textSize = 14f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.parseColor("#334155"))
                }
                cardContent.addView(instructionsTitle)

                val instructionsText = instructionsList.mapIndexed { idx, step ->
                    "${idx + 1}. $step"
                }.joinToString("\n\n")

                val instructionsView = TextView(this).apply {
                    text = instructionsText
                    textSize = 13f
                    setTextColor(Color.parseColor("#475569"))
                    setPadding(0, 6, 0, 0)
                }
                cardContent.addView(instructionsView)
            }

            card.addView(cardContent)
            container.addView(card)
        }
    }
}

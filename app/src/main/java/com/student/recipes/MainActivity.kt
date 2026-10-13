package com.student.recipes

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val container = findViewById<LinearLayout>(R.id.recipeContainer)

        try {
            val inputStream = assets.open("recipes.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonStr = String(buffer, Charset.forName("UTF-8"))

            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val recipe = jsonArray.getJSONObject(i)
                val name = recipe.getString("name")
                val calories = recipe.getInt("calories")
                val ingredients = recipe.getJSONArray("ingredients")
                
                val textView = TextView(this)
                textView.text = "🍽 $name ($calories kcal)\nMalzemeler: $ingredients\n\n"
                textView.textSize = 18f
                container.addView(textView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val errorView = TextView(this)
            errorView.text = "Tarifler yüklenirken hata oluştu!"
            container.addView(errorView)
        }
    }
}

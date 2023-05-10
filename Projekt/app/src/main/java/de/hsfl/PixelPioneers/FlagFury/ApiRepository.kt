package de.hsfl.PixelPioneers.FlagFury

import android.app.Application
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class ApiRepository private constructor(private val application: Application) {

    companion object {
        private var instance: ApiRepository? = null

        fun getInstance(application: Application): ApiRepository {
            if (instance == null) {
                instance = ApiRepository(application)
            }
            return instance!!
        }
    }

    fun registerGame(name: String, callback: (gameId: Int, token: String) -> Unit) {
        val url = "https://ctf.letorbi.de/game/register"

        val jsonRequest = JSONObject().apply {
            put("name", name)
            put("points", JSONArray())
        }

        val request = JsonObjectRequest(Request.Method.POST, url, jsonRequest,
            { response ->
                val gameId = response.getInt("game")
                val token = response.getString("token")
                Log.d("ApiRepository", "Game ID: $gameId, Token: $token")
                callback(gameId, token)
            },
            { error ->
                Log.e("error", "Fehler")
            })

        Volley.newRequestQueue(application).add(request)
    }


    fun joinGame(gameId: Int, name: String, callback: (gameId: Int, name: String, team: Int, token : String) -> Unit) {
        val url = "https://ctf.letorbi.de/game/join"

        val jsonRequest = JSONObject().apply {
            put("gameId", gameId)
            put("name", name)
            put("team", 0)
        }

        val request = JsonObjectRequest(Request.Method.POST, url, jsonRequest,
            { response ->
                val gameId = response.getInt("game")
                val name = response.getString("name")
                val team = response.getInt("team")
                val token = response.getString("token")
                Log.d("ApiRepository", "Game ID: $gameId, Name: $name, Team: $team, Token: $token")
                callback(gameId, name, team , token)
            },
            { error ->
                Log.e("error", "Fehler")
            })

        Volley.newRequestQueue(application).add(request)
    }
}


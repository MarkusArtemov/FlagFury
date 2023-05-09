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
                callback(gameId, token)
            },
            { error ->
                Log.e("error", "Daten nicht transferiert")
            })

        Volley.newRequestQueue(application).add(request)
    }

    fun joinGame(gameId: Int, playerName: String, team: Int, onSuccess: (response: JSONObject) -> Unit, onError: (error: String) -> Unit) {
        val url = "https://ctf.letorbi.de/game/join"
        val requestBody = JSONObject().apply {
            put("game", gameId)
            put("name", playerName)
            put("team", team)
        }

        val request: JsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, requestBody,
            { response ->
                onSuccess(response)
            },
            { error ->
                onError(error.message ?: "Unknown error occurred")
            }
        )

        Volley.newRequestQueue(application).add(request)
    }
}


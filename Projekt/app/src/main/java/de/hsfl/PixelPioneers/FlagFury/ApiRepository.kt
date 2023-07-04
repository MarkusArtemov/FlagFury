package de.hsfl.PixelPioneers.FlagFury

import android.app.Application
import android.util.Log
import com.android.volley.Request
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

    fun registerGame(name: String, points: List<Pair<Double, Double>>, callback: (gameId: String, token: String) -> Unit, errorCallback: (error: String?) -> Unit) {
        val url = "https://ctf.letorbi.de/game/register"

        val jsonArray = JSONArray()
        for (point in points) {
            val jsonObject = JSONObject()
            jsonObject.put("long", point.first)
            jsonObject.put("lat", point.second)
            jsonArray.put(jsonObject)
        }

        val jsonRequest = JSONObject().apply {
            put("name", name)
            put("points", jsonArray)
            Log.d("Api", "das sind die : $points")
        }

        val request = JsonObjectRequest(Request.Method.POST, url, jsonRequest,
            { response ->
                val gameId = response.getString("game")
                val token = response.getString("token")
                Log.d("ApiRepository", "Game ID: $gameId, Token: $token")
                callback(gameId, token)
            },
            { error ->
                errorCallback("Es ist zu einem Fehler gekommen")
            })

        Volley.newRequestQueue(application).add(request)
    }



    fun conquerPoint(game: String?, pointId: String?,team: String?,name: String?,token: String?,
                     callback: (obj : JSONObject?) -> Unit
                     ,errorCallback: (error: String?) -> Unit) {
        val url = "https://ctf.letorbi.de/point/conquer"

        val jsonRequest = JSONObject().apply {
            put("game", game)
            put("point", pointId)
            put("team", team)
            put("auth", JSONObject().apply {
                put("name", name)
                put("token", token)
            })
        }
        Log.d("Das hingesendet","$jsonRequest")

        val request = JsonObjectRequest(Request.Method.POST, url, jsonRequest,
            { response ->
                callback(response)
            },
            { error ->
                errorCallback("Es ist zu einem Fehler gekommen")
            })

        Volley.newRequestQueue(application).add(request)


    }


    fun joinGame(game: String, name: String, team: Int, callback: (team: Int, token : String) -> Unit, errorCallback: (error: String?) -> Unit) {
        val url = "https://ctf.letorbi.de/game/join"

        val jsonRequest = JSONObject().apply {
            put("game", game)
            put("name", name)
            put("team", team)
        }

        val request = JsonObjectRequest(Request.Method.POST, url, jsonRequest,
            { response ->
                val game = response.getString("game")
                val name = response.getString("name")
                val team = response.getInt("team")
                val token = response.getString("token")
                Log.d("ApiRepository", "Game ID: $game, Name: $name, Team: $team, Token: $token")
                callback(team , token)
            },
            { error ->
                errorCallback("Es ist zu einem Fehler gekommen")
            })

        Volley.newRequestQueue(application).add(request)
    }



    fun getPlayers(game: String?, name: String?, token: String?, callback: (players: JSONObject?) -> Unit,errorCallback: (error: String?) -> Unit) {
        val url = "https://ctf.letorbi.de/players"

        val jsonRequest = JSONObject().apply {
            put("game", game)
            put("auth", JSONObject().apply {
                put("name", name)
                put("token", token)
            })
        }
        val request = JsonObjectRequest(Request.Method.POST, url, jsonRequest,
            { response ->
                Log.d("ApiRepository", "Players: $response")
                callback(response)
            },
            { error ->
                    errorCallback("Es ist zu einem Fehler gekommen")
            })

        Volley.newRequestQueue(application).add(request)
    }

    fun startGame(game: String?, name: String?, token: String?,errorCallback: (error: String?) -> Unit) {
        val url = "https://ctf.letorbi.de/game/start"

        val jsonRequest = JSONObject().apply {
            put("game", game)
            put("auth", JSONObject().apply {
                put("name", name)
                put("token", token)
            })
        }
        val request = JsonObjectRequest(Request.Method.POST, url, jsonRequest,
            { response ->
            },
            { error ->
                errorCallback("Fehler beim Starten des Spiels")
            })

        Volley.newRequestQueue(application).add(request)
    }


    fun endGame(game: String?, name: String?, token: String?,errorCallback: (error: String?) -> Unit) {
        val url = "https://ctf.letorbi.de/game/end"

        val jsonRequest = JSONObject().apply {
            put("game", game)
            put("auth", JSONObject().apply {
                put("name", name)
                put("token", token)
            })
        }
        val request = JsonObjectRequest(Request.Method.POST, url, jsonRequest,
            { response ->
            },
            { error ->
                errorCallback("Es ist zu einem Fehler beim Beenden des Spiels gekommen")
            })

        Volley.newRequestQueue(application).add(request)
    }


    fun removePlayer(game: String?, name: String?, token: String?, callback: (game : String?, name : String?) -> Unit, errorCallback: (error: String?) -> Unit){

        val url = "https://ctf.letorbi.de/player/remove"

        val jsonRequest = JSONObject().apply {
            put("game", game)
            put("name", name)
            put("auth", JSONObject().apply {
                put("name", name)
                put("token", token)
            })
        }
        val request = JsonObjectRequest(Request.Method.POST, url, jsonRequest,
            { response ->
                Log.d("ApiRepository", "Removed Player: $response")
                callback(response.getString("game"), response.getString("name"))
            },
            { error ->
                errorCallback("Es ist zu einem Fehler gekommen")
            })

        Volley.newRequestQueue(application).add(request)
    }


    fun getPoints(game: String?, name: String?, token: String?, callback: (points: List<Point>?, state : String?, game : String?) -> Unit, errorCallback: (error: String?) -> Unit) {
        val url = "https://ctf.letorbi.de/points"

        val jsonRequest = JSONObject().apply {
            put("game", game)
            put("auth", JSONObject().apply {
                put("name", name)
                put("token", token)
            })
        }

        val request = JsonObjectRequest(Request.Method.POST, url, jsonRequest,
            { response ->
                val state = response.getString("state")
                val game = response.getString("game")

                val pointsJsonArray = response.getJSONArray("points")
                val points = mutableListOf<Point>()

                for (i in 0 until pointsJsonArray.length()) {
                    val pointObject = pointsJsonArray.getJSONObject(i)
                    val id = pointObject.getString("id")
                    val team = pointObject.getInt("team")
                    val lat = pointObject.getDouble("lat")
                    val long = pointObject.getDouble("long")
                    val point = Point(id, team, lat, long)
                    points.add(point)
                }
                callback(points,state, game)
            },
            { error ->
                errorCallback("Fehler beim Abrufen der Eroberungspunkte")
            })

        Volley.newRequestQueue(application).add(request)
    }



}


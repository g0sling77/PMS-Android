package com.pms.data

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom

data class ServerConnection(
    val serverName: String,
    val version: String
)

data class MusicAlbum(
    val id: String,
    val name: String,
    val artist: String,
    val year: Int?,
    val songCount: Int
)

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int
)

object NavidromeClient {
    private const val API_VERSION = "1.16.1"
    private const val DEFAULT_PORT = 4533

    fun normalizeServerAddress(input: String): String {
        val value = input.trim().trimEnd('/')
        require(value.isNotBlank()) { "Введите IP-адрес сервера" }
        val withScheme = if (value.startsWith("http://", true) || value.startsWith("https://", true)) {
            value
        } else {
            "http://$value"
        }
        val schemeEnd = withScheme.indexOf("://") + 3
        val authorityEnd = withScheme.indexOfAny(charArrayOf('/', '?', '#'), startIndex = schemeEnd)
            .let { if (it == -1) withScheme.length else it }
        val authority = withScheme.substring(schemeEnd, authorityEnd)
        require(authority.isNotBlank()) { "Проверьте IP-адрес сервера" }
        val hasPort = (authority.startsWith("[") && authority.contains("]:")) ||
            (!authority.startsWith("[") && authority.count { it == ':' } == 1)
        val normalized = if (hasPort) {
            withScheme
        } else if (authority.startsWith("[") && authority.endsWith("]")) {
            withScheme.substring(0, authorityEnd).replace(authority, "$authority:$DEFAULT_PORT") +
                withScheme.substring(authorityEnd)
        } else {
            withScheme.substring(0, authorityEnd).replace(authority, "$authority:$DEFAULT_PORT") +
                withScheme.substring(authorityEnd)
        }
        return normalized.trimEnd('/')
    }

    fun testConnection(serverAddress: String, username: String, password: String): ServerConnection {
        val response = request(serverAddress, username, password, "ping.view")
        return ServerConnection(
            serverName = "Navidrome / Subsonic",
            version = response.optString("version", "не указана")
        )
    }

    fun getAlbums(serverAddress: String, username: String, password: String): List<MusicAlbum> {
        val response = request(
            serverAddress, username, password, "getAlbumList2.view",
            mapOf("type" to "alphabeticalByName", "size" to "500", "offset" to "0")
        )
        val list = response.optJSONObject("albumList2")
            ?: throw IllegalStateException("Сервер не вернул список альбомов")
        return jsonObjects(list.opt("album")).mapNotNull { item ->
            val id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            MusicAlbum(
                id = id,
                name = item.optString("name", "Без названия"),
                artist = item.optString("artist", "Неизвестный исполнитель"),
                year = item.optInt("year").takeIf { item.has("year") && !item.isNull("year") },
                songCount = item.optInt("songCount")
            )
        }
    }

    fun getStreamUrl(serverAddress: String, username: String, password: String, trackId: String): String {
        val base = normalizeServerAddress(serverAddress)
        require(username.isNotBlank()) { "Введите имя пользователя" }
        require(password.isNotEmpty()) { "Введите пароль" }
        val salt = randomSalt()
        val token = md5(password + salt)
        val endpoint = if (base.endsWith("/rest")) "$base/stream.view" else "$base/rest/stream.view"
        val parameters = linkedMapOf(
            "u" to username,
            "t" to token,
            "s" to salt,
            "v" to API_VERSION,
            "c" to "PMS",
            "id" to trackId
        )
        val query = parameters.entries.joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value)}"
        }
        return "$endpoint?$query"
    }

    fun getAlbum(serverAddress: String, username: String, password: String, albumId: String): List<MusicTrack> {
        val response = request(
            serverAddress, username, password, "getAlbum.view",
            mapOf("id" to albumId)
        )
        val album = response.optJSONObject("album")
            ?: throw IllegalStateException("Сервер не вернул данные альбома")
        return jsonObjects(album.opt("song")).mapNotNull { item ->
            val id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            MusicTrack(
                id = id,
                title = item.optString("title", "Без названия"),
                artist = item.optString("artist", "Неизвестный исполнитель"),
                durationSeconds = item.optInt("duration")
            )
        }
    }

    private fun request(
        serverAddress: String,
        username: String,
        password: String,
        method: String,
        parameters: Map<String, String> = emptyMap()
    ): JSONObject {
        val base = normalizeServerAddress(serverAddress)
        require(username.isNotBlank()) { "Введите имя пользователя" }
        require(password.isNotEmpty()) { "Введите пароль" }

        val salt = randomSalt()
        val token = md5(password + salt)
        val endpoint = if (base.endsWith("/rest")) "$base/$method" else "$base/rest/$method"
        val queryParameters = linkedMapOf(
            "u" to username,
            "t" to token,
            "s" to salt,
            "v" to API_VERSION,
            "c" to "PMS",
            "f" to "json"
        )
        queryParameters.putAll(parameters)
        val query = queryParameters.entries.joinToString("&") { (key, value) ->
            "${encode(key)}=${encode(value)}"
        }

        val connection = (URL("$endpoint?$query").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
        }

        return try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (body.isBlank()) {
                throw IllegalStateException("Сервер вернул пустой ответ (HTTP $responseCode)")
            }
            val root = JSONObject(body)
            val response = root.optJSONObject("subsonic-response")
                ?: throw IllegalStateException("Ответ не похож на Subsonic API")
            if (response.optString("status") != "ok") {
                val error = response.optJSONObject("error")
                val message = error?.optString("message")?.takeIf { it.isNotBlank() }
                    ?: "Сервер отклонил запрос"
                throw IllegalStateException(message)
            }
            response
        } finally {
            connection.disconnect()
        }
    }

    private fun jsonObjects(value: Any?): List<JSONObject> = when (value) {
        is JSONArray -> (0 until value.length()).mapNotNull { value.optJSONObject(it) }
        is JSONObject -> listOf(value)
        else -> emptyList()
    }

    private fun randomSalt(): String {
        val bytes = ByteArray(12)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun md5(value: String): String {
        val digest = MessageDigest.getInstance("MD5").digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")
}

package com.pms.data

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

object NavidromeClient {
    fun testConnection(
        serverAddress: String,
        username: String,
        password: String
    ): ServerConnection {
        val base = serverAddress.trim().trimEnd('/')
        require(base.startsWith("http://") || base.startsWith("https://")) {
            "Адрес должен начинаться с http:// или https://"
        }
        require(username.isNotBlank()) { "Введите имя пользователя" }
        require(password.isNotEmpty()) { "Введите пароль" }

        val salt = randomSalt()
        val token = md5(password + salt)
        val endpoint = if (base.endsWith("/rest")) {
            "$base/ping.view"
        } else {
            "$base/rest/ping.view"
        }

        val query = listOf(
            "u" to username,
            "t" to token,
            "s" to salt,
            "v" to "1.16.1",
            "c" to "PMS",
            "f" to "json"
        ).joinToString("&") { (key, value) ->
            "$key=${encode(value)}"
        }

        val connection = (URL("$endpoint?$query").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
        }

        try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (body.isBlank()) {
                throw IllegalStateException("Сервер вернул пустой ответ (HTTP $responseCode)")
            }

            val root = JSONObject(body)
            val response = root.optJSONObject("subsonic-response")
                ?: throw IllegalStateException("Ответ не похож на Subsonic API")

            if (response.optString("status") != "ok") {
                val error = response.optJSONObject("error")
                val message = error?.optString("message")
                    ?.takeIf { !it.isNullOrBlank() }
                    ?: "Сервер отклонил подключение"
                throw IllegalStateException(message)
            }

            ServerConnection(
                serverName = "Navidrome / Subsonic",
                version = response.optString("version", "не указана")
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun randomSalt(): String {
        val bytes = ByteArray(12)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun md5(value: String): String {
        val digest = MessageDigest.getInstance("MD5")
            .digest(value.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8")
}

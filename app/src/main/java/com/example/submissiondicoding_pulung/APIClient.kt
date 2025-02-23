package com.example.submissiondicoding_pulung

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object APIClient {
    private const val BASE_URL = "https://event-api.dicoding.dev/"

    fun api(endpoint: String, jsonBody: String? = null, method: String): String? {
        val url = URL(BASE_URL + endpoint)
        val connection = url.openConnection() as HttpURLConnection

        return try {
            connection.requestMethod = method
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (method in listOf("POST", "PUT", "DELETE")) {
                connection.doOutput = true
                connection.outputStream.use { outputStream ->
                    jsonBody?.let {
                        outputStream.write(it.toByteArray(Charsets.UTF_8))
                        outputStream.flush()
                    }
                }
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use(BufferedReader::readText)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

}

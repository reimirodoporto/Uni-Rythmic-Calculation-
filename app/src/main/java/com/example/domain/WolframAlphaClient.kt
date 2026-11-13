package com.example.domain

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class WolframPodResult(
    val title: String,
    val plainText: String,
    val origin: DomainOrigin
)

sealed interface WolframLookupState {
    data object Idle : WolframLookupState
    data object Loading : WolframLookupState
    data class Success(
        val query: String,
        val pods: List<WolframPodResult>,
        val primaryOrigin: DomainOrigin,
        val secondaryOrigin: DomainOrigin?,
        val webUrl: String
    ) : WolframLookupState

    data class MissingKey(
        val query: String,
        val webUrl: String
    ) : WolframLookupState

    data class Error(
        val message: String,
        val webUrl: String
    ) : WolframLookupState
}

object WolframAlphaClient {

    fun isConfigured(): Boolean {
        val key = BuildConfig.WOLFRAM_APP_ID.trim()
        return key.isNotEmpty() && key != "YOUR_WOLFRAM_APP_ID"
    }

    fun buildWebLookupUrl(query: String): String {
        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        return "https://www.wolframalpha.com/input?i=$encoded"
    }

    suspend fun queryLookup(rawQuery: String): WolframLookupState = withContext(Dispatchers.IO) {
        val q = rawQuery.trim()
        if (q.isEmpty()) return@withContext WolframLookupState.Idle
        val webUrl = buildWebLookupUrl(q)

        if (!isConfigured()) {
            return@withContext WolframLookupState.MissingKey(
                query = q,
                webUrl = webUrl
            )
        }

        runCatching {
            val encodedInput = URLEncoder.encode(q, "UTF-8")
            val encodedAppId = URLEncoder.encode(BuildConfig.WOLFRAM_APP_ID.trim(), "UTF-8")
            val endpoint = "https://api.wolframalpha.com/v2/query?input=$encodedInput&appid=$encodedAppId&output=json&format=plaintext"
            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 20_000
            }

            val status = conn.responseCode
            val stream = if (status in 200..299) conn.inputStream else conn.errorStream
            val body = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            conn.disconnect()

            if (status !in 200..299) {
                return@withContext WolframLookupState.Error(
                    message = "wolframalpha api returned http $status",
                    webUrl = webUrl
                )
            }

            val root = JSONObject(body).optJSONObject("queryresult")
                ?: return@withContext WolframLookupState.Error("invalid wolframalpha json response", webUrl)

            if (!root.optBoolean("success", false)) {
                val errMsg = root.optJSONObject("error")?.optString("msg")
                    ?: "no computational pods matched query"
                return@withContext WolframLookupState.Error(
                    message = sanitizePlain(errMsg),
                    webUrl = webUrl
                )
            }

            val podsArray = root.optJSONArray("pods")
            val parsedPods = mutableListOf<WolframPodResult>()
            if (podsArray != null) {
                for (i in 0 until podsArray.length()) {
                    val podObj = podsArray.optJSONObject(i) ?: continue
                    val title = podObj.optString("title", "RESULT").uppercase()
                    val subpods = podObj.optJSONArray("subpods") ?: continue
                    val textBuilder = StringBuilder()
                    for (j in 0 until subpods.length()) {
                        val sub = subpods.optJSONObject(j) ?: continue
                        val plain = sub.optString("plaintext", "").trim()
                        if (plain.isNotEmpty()) {
                            if (textBuilder.isNotEmpty()) textBuilder.append("\n")
                            textBuilder.append(plain)
                        }
                    }
                    val combined = textBuilder.toString().trim()
                    if (combined.isNotEmpty()) {
                        parsedPods.add(
                            WolframPodResult(
                                title = title,
                                plainText = combined,
                                origin = classifyOrigin("$q $title $combined")
                            )
                        )
                    }
                }
            }

            if (parsedPods.isEmpty()) {
                return@withContext WolframLookupState.Error("wolframalpha returned empty pods", webUrl)
            }

            val primaryOrigin = classifyOrigin(q)
            val secondary = parsedPods.firstOrNull { it.origin != primaryOrigin }?.origin

            WolframLookupState.Success(
                query = q,
                pods = parsedPods.take(6),
                primaryOrigin = primaryOrigin,
                secondaryOrigin = secondary,
                webUrl = webUrl
            )
        }.getOrElse { err ->
            WolframLookupState.Error(
                message = sanitizePlain(err.localizedMessage ?: "wolframalpha network error"),
                webUrl = webUrl
            )
        }
    }

    fun classifyOrigin(text: String): DomainOrigin {
        val lower = text.lowercase()
        return when {
            listOf("planck", "quantum", "hbar", "cesium", "hyperfine", "electron", "proton", "photon", "schrodinger")
                .any { lower.contains(it) } -> DomainOrigin.QUANTUM
            listOf("boltzmann", "kelvin", "entropy", "thermo", "heat", "temperature", "joule")
                .any { lower.contains(it) } -> DomainOrigin.THERMO
            listOf("parsec", "light year", "astronomical", "galaxy", "hubble", "cosmos", "solar", "orbit")
                .any { lower.contains(it) } -> DomainOrigin.COSMOLOGY
            listOf("phi", "fibonacci", "prime", "mertens", "mobius", "integral", "derivative", "matrix", "tensor", "ratio", "sqrt")
                .any { lower.contains(it) } -> DomainOrigin.MATH
            listOf("meter", "kilogram", "newton", "gravity", "velocity", "speed of light", "ampere", "coulomb", "force", "mass")
                .any { lower.contains(it) } -> DomainOrigin.PHYSICS
            else -> DomainOrigin.GENERAL
        }
    }

    private fun sanitizePlain(raw: String): String {
        return raw
            .replace(Regex("[#*\"_()\\[\\]{}<>~`•—–\\-.]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .lowercase()
    }
}

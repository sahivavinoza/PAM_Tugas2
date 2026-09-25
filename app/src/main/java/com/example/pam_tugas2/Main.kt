package com.example.pam_tugas2

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class NewsItem(
    val id: Int,
    val title: String,
    val category: String,
    val contentPreview: String
)

data class FormattedNews(
    val displayId: String,
    val headline: String,
    val tag: String
)

class NewsFeedSimulator {
    private val _readCount = MutableStateFlow(0)
    val readCount: StateFlow<Int> = _readCount.asStateFlow()

    private val newsDatabase = listOf(
        NewsItem(1, "Rilis Pembaruan Fitur Kotlin 2.0", "Tech", "Fitur compiler K2 semakin cepat..."),
        NewsItem(2, "Klasemen Terbaru Liga Inggris Pekan Ini", "Sport", "Persaingan papan atas memanas..."),
        NewsItem(3, "Pemanfaatan AI Generatif di Aplikasi Mobile", "Tech", "Pengembang mulai adopsi model lokal..."),
        NewsItem(4, "Pola Tidur Sehat untuk Produktivitas Programmer", "Health", "Istirahat cukup mencegah burnout..."),
        NewsItem(5, "Arsitektur Modern Compose Multiplatform 2026", "Tech", "UI deklaratif lintas platform kian matang...")
    )

    fun getNewsStream(): Flow<NewsItem> = flow {
        for (news in newsDatabase) {
            delay(2000) // Emisi tiap 2 detik
            emit(news)
        }
    }

    suspend fun fetchNewsDetailAsync(newsId: Int): String = coroutineScope {
        val detailDeferred = async(Dispatchers.Default) {
            println("--> [Async Loading] Mengambil detail berita ID #$newsId dari server...")
            delay(1000) // Simulasi jeda network 1 detik
            "Detail lengkap berita ID #$newsId berhasil dimuat."
        }
        detailDeferred.await()
    }

    // Fungsi menambah counter berita dibaca
    fun markAsRead() {
        _readCount.value += 1
    }
}

fun main() = runBlocking {
    val simulator = NewsFeedSimulator()
    val targetCategory = "Tech"

    println("==================================================")
    println("      SIMULATOR NEWS FEED (KOTLIN FLOW & COROUTINE)")
    println("==================================================")
    println("Nama Mahasiswa : Sahiva Syamdo Vinoza")
    println("NIM            : 123140194")
    println("Filter Kategori: [$targetCategory]")
    println("Status Awal Dibaca: ${simulator.readCount.value}\n")

    val stateFlowObserver = launch {
        simulator.readCount.collect { count ->
            println("[StateFlow Notification] Total dibaca saat ini: $count")
        }
    }

    simulator.getNewsStream()
        .filter { it.category.equals(targetCategory, ignoreCase = true) }

        .map { news ->
            FormattedNews(
                displayId = "#NEWS-${news.id}",
                headline = news.title.uppercase(),
                tag = "[${news.category}]"
            )
        }
        .collect { formatted ->
            println("\n--------------------------------------------------")
            println("BERITA BARU MASUK!")
            println("ID   : ${formatted.displayId}")
            println("Judul: ${formatted.headline}")
            println("Tag  : ${formatted.tag}")

            val rawId = formatted.displayId.removePrefix("#NEWS-").toInt()

            val detail = simulator.fetchNewsDetailAsync(rawId)
            println("Hasil: $detail")

            simulator.markAsRead()
        }

    println("\n==================================================")
    println("Aliran data selesai. Total berita dibaca: ${simulator.readCount.value}")
    println("==================================================")

    stateFlowObserver.cancel()
}
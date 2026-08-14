package com.videoqa.challenge.util

data class VideoCard(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val publishedDate: String,
    val duration: String
)

object TestFixtures {
    object Videos {
        val AmsterdamFromAbove = VideoCard(
            id = "amsterdam",
            title = "Amsterdam from above",
            category = "Travel",
            description = "Explore Amsterdam and its surroundings from a different perspective.",
            publishedDate = "15 July 2026",
            duration = "02:30"
        )
        val WeekendTravelGuide = VideoCard(
            id = "travel",
            title = "Weekend travel guide",
            category = "Travel",
            description = "Short trips and hidden gems for your next weekend away.",
            publishedDate = "18 July 2026",
            duration = "03:30"
        )
    }
}
package com.example.myfilimapp.model.request

enum class EqPreset(val label: String, val gains: List<Float>) {
    FLAT     ("Flat",      listOf( 0f,  0f,  0f,  0f,  0f)),
    ROCK     ("Rock",      listOf( 5f,  3f, -1f,  3f,  5f)),
    JAZZ     ("Jazz",      listOf( 3f,  2f,  0f,  2f,  3f)),
    CLASSICAL("Classical", listOf( 4f,  3f, -2f,  3f,  4f)),
    POP      ("Pop",       listOf(-1f,  3f,  5f,  3f, -1f)),
    VOCAL    ("Vocal",     listOf(-3f,  0f,  5f,  4f,  2f))
}
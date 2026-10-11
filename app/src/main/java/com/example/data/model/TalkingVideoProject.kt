package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "talking_video_projects")
data class TalkingVideoProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val avatarSource: String, // "res:anchor", "res:cyber", or file/content URI
    val scriptText: String,
    val languageCode: String = "fr-FR",
    val voiceTone: String = "Naturel",
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val videoFilterStyle: String = "Studio", // Studio, Cyberpunk, Vintage, Hologram, Glow
    val bgAmbiance: String = "Aucun", // Aucun, JT Info, Lo-Fi, Épique
    val audioPath: String? = null,
    val estimatedDurationSec: Int = 5,
    val createdAt: Long = System.currentTimeMillis()
)

package de.yanos.islam.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class VideoLearning(
    @PrimaryKey val id: String,
    val index: Int,
    val remoteUrl: String,
    val thumbRemoteUrl: String,
    val title: String,
    val description: String,
    val author: String
)
package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TalkingVideoProject
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM talking_video_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<TalkingVideoProject>>

    @Query("SELECT * FROM talking_video_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): TalkingVideoProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: TalkingVideoProject): Long

    @Update
    suspend fun updateProject(project: TalkingVideoProject)

    @Delete
    suspend fun deleteProject(project: TalkingVideoProject)

    @Query("DELETE FROM talking_video_projects WHERE id = :id")
    suspend fun deleteById(id: Long)
}

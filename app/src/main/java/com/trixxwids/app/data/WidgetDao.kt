package com.trixxwids.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetDao {

    @Query("SELECT * FROM widgets ORDER BY updatedAt DESC")
    fun getAllWidgets(): Flow<List<WidgetEntity>>

    @Query("SELECT * FROM widgets WHERE id = :id")
    suspend fun getWidgetById(id: Long): WidgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWidget(widget: WidgetEntity): Long

    @Update
    suspend fun updateWidget(widget: WidgetEntity)

    @Delete
    suspend fun deleteWidget(widget: WidgetEntity)

    @Query("DELETE FROM widgets WHERE id = :id")
    suspend fun deleteWidgetById(id: Long)
}

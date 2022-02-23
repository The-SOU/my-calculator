package com.sou.mycalculator.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.sou.mycalculator.model.History

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history")
    fun getAll(): List<History>

    @Insert
    fun insertHistory(history: History)

    @Query("DELETE FROM history")
    fun deleteAll()

//    @Delete
//    fun delete(history: History)
//
//    //객체 값에 따른 데이터 불러오기
//    @Query("SELECT * FROM history WHERE result LIKE result LIMIT 1")
//    fun getByResult(result: String): History

}
package com.sou.mycalculator

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sou.mycalculator.dao.HistoryDao
import com.sou.mycalculator.model.History

@Database(entities = [History::class], version = 1)
abstract class AppDatabase: RoomDatabase(){
    abstract fun historyDao() : HistoryDao

}
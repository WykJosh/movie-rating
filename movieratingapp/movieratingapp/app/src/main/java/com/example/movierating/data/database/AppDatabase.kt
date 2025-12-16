package com.example.movierating.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// pre creates users on start
@Database(
    entities = [MovieEntity::class, UserEntity::class, RatingEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun userDao(): UserDao
    abstract fun ratingDao(): RatingDao
    companion object {
        @Volatile // means to read and write this variable from main memory and not cache ot have dbs up to date always
        private var INSTANCE: AppDatabase? = null // instanc =  Single Database Copy holds one dbs in entire app

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "movie_rating_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance // saves instance globally

                // this precreates user 1 and user2 and tokens manually inputted
                instance.openHelper.writableDatabase.execSQL(
                    """
                    INSERT OR IGNORE INTO users (id, username, token) VALUES 
                    (1, 'user1', 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.user1'),
                    (2, 'user2', 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.user2')
                """
                )
                instance // this returns insance to the caller
            }
        }
    }
}

//App starts → No database file exists (INSTANCE = null)
//User opens login → getDatabase() → Creates file ONCE → INSTANCE = database
//MovieList opens → getDatabase() → Returns SAME database

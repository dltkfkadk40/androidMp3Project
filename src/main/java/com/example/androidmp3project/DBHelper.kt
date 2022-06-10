package com.example.androidmp3project

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DBHelper(context: Context?, name: String?, version: Int): SQLiteOpenHelper(context, name, null, version) {
    //DB테이블 생성
    override fun onCreate(db: SQLiteDatabase?) {
        var createQuery = """
                create table musicsFile(
                id TEXT primary key,
                title TEXT ,
                artist TEXT,
                albumId TEXT,
                duration INTEGER,
                good INTEGER,
                bad INTEGER) 
                """.trimIndent()
        db?.execSQL(createQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        var dropQuery ="drop table if exists musicsFile"
        db?.execSQL(dropQuery)
        onCreate(db)
    }
    
    fun insertMusic(musicData: MusicData): Boolean{
        var insertFlag = false
        val db:SQLiteDatabase = this.writableDatabase

        try{
            val insertQuery = "INSERT INTO musicsFile(id, title, artist, albumId, duration, good, bad) VALUES ('${musicData.id}', '${musicData.title}', '${musicData.artist}', '${musicData.albumId}', '${musicData.duration}', '${musicData.good}', '${musicData.bad}')"
            db.execSQL(insertQuery)
            insertFlag = true
        }catch (e:SQLException){
            e.printStackTrace()
        }finally {
            db.close()
        }
        return insertFlag
    }
    //검색: SearchView 글자를 입력할때 제목과 아티스트 검색 하면 찾아지게 설정
    fun selectMusic(title: String, artist: String): MutableList<MusicData>?{
        var musicDataList:MutableList<MusicData>? = mutableListOf<MusicData>()
        val db: SQLiteDatabase =this.readableDatabase

        val selectQuery = "select * from musicsFile where title like '%${title}%' or artist like '%${artist}%'"
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery,null)
            if(cursor.count > 0){
                while(cursor.moveToNext()){
                    val id = cursor.getString(0)
                    val title = cursor.getString(1).replace("'"," ")
                    val artist = cursor.getString(2).replace("'"," ")
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val good = cursor.getInt(5)
                    val bad = cursor.getInt(6)

                    musicDataList?.add(MusicData(id, title, artist, albumId, duration, good, bad))
                }
            }
        }catch (e: SQLException){
            e.printStackTrace()
        }finally {
            cursor?.close()
            db.close()
        }
        return musicDataList
    }
    
    //테이블에 있는 모든정보 보여줌
    fun selectAllMusic(): MutableList<MusicData>?{
        var musicDataList:MutableList<MusicData>? = mutableListOf<MusicData>()
        val db: SQLiteDatabase = this.readableDatabase
        val selectAllQuery = "select * from musicsFile"
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectAllQuery, null)
            if(cursor.count > 0){
                while(cursor.moveToNext()){
                    val id = cursor.getString(0)
                    val title = cursor.getString(1).replace("'"," ")
                    val artist = cursor.getString(2).replace("'"," ")
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    var good = cursor.getInt(5)
                    var bad = cursor.getInt(6)

                    musicDataList?.add(MusicData(id, title, artist, albumId, duration, good, bad))
                }
            }else{
                musicDataList = null
            }
        }catch (e:SQLException){
            e.printStackTrace()
            musicDataList = null
        }finally {
            cursor?.close()
            db.close()
        }
        return musicDataList
    }
    
    //좋아요를 터치하였을때 1로 업데이틑 시켜줌
    fun updateGood(musicData: MusicData): Boolean {
        var updateGoodFlag = false
        val db = this.readableDatabase

        var updateQuery: String = """
            update musicsFile set good = '${musicData.good}' where id = '${musicData.id}'
        """.trimIndent()
        try {
            db.execSQL(updateQuery)
            updateGoodFlag = true
        }catch (e:SQLException){
            e.printStackTrace()
        }
        return updateGoodFlag
    }

    //좋아요가 1이면 찾아줌
    fun selectGood(): MutableList<MusicData>? {
        var musicDataList:MutableList<MusicData>? = mutableListOf<MusicData>()

        val db: SQLiteDatabase =this.readableDatabase

        val selectQuery = "select * from musicsFile where good = 1"
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery,null)
            if(cursor.count > 0){
                while(cursor.moveToNext()){
                    val id = cursor.getString(0)
                    val title = cursor.getString(1).replace("'"," ")
                    val artist = cursor.getString(2).replace("'"," ")
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val good = cursor.getInt(5)
                    val bad = cursor.getInt(6)

                    musicDataList?.add(MusicData(id, title, artist, albumId, duration, good, bad))
                }
            }
        }catch (e: SQLException){
            e.printStackTrace()
        }finally {
            cursor?.close()
            db.close()
        }
        return musicDataList
    }
    //나빠요를 터치하였을때 1로 업데이틑 시켜줌
    fun updateBad(musicData: MusicData): Boolean {
        var updateBadFlag = false
        val db = this.readableDatabase

        var updateQuery: String = """
            update musicsFile set bad = '${musicData.bad}' where id = '${musicData.id}'
        """.trimIndent()
        try {
            db.execSQL(updateQuery)
            updateBadFlag = true
        }catch (e:SQLException){
            e.printStackTrace()
        }
        return updateBadFlag
    }

    fun selectBad(): MutableList<MusicData>? {
        var musicDataList:MutableList<MusicData>? = mutableListOf<MusicData>()

        val db: SQLiteDatabase =this.readableDatabase

        val selectQuery = "select * from musicsFile where bad = 1"
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery,null)
            if(cursor.count > 0){
                while(cursor.moveToNext()){
                    val id = cursor.getString(0)
                    val title = cursor.getString(1).replace("'"," ")
                    val artist = cursor.getString(2).replace("'"," ")
                    val albumId = cursor.getString(3)
                    val duration = cursor.getInt(4)
                    val good = cursor.getInt(5)
                    val bad = cursor.getInt(6)

                    musicDataList?.add(MusicData(id, title, artist, albumId, duration, good, bad))
                }
            }
        }catch (e: SQLException){
            e.printStackTrace()
        }finally {
            cursor?.close()
            db.close()
        }
        return musicDataList
    }
}
package com.example.riberasplayer.model


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

// Constantes para la base de datos
private const val DATABASE_NAME = "RiberasPlayerDB"
private const val DATABASE_VERSION = 1

// Tabla canciones
private const val TABLE_SONGS = "songs"
private const val COL_SONG_ID = "id"
private const val COL_TITLE = "title"
private const val COL_ARTIST = "artist"
private const val COL_ALBUM = "album"
private const val COL_DURATION = "duration"
private const val COL_PATH = "path"

// Tabla playlists
private const val TABLE_PLAYLISTS = "playlists"
private const val COL_PLAYLIST_ID = "id"
private const val COL_PLAYLIST_NAME = "name"
private const val COL_CREATED_AT = "created_at"

// Tabla intermedia: canciones por playlist
private const val TABLE_PLAYLIST_SONGS = "playlist_songs"
private const val COL_POSITION = "position"

class MusicDatabaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Crear tabla de canciones
        val createSongsTable = """
            CREATE TABLE $TABLE_SONGS (
                $COL_SONG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_ARTIST TEXT,
                $COL_ALBUM TEXT,
                $COL_DURATION INTEGER,
                $COL_PATH TEXT NOT NULL UNIQUE
            )
        """.trimIndent()

        // Crear tabla de playlists
        val createPlaylistsTable = """
            CREATE TABLE $TABLE_PLAYLISTS (
                $COL_PLAYLIST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PLAYLIST_NAME TEXT NOT NULL UNIQUE,
                $COL_CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        // Crear tabla intermedia playlist_songs
        val createPlaylistSongsTable = """
            CREATE TABLE $TABLE_PLAYLIST_SONGS (
                $COL_PLAYLIST_ID INTEGER NOT NULL,
                $COL_SONG_ID INTEGER NOT NULL,
                $COL_POSITION INTEGER DEFAULT 0,
                PRIMARY KEY ($COL_PLAYLIST_ID, $COL_SONG_ID),
                FOREIGN KEY ($COL_PLAYLIST_ID) REFERENCES $TABLE_PLAYLISTS($COL_PLAYLIST_ID) ON DELETE CASCADE,
                FOREIGN KEY ($COL_SONG_ID) REFERENCES $TABLE_SONGS($COL_SONG_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createSongsTable)
        db.execSQL(createPlaylistsTable)
        db.execSQL(createPlaylistSongsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PLAYLIST_SONGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PLAYLISTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SONGS")
        onCreate(db)
    }

    // Operaciones para canciones

    fun addSong(song: Song): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, song.title)
            put(COL_ARTIST, song.artist)
            put(COL_ALBUM, song.album)
            put(COL_DURATION, song.duration)
            put(COL_PATH, song.path)
        }

        val id = db.insert(TABLE_SONGS, null, values)
        db.close()
        return id
    }

    @SuppressLint("Range")
    fun getSong(id: Int): Song? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_SONGS,
            arrayOf(COL_SONG_ID, COL_TITLE, COL_ARTIST, COL_ALBUM, COL_DURATION, COL_PATH),
            "$COL_SONG_ID = ?",
            arrayOf(id.toString()),
            null, null, null, null
        )

        return if (cursor.moveToFirst()) {
            val song = Song(
                cursor.getInt(cursor.getColumnIndex(COL_SONG_ID)),
                cursor.getString(cursor.getColumnIndex(COL_TITLE)),
                cursor.getString(cursor.getColumnIndex(COL_ARTIST)),
                cursor.getString(cursor.getColumnIndex(COL_ALBUM)),
                cursor.getLong(cursor.getColumnIndex(COL_DURATION)),
                cursor.getString(cursor.getColumnIndex(COL_PATH))
            )
            cursor.close()
            song
        } else {
            cursor.close()
            null
        }
    }

    @SuppressLint("Range")
    fun getAllSongs(): List<Song> {
        val songList = mutableListOf<Song>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_SONGS", null)

        if (cursor.moveToFirst()) {
            do {
                val song = Song(
                    cursor.getInt(cursor.getColumnIndex(COL_SONG_ID)),
                    cursor.getString(cursor.getColumnIndex(COL_TITLE)),
                    cursor.getString(cursor.getColumnIndex(COL_ARTIST)),
                    cursor.getString(cursor.getColumnIndex(COL_ALBUM)),
                    cursor.getLong(cursor.getColumnIndex(COL_DURATION)),
                    cursor.getString(cursor.getColumnIndex(COL_PATH))
                )
                songList.add(song)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return songList
    }

    fun updateSong(song: Song): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, song.title)
            put(COL_ARTIST, song.artist)
            put(COL_ALBUM, song.album)
            put(COL_DURATION, song.duration)
            put(COL_PATH, song.path)
        }

        val rowsAffected = db.update(
            TABLE_SONGS,
            values,
            "$COL_SONG_ID = ?",
            arrayOf(song.id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun deleteSong(songId: Int): Int {
        val db = this.writableDatabase
        val rowsDeleted = db.delete(
            TABLE_SONGS,
            "$COL_SONG_ID = ?",
            arrayOf(songId.toString())
        )
        db.close()
        return rowsDeleted
    }

    // Operaciones para playlists

    fun createPlaylist(name: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_PLAYLIST_NAME, name)
        }

        val id = db.insert(TABLE_PLAYLISTS, null, values)
        db.close()
        return id
    }

    @SuppressLint("Range")
    fun getPlaylist(id: Int): Playlist? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_PLAYLISTS,
            arrayOf(COL_PLAYLIST_ID, COL_PLAYLIST_NAME, COL_CREATED_AT),
            "$COL_PLAYLIST_ID = ?",
            arrayOf(id.toString()),
            null, null, null, null
        )

        return if (cursor.moveToFirst()) {
            val playlist = Playlist(
                cursor.getInt(cursor.getColumnIndex(COL_PLAYLIST_ID)),
                cursor.getString(cursor.getColumnIndex(COL_PLAYLIST_NAME)),
                cursor.getString(cursor.getColumnIndex(COL_CREATED_AT))
            )
            cursor.close()
            playlist
        } else {
            cursor.close()
            null
        }
    }

    @SuppressLint("Range")
    fun getAllPlaylists(): List<Playlist> {
        val playlistList = mutableListOf<Playlist>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PLAYLISTS", null)

        if (cursor.moveToFirst()) {
            do {
                val playlist = Playlist(
                    cursor.getInt(cursor.getColumnIndex(COL_PLAYLIST_ID)),
                    cursor.getString(cursor.getColumnIndex(COL_PLAYLIST_NAME)),
                    cursor.getString(cursor.getColumnIndex(COL_CREATED_AT))
                )
                playlistList.add(playlist)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return playlistList
    }

    fun updatePlaylist(playlist: Playlist): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_PLAYLIST_NAME, playlist.name)
        }

        val rowsAffected = db.update(
            TABLE_PLAYLISTS,
            values,
            "$COL_PLAYLIST_ID = ?",
            arrayOf(playlist.id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun deletePlaylist(playlistId: Int): Int {
        val db = this.writableDatabase
        val rowsDeleted = db.delete(
            TABLE_PLAYLISTS,
            "$COL_PLAYLIST_ID = ?",
            arrayOf(playlistId.toString())
        )
        db.close()
        return rowsDeleted
    }

    // Operaciones para canciones en playlists

    fun addSongToPlaylist(playlistId: Int, songId: Int, position: Int = 0): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_PLAYLIST_ID, playlistId)
            put(COL_SONG_ID, songId)
            put(COL_POSITION, position)
        }

        val id = db.insert(TABLE_PLAYLIST_SONGS, null, values)
        db.close()
        return id
    }

    @SuppressLint("Range")
    fun getSongsInPlaylist(playlistId: Int): List<Song> {
        val songList = mutableListOf<Song>()
        val db = this.readableDatabase
        val query = """
            SELECT s.* FROM $TABLE_SONGS s
            JOIN $TABLE_PLAYLIST_SONGS ps ON s.$COL_SONG_ID = ps.$COL_SONG_ID
            WHERE ps.$COL_PLAYLIST_ID = ?
            ORDER BY ps.$COL_POSITION
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(playlistId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val song = Song(
                    cursor.getInt(cursor.getColumnIndex(COL_SONG_ID)),
                    cursor.getString(cursor.getColumnIndex(COL_TITLE)),
                    cursor.getString(cursor.getColumnIndex(COL_ARTIST)),
                    cursor.getString(cursor.getColumnIndex(COL_ALBUM)),
                    cursor.getLong(cursor.getColumnIndex(COL_DURATION)),
                    cursor.getString(cursor.getColumnIndex(COL_PATH))
                )
                songList.add(song)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return songList
    }

    fun removeSongFromPlaylist(playlistId: Int, songId: Int): Int {
        val db = this.writableDatabase
        val rowsDeleted = db.delete(
            TABLE_PLAYLIST_SONGS,
            "$COL_PLAYLIST_ID = ? AND $COL_SONG_ID = ?",
            arrayOf(playlistId.toString(), songId.toString())
        )
        db.close()
        return rowsDeleted
    }

    fun updateSongPositionInPlaylist(playlistId: Int, songId: Int, newPosition: Int): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_POSITION, newPosition)
        }

        val rowsAffected = db.update(
            TABLE_PLAYLIST_SONGS,
            values,
            "$COL_PLAYLIST_ID = ? AND $COL_SONG_ID = ?",
            arrayOf(playlistId.toString(), songId.toString())
        )
        db.close()
        return rowsAffected
    }


}
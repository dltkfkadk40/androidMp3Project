package com.example.androidmp3project

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import java.io.IOException
import java.io.Serializable

class MusicData(
    id: String,
    title: String?,
    artist: String?,
    albumId: String?,
    duration: Int?,
    good: Int?,
    bad: Int?
):Serializable {

    var id: String = ""
    var title:String? = null
    var artist: String? = null
    var albumId: String? = null
    var duration: Int? = 0
    var good: Int? = 0
    var bad: Int? = 0

    init{
        this.id = id
        this.title = title
        this.artist = artist
        this.albumId = albumId
        this.duration = duration
        this.good = good
        this.bad = bad
    }

    fun getAlbumFileUri(): Uri {
        return Uri.parse("content://media/external/audio/albumart/"+ albumId)
    }

    //음악정보를 가져오기 위한 경로 Uri 얻기 (음악정보)
    fun getMusicFileUri(): Uri{
        return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
    }

    //해당되는 음악에 이미지를 내가 원하는 사이즈로 비트맵 만들어 돌려주기
    fun getAlbumImage(context: Context, albumImageSize: Int): Bitmap?{
        val contentResolver: ContentResolver = context.getContentResolver()
        //앨범경로
        val uri = getAlbumFileUri()
        //앨범에 대한 정보를 저장하기 위한 경로
        val options = BitmapFactory.Options()

        if(uri != null){
            var parcelFileDescriptor: ParcelFileDescriptor? = null
            try {
                //외부파일에 있는 이미지파일을 가져오기 위한 스트림
                parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")

                var bitmap = BitmapFactory.decodeFileDescriptor(parcelFileDescriptor!!.fileDescriptor, null, options)

                // 비트맵을 가져와서 이미지사이즈 결정(원본 이미지 사이즈가 내가 원하는 사이즈하고 맞지 않을경우 원하는 사이즈로 가져온다.)
                if(bitmap != null){
                    if(options.outHeight !== albumImageSize|| options.outWidth !== albumImageSize){
                        val tempBitmap = Bitmap.createScaledBitmap(bitmap, albumImageSize, albumImageSize, true)
                        bitmap.recycle()
                        bitmap = tempBitmap
                    }
                }
                return bitmap
            }catch (e: Exception){
                Log.d("shin", "getAlbumImage() ${e.toString()}")
            }finally {
                try {
                    parcelFileDescriptor?.close()
                }catch (e: IOException){
                    Log.d("shin","parcelFileDescriptor?.close() ${e.toString()}")
                }
            }
        }
        return null
    }
}
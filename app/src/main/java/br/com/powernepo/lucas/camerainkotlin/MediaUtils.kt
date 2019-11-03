package br.com.powernepo.lucas.camerainkotlin

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.graphics.rotationMatrix
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.time.LocalDate
import java.util.*

object MediaUtils {
    const val REQUEST_CODE_PHOTO = 1
    const val REQUEST_CODE_VIDEO = 2
    const val REQUEST_CODE_AUDIO = 3
    const val PROVIDER_AUTHORITY = "br.com.powernepo.lucas.camerainkotlin.fileprovider"

    private const val PREFERENCE_MEDIA = "media_prefs"
    private const val MEDIA_FOLDER = "KotlinPhotos"

    enum class MediaType(val extension:String, val preferenceKey:String){
        MEDIA_PHOTO(".jpg", "PHOTO_KEY"),
        MEIDA_VIDEO(".mp4","VIDEO_KEY"),
        MEDIA_AUDIO(".3gp","AUDIO_KEY")
    }

    private fun rotateImage(bitmap: Bitmap, angle: Float): Bitmap{
        val matrix = Matrix()
        matrix.postRotate(angle)

        return Bitmap.createBitmap(
            bitmap, 0 , 0, bitmap.width, bitmap.height, matrix, true
        )
    }

    private fun rotateImage(bitmap: Bitmap,filePath: String):Bitmap{
        var mBitmap = bitmap
    try {
        val exifInterface = ExifInterface(filePath)
        val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL)

        when(orientation){
            ExifInterface.ORIENTATION_ROTATE_90 -> mBitmap = rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> mBitmap = rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> mBitmap = rotateImage(bitmap, 270f)
        }
    }catch (e: IOException){
        e.printStackTrace()
    }
       return mBitmap
    }

    fun newNameMediaFile():String{
        val ct : DateFormat = DateFormat.getDateInstance()
        return ct.format("yyyy-MM-dd_hh:mm:ss").toString();
    }

    fun newMedia(baseConfigMedia: MediaType): File{
        val fileName = "PHOTO_ ${newNameMediaFile()}"
        val mediaDir = File(Environment.getExternalStorageDirectory(), MEDIA_FOLDER)

        if(!mediaDir.exists())
            if(!mediaDir.mkdirs())  throw IllegalArgumentException("Fail to create directories, please try again more later")

        return File(mediaDir, fileName + baseConfigMedia.extension)
    }

    fun saveLastMediaPath(context: Context,baseConfigMedia: MediaType, path: String) {
        // Save last file directory of user
        context.getSharedPreferences(PREFERENCE_MEDIA,Context.MODE_PRIVATE)
            .edit().putString(baseConfigMedia.preferenceKey,path).apply()
        context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
             Uri.parse(path)
        })
    }

    fun getLastMediaPath(context: Context, mediaType: MediaType):String?{
        return context.getSharedPreferences(PREFERENCE_MEDIA, Context.MODE_PRIVATE)
            .getString(mediaType.preferenceKey, null)
    }

    fun loadImage(file: File,width: Int,height: Int): Bitmap?{

        if(width ==0 || height == 0) return null

        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inJustDecodeBounds = true

        BitmapFactory.decodeFile(file.absolutePath, bitmapOptions)

        val fileWidth = bitmapOptions.outWidth
        val fileHeight = bitmapOptions.outHeight

        val scale = Math.min(fileWidth / width,fileHeight / height)

        bitmapOptions.inJustDecodeBounds = false
        bitmapOptions.inSampleSize = scale
        bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565

        var bitmap = BitmapFactory.decodeFile(file.absolutePath, bitmapOptions)
        return rotateImage(bitmap, file.absolutePath)
    }

    fun getVideoUri(context: Context): Uri{
        val newVideoFile = newMedia(MediaType.MEIDA_VIDEO)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            FileProvider.getUriForFile(context, PROVIDER_AUTHORITY, newVideoFile)
        else Uri.fromFile(newVideoFile)
    }

}

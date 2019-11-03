package br.com.powernepo.lucas.camerainkotlin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.FileProvider
import br.com.powernepo.lucas.camerainkotlin.MediaUtils.loadImage
import kotlinx.android.synthetic.main.fragment_camera_photo.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [CameraPhotoFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [CameraPhotoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CameraPhotoFragment : MultimediaFragment(), ViewTreeObserver.OnGlobalLayoutListener {

    private var mediaFile: File? = null
    private var mediaWidht: Int = 0
    private var mediaHeight: Int = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        retainInstance = true

        if(mediaFile == null){
            activity?.let {fragmentActivity ->
                val lastPath = MediaUtils.getLastMediaPath(fragmentActivity,MediaUtils.MediaType.MEDIA_PHOTO)
                if (lastPath != null){
                    mediaFile = File(lastPath)
                }

            }
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle? ): View? {
        val layout = inflater.inflate(R.layout.fragment_camera_photo, container, false)
        layout.viewTreeObserver.addOnGlobalLayoutListener(this)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imgButton.setOnClickListener {
            openCamera();
        }
    }

    private fun openCamera() {
        activity?.let {
            if(hasPermission()){
                try {
                    val newMediaFile = MediaUtils.newMedia(MediaUtils.MediaType.MEDIA_PHOTO)
                    mediaFile = newMediaFile

                    val photoUri = FileProvider.getUriForFile(it, MediaUtils.PROVIDER_AUTHORITY, newMediaFile)
                    val intent = Intent(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(intent, MediaUtils.REQUEST_CODE_PHOTO)
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }else requestPermission(RC_OPEN_CAMERA)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == MediaUtils.REQUEST_CODE_PHOTO
            && resultCode == Activity.RESULT_OK){
            oadImageInImageView();
        }
    }
    fun oadImageInImageView(){
        val file = mediaFile
        if (file?.exists() == true){
            if (hasPermission()){
                launch {
                    val bitmap = withContext(Dispatchers.IO){
                        loadImage(file,mediaWidht,mediaHeight)
                    }
                    imgPhoto.setImageBitmap(bitmap)
                    activity?.let {
                        MediaUtils.saveLastMediaPath(it, MediaUtils.MediaType.MEDIA_PHOTO, file.absolutePath)
                    }
                }
            }
        }else requestPermission(RC_LOAD_PHOTO)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.none{ it == PackageManager.PERMISSION_DENIED}){
            when(requestCode){
                RC_LOAD_PHOTO -> oadImageInImageView()
                RC_OPEN_CAMERA -> openCamera()
            }

        }
    }

    override fun onGlobalLayout() {

    }
    companion object{
        private const val RC_LOAD_PHOTO = 1
        private const val RC_OPEN_CAMERA = 2
    }
}

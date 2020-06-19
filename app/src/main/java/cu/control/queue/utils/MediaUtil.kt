package cu.control.queue.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.database.DatabaseUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import java.io.*
import java.util.*


object MediaUtil {

    private const val AUTHORITY = "com.ianhanniballake.localstorage.documents"
    private const val DEBUG = false

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getPath(context: Context, uri: Uri): String? {

        // check here to KITKAT or new version
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            when {
                DocumentsContract.isDocumentUri(context, uri) -> // LocalStorageProvider
                    when {
                        isLocalStorageDocument(uri) -> // The path is the id
                            return DocumentsContract.getDocumentId(uri)
                        isExternalStorageDocument(uri) -> {
                            val docId = DocumentsContract.getDocumentId(uri)
                            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                            val type = split[0]

                            return if ("primary".equals(type, ignoreCase = true)) {
                                try {
                                    Environment.getExternalStorageDirectory()
                                        .toString() + "/" + split[1]
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Environment.getExternalStorageDirectory().toString()
                                }
                            } else {
                                try {
                                    "/storage/" + split[0] + "/" + split[1]
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    "/storage/" + split[0]
                                }
                            }
                            // handle non-primary volumes
                        }
                        isDownloadsDocument(uri) -> {

                            val id = DocumentsContract.getDocumentId(uri)

                            val split = id.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()

                            return if (split.size > 1) {
                                split[1]
                            } else {
                                return getPathFromContent(context, uri)
                            }
                        }
                        isMediaDocument(uri) -> {
                            val docId = DocumentsContract.getDocumentId(uri)
                            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                            val type = split[0]
                            var contentUri: Uri? = null
                            when (type) {
                                "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                                "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            }
                            val selection = "_id=?"
                            val selectionArgs = arrayOf(split[1])
                            return getDataColumn(context, contentUri, selection, selectionArgs)
                        }
                    }
                "content".equals(uri.scheme!!, ignoreCase = true) -> // Return the remote address
                    return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                        context,
                        uri,
                        null,
                        null
                    )
                "file".equals(uri.scheme!!, ignoreCase = true) -> return uri.path
            }
            return null

        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            return getPathFromContent(context, uri)

        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return uri.toString()
    }

    fun getPathFromContent(context: Context, uri: Uri): String? {

        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        val mimeType = MimeUtils.guessMimeTypeFromUri(context, uri)
        val file = File(
            context.externalCacheDir, getFileNameByUri(context, uri)
                ?: getNewFileName(mimeType)
        )
        return if (IO.save(context, file.name, inputStream, file.outputStream())) {
            file.path
        } else {
            null
        }
    }

    fun getNewFileName(mimeType: String): String {
        return UUID.randomUUID().toString() + "." + mimeType
    }

    private fun getFileNameByUri(context: Context, uri: Uri): String? {
        var result: String? = null
        try {
            if (uri.scheme.equals("content")) {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor.use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        result =
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result?.lastIndexOf('/')
                if (cut != null && cut != -1) {
                    result = result?.substring(cut.plus(1))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun isLocal(url: String?): Boolean {
        return url != null && !url.startsWith("http://") && !url.startsWith("https://")
    }

    private fun isLocalStorageDocument(uri: Uri): Boolean {
        return AUTHORITY == uri.authority
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }


    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    @SuppressLint("Recycle")
    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        val column = "_data"
        val projection = arrayOf(column)

        context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)!!
            .use { cursor ->
                if (cursor.moveToFirst()) {
                    if (DEBUG)
                        DatabaseUtils.dumpCursor(cursor)

                    val column_index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(column_index)
                }
            }
        return null
    }
}
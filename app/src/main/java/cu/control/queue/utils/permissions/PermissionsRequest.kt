package cu.control.queue.utils.permissions


import android.content.pm.PackageManager
import java.util.*

internal class PermissionsRequest(private val allGrantedListener: () -> Unit,
                                  private val anyDeniedListener: () -> Unit,
                                  private val anyPermanentlyDeniedListener: () -> Unit,
                                  private val anyResultListener: () -> Unit,
                                  private val someGrantedListener: (List<String>) -> Unit,
                                  private val someDeniedListener: (List<String>) -> Unit,
                                  private val somePermanentlyDeniedListener: (List<String>) -> Unit) {

    private val PRE_REQUEST_MAPPING = HashMap<String, Boolean>()

    fun onResult(permissions: Array<out String>, grantResults: IntArray, shouldShowRationaleDialog: BooleanArray) {
        val granted = ArrayList<String>(permissions.size)
        val denied = ArrayList<String>(permissions.size)
        val permanentlyDenied = ArrayList<String>(permissions.size)

        for (i in permissions.indices) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(permissions[i])
            } else {
                val preRequestShouldShowRationaleDialog = PRE_REQUEST_MAPPING[permissions[i]]

                if (!preRequestShouldShowRationaleDialog!! && !shouldShowRationaleDialog[i]) {
                    permanentlyDenied.add(permissions[i])
                } else {
                    denied.add(permissions[i])
                }
            }
        }

        if (granted.size > 0 && denied.size == 0 && permanentlyDenied.size == 0) {
            allGrantedListener()
        } else if (granted.size > 0) {
            someGrantedListener(granted)
        }

        if (denied.size > 0) {
            anyDeniedListener()
            someDeniedListener(denied)
        }

        if (permanentlyDenied.size > 0) {
            anyPermanentlyDeniedListener()
            somePermanentlyDeniedListener(permanentlyDenied)
        }

        anyResultListener()
    }

    fun addMapping(permission: String, shouldShowRationaleDialog: Boolean) {
        PRE_REQUEST_MAPPING[permission] = shouldShowRationaleDialog
    }
}

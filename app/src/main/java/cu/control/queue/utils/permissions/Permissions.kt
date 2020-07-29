package cu.control.queue.utils.permissions


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cu.control.queue.R
import java.lang.ref.WeakReference
import java.security.SecureRandom

object Permissions {

    private val OUTSTANDING = LRUCache<Int, PermissionsRequest>(2)

    fun with(activity: Activity): PermissionsBuilder {
        return PermissionsBuilder(ActivityPermissionObject(activity))
    }

    fun with(fragment: androidx.fragment.app.Fragment): PermissionsBuilder {
        return PermissionsBuilder(FragmentPermissionObject(fragment))
    }

    class PermissionsBuilder constructor(val permissionObject: PermissionObject) {

        private var requestedPermissions: Array<String> = emptyArray()

        private var allGrantedListener: () -> Unit = {}

        private var anyDeniedListener: () -> Unit = {}
        private var anyPermanentlyDeniedListener: () -> Unit = {}
        private var anyResultListener: () -> Unit = {}

        private var someGrantedListener: (List<String>) -> Unit = {}
        private var someDeniedListener: (List<String>) -> Unit = {}
        private var somePermanentlyDeniedListener: (List<String>) -> Unit = {}

        @DrawableRes
        private var rationalDialogHeader: IntArray = intArrayOf()
        private var rationaleDialogMessage: String = ""

        private var ifNecesary: Boolean = false

        private var condition = true

        fun request(vararg requestedPermissions: String): PermissionsBuilder {
            this.requestedPermissions = requestedPermissions as Array<String>
            return this
        }

        fun ifNecessary(): PermissionsBuilder {
            this.ifNecesary = true
            return this
        }

        fun ifNecessary(condition: Boolean): PermissionsBuilder {
            this.ifNecesary = true
            this.condition = condition
            return this
        }

        fun withRationaleDialog(message: String, @DrawableRes vararg headers: Int): PermissionsBuilder {
            this.rationalDialogHeader = headers
            this.rationaleDialogMessage = message
            return this
        }

        fun withPermanentDenialDialog(message: String): PermissionsBuilder {
            return onAnyPermanentlyDenied { SettingsDialogListener(permissionObject.context, message).run() }
        }

        fun onAllGranted(allGrantedListener: () -> Unit): PermissionsBuilder {
            this.allGrantedListener = allGrantedListener
            return this
        }

        fun onAnyDenied(anyDeniedListener: () -> Unit): PermissionsBuilder {
            this.anyDeniedListener = anyDeniedListener
            return this
        }

        private fun onAnyPermanentlyDenied(anyPermanentlyDeniedListener: () -> Unit): PermissionsBuilder {
            this.anyPermanentlyDeniedListener = anyPermanentlyDeniedListener
            return this
        }

        fun onAnyResult(anyResultListener: () -> Unit): PermissionsBuilder {
            this.anyResultListener = anyResultListener
            return this
        }

        fun onSomeGranted(someGrantedListener: (List<String>) -> Unit): PermissionsBuilder {
            this.someGrantedListener = someGrantedListener
            return this
        }

        fun onSomeDenied(someDeniedListener: (List<String>) -> Unit): PermissionsBuilder {
            this.someDeniedListener = someDeniedListener
            return this
        }

        fun onSomePermanentlyDenied(somePermanentlyDeniedListener: (List<String>) -> Unit): PermissionsBuilder {
            this.somePermanentlyDeniedListener = somePermanentlyDeniedListener
            return this
        }

        fun execute() {
            val request = PermissionsRequest(allGrantedListener, anyDeniedListener, anyPermanentlyDeniedListener, anyResultListener,
                    someGrantedListener, someDeniedListener, somePermanentlyDeniedListener)

            if (ifNecesary && (permissionObject.hasAll(*requestedPermissions) || !condition)) {
                executePreGrantedPermissionsRequest(request)
            } else if (rationaleDialogMessage.isNotEmpty() && rationalDialogHeader.isNotEmpty()) {
                executePermissionsRequestWithRationale(request)
            } else {
                executePermissionsRequest(request)
            }
        }

        private fun executePreGrantedPermissionsRequest(request: PermissionsRequest) {
            val grantResults = IntArray(requestedPermissions.size)
            for (i in grantResults.indices)
                grantResults[i] = PackageManager.PERMISSION_GRANTED

            request.onResult(requestedPermissions, grantResults, BooleanArray(requestedPermissions.size))
        }

        private fun executePermissionsRequestWithRationale(request: PermissionsRequest) {
            RationaleDialog.createFor(
                permissionObject.context,
                rationaleDialogMessage,
                *rationalDialogHeader
            )
                    .setPositiveButton(R.string.Permissions_continue) { dialog, which -> executePermissionsRequest(request) }
                    .setNegativeButton(R.string.Permissions_not_now, null)
                    .show()
                    .window!!
                    .setLayout((permissionObject.windowWidth * .75).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        private fun executePermissionsRequest(request: PermissionsRequest) {
            val requestCode = SecureRandom().nextInt(65434) + 100

            synchronized(OUTSTANDING) {
                OUTSTANDING.put(requestCode, request)
            }

            for (permission in requestedPermissions) {
                request.addMapping(permission, permissionObject.shouldShouldPermissionRationale(permission))
            }

            permissionObject.requestPermissions(requestCode, *requestedPermissions)
        }

    }

    private fun requestPermissions(activity: Activity, requestCode: Int, vararg permissions: String) {
        ActivityCompat.requestPermissions(activity, filterNotGranted(activity, *permissions), requestCode)
    }

    private fun requestPermissions(fragment: androidx.fragment.app.Fragment, requestCode: Int, vararg permissions: String) {
        fragment.requestPermissions(filterNotGranted(fragment.requireContext(), *permissions), requestCode)
    }

    private fun filterNotGranted(context: Context, vararg permissions: String): Array<String> {
        return permissions.asSequence()
                .filter { permission -> ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED }
            .toList().toTypedArray()
    }

    fun hasAny(context: Context, vararg permissions: String): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || permissions.asSequence().any { permission -> ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED }

    }

    fun hasAll(context: Context, vararg permissions: String): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || permissions.asSequence().all { permission -> ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED }

    }

    fun onRequestPermissionsResult(fragment: androidx.fragment.app.Fragment, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        onRequestPermissionsResult(FragmentPermissionObject(fragment), requestCode, permissions, grantResults)
    }

    fun onRequestPermissionsResult(activity: Activity, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        onRequestPermissionsResult(ActivityPermissionObject(activity), requestCode, permissions, grantResults)
    }

    private fun onRequestPermissionsResult(context: PermissionObject, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        var resultListener: PermissionsRequest?

        synchronized(OUTSTANDING) {
            resultListener = OUTSTANDING.remove(requestCode)
        }

        if (resultListener == null) return

        val shouldShowRationaleDialog = BooleanArray(permissions.size)

        for (i in permissions.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                shouldShowRationaleDialog[i] = context.shouldShouldPermissionRationale(permissions[i])
            }
        }

        resultListener!!.onResult(permissions, grantResults, shouldShowRationaleDialog)
    }

    private fun getApplicationSettingsIntent(context: Context): Intent {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri

        return intent
    }

    abstract class PermissionObject {

        internal abstract val context: Context

        internal val windowWidth: Int
            get() {
                val windowManager = context.getSystemService(Activity.WINDOW_SERVICE) as WindowManager
                val display = windowManager.defaultDisplay
                val metrics = DisplayMetrics()
                display.getMetrics(metrics)

                return metrics.widthPixels
            }

        internal abstract fun shouldShouldPermissionRationale(permission: String): Boolean

        internal abstract fun hasAll(vararg permissions: String): Boolean

        internal abstract fun requestPermissions(requestCode: Int, vararg permissions: String)
    }

    private class ActivityPermissionObject internal constructor(private val activity: Activity) : PermissionObject() {

        public override val context: Context
            get() = activity

        public override fun shouldShouldPermissionRationale(permission: String): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }

        public override fun hasAll(vararg permissions: String): Boolean {
            return hasAll(activity, *permissions)
        }

        public override fun requestPermissions(requestCode: Int, vararg permissions: String) {
            requestPermissions(activity, requestCode, *permissions)
        }
    }

    private class FragmentPermissionObject internal constructor(private val fragment: androidx.fragment.app.Fragment) : PermissionObject() {

        public override val context: Context
            get() = fragment.requireContext()

        public override fun shouldShouldPermissionRationale(permission: String): Boolean {
            return fragment.shouldShowRequestPermissionRationale(permission)
        }

        public override fun hasAll(vararg permissions: String): Boolean {
            return hasAll(fragment.requireContext(), *permissions)
        }

        public override fun requestPermissions(requestCode: Int, vararg permissions: String) {
            requestPermissions(fragment, requestCode, *permissions)
        }
    }

    private class SettingsDialogListener constructor(context: Context, private val message: String) : Runnable {

        private val context: WeakReference<Context> = WeakReference(context)

        override fun run() {
            val context = this.context.get()

            if (context != null) {
                androidx.appcompat.app.AlertDialog.Builder(context, R.style.AlertDialogCustom)
                        .setTitle(R.string.Permissions_permission_required)
                        .setMessage(message)
                        .setPositiveButton(R.string.Permissions_continue) { _, _ -> context.startActivity(
                            getApplicationSettingsIntent(context)
                        ) }
                    .setNegativeButton(android.R.string.cancel, null)
                        .show()
            }
        }
    }
}

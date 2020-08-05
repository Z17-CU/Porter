package cu.control.queue

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntro2Fragment
import com.github.paolorotolo.appintro.model.SliderPagerBuilder
import cu.control.queue.dialogs.DialogHiClient
import cu.control.queue.interfaces.OnDialogHiClientEvent
import cu.control.queue.utils.PreferencesManager
import io.reactivex.disposables.CompositeDisposable

class WelcomeActivity : AppIntro2() {

    private lateinit var manager: PreferencesManager

    private var onDialogHiClientEvent: OnDialogHiClientEvent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        manager = PreferencesManager(this)
        if (manager.isFirstRun()) {
            showIntroSlides()
        } else {
            goToMain()
        }
    }

    private fun showIntroSlides() {
        val pageOne = SliderPagerBuilder()
            .title(getString(R.string.hi))
            .titleColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorPrimary
                )
            )
            .descColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorPrimary
                )
            )
            .description(getString(R.string.first_description))
            .imageDrawable(R.drawable.ic_recurso_1)
            .bgColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorIntroBG
                )
            )
            .build()
        val pageTwo = SliderPagerBuilder()
            .title(getString(R.string.id))
            .titleColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorPrimary
                )
            )
            .descColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorPrimary
                )
            )
            .description(getString(R.string.second_description))
            .imageDrawable(R.drawable.ic_recurso_2)
            .bgColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorIntroBG
                )
            )
            .build()
        val pageThree = SliderPagerBuilder()
            .title(getString(R.string.reg))
            .titleColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorPrimary
                )
            )
            .descColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorPrimary
                )
            )
            .description(getString(R.string.third_description))
            .imageDrawable(R.drawable.ic_recurso_3)
            .bgColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorIntroBG
                )
            )
            .build()
        val pageFour = SliderPagerBuilder()
            .title(getString(R.string.once))
            .titleColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorPrimary
                )
            )
            .descColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorPrimary
                )
            )
            .description(getString(R.string.fourth_description))
            .imageDrawable(R.drawable.ic_recurso_4)
            .bgColor(
                ContextCompat.getColor(
                    this,
                    R.color.colorIntroBG
                )
            )
            .build()
        addSlide(AppIntro2Fragment.newInstance(pageOne))
        addSlide(AppIntro2Fragment.newInstance(pageTwo))
        addSlide(AppIntro2Fragment.newInstance(pageThree))
        addSlide(AppIntro2Fragment.newInstance(pageFour))

        setFadeAnimation()
    }

    override fun onResume() {
        super.onResume()
        onDialogHiClientEvent?.onResume()
    }

    override fun onPause() {
        super.onPause()
        onDialogHiClientEvent?.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        for (index in permissions.indices.reversed()) {
            if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        // all permissions were granted
        onDialogHiClientEvent?.onCameraPermissionOk()
    }

    private fun goToMain() {
        manager.setFirstRun()
        if (manager.getSecureHasCode().isBlank())
            manager.setSecureHasCode()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        showDialog()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        showDialog()
    }

    private fun showDialog() {
        val dialogHiClient = DialogHiClient(this, CompositeDisposable(), manager)
        onDialogHiClientEvent = dialogHiClient
        val dialog = dialogHiClient.create()
        dialog.setOnDismissListener {
            goToMain()
        }
        dialog.show()
    }
}
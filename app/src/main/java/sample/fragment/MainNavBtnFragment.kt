package sample.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main_nav_buttons.*
import sample.R
import sample.activities.*
import sample.showToast

class MainNavBtnFragment : Fragment() {

    companion object {
        private val TAG = MainNavBtnFragment::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_nav_buttons, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnShowToast.setOnClickListener {
            Log.i(TAG, "Button is Clicked")
            val message: String = etUserMessage.text.toString()

            activity!!.showToast(message)

            val intent: Intent = Intent(activity!!, SecondActivity::class.java)
                .putExtra("user_message", message)
            startActivity(intent)
        }

        btnImplicitIntent.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, etUserMessage.text.toString())
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "Share to: "))
        }

        btnShowHobbiesList.setOnClickListener {
            startActivity(Intent(activity!!, HobbiesActivity::class.java))
        }

        btnGoToLocationActivity.setOnClickListener {
            startActivity(Intent(activity!!, LocationActivity::class.java))
        }

        btnGotoUserActivity.setOnClickListener {
            startActivity(Intent(activity!!, UserActivity::class.java))
        }

        btnGoToOpenGL.setOnClickListener {
            startActivity(Intent(activity!!, AirHockeyOpenGLActivity::class.java))
        }

        btnGoToCamera.setOnClickListener {
            startActivity(Intent(activity!!, CameraActivity::class.java))
        }

        btnGoToDrawingCanvas.setOnClickListener {
            startActivity(Intent(activity!!, DrawingCanvasActivity::class.java))
        }
    }
}
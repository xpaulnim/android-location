package sample.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import sample.R
import sample.showToast

class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnShowToast.setOnClickListener {
            Log.i(TAG, "Button is Clicked")
            val message: String = etUserMessage.text.toString()

            lblLabel.text = message
            showToast(message)

            val intent: Intent = Intent(this, SecondActivity::class.java)
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
            startActivity(Intent(this, HobbiesActivity::class.java))
        }

        btnGoToLocationActivity.setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }

        btnGotoUserActivity.setOnClickListener {
            startActivity(Intent(this, UserActivity::class.java))
        }

        btnGoToDayPlan.setOnClickListener {
            startActivity(Intent(this, DayPlanActivity::class.java))
        }

        btnGoToOpenGL.setOnClickListener {
            startActivity(Intent(this, OpenGLActivity::class.java))
        }
    }
}
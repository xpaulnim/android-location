package sample.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import sample.R
import sample.fragment.DayPlanFragment
import sample.fragment.MainNavBtnFragment

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, MainNavBtnFragment())
                .commit()
        }

        bottom_nav_view.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_map -> {
                    Log.i(TAG, "$it selected")

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, DayPlanFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }

            true
        }
    }
}

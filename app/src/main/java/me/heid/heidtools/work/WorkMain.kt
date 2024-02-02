package me.heid.heidtools.work

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_work_main.*
import me.heid.heidtools.R

class WorkMain : AppCompatActivity() {

    //✅ TEST MODE INFO IN BROWSE MENU
    //✅ TEST OCR FOR KEY
    //✅ TEST ADD SUPPORT FOR EXTRA TEXT
    //✅ TEST DON´T CREATE EMPTY ENTRIES IN ADD MODE
    //✅ TEST IMPLEMENT CAMERA 2 FOR FASTER LOAD TIMES
    //✅ Första foto laddar inte
    //✅ Save skickar inte tillbaka till fungerade home
    //✅ ZOOOMA IN PÅ BILDER
    //✅ Km text stoppas in på serienr rad i create
    //✅ Serie och km visas inte i view eller browse
    //✅ Serienr blir inte korrekt
    //✅ View till calendar ska skicka tillbaka till samma dag
    //TODO: TEST DELETE ENTRIES
    //TODO: TEST EDIT ENTRIES
    //TODO: CLEANUP

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.profile){
            supportFragmentManager.beginTransaction().setReorderingAllowed(true).replace(R.id.fragment_container_view,WorkSettings::class.java,null).commit()
        }

        return super.onOptionsItemSelected(item)
    }

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_work_main)
            setSupportActionBar(toolbar)
            if(savedInstanceState == null) {

                supportFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.slide_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out
                    )
                    if (intent.action.equals(Intent.ACTION_VIEW) && intent.data != null) {
                        if (intent.data!!.toString() == "New Entry") {
                            replace(R.id.fragment_container_view, WorkAddCamera2::class.java, null)
                        }else {
                            replace(R.id.fragment_container_view, WorkBrowse::class.java, null)
                        }
                    }else{
                        replace(R.id.fragment_container_view, WorkBrowse::class.java, null)
                        }

                    addToBackStack(null)

                }




           /*     if (intent.action.equals(Intent.ACTION_VIEW) && intent.data != null) {
                    if (intent.data!!.toString() == "New Entry") {
                        supportFragmentManager.beginTransaction().setReorderingAllowed(true)
                            .replace(R.id.fragment_container_view, WorkAddCamera2::class.java, null)
                            .commit()
                    }
                }
                    supportFragmentManager.beginTransaction().setReorderingAllowed(true)
                        .replace(R.id.fragment_container_view, WorkBrowse::class.java, null)
                        .commit()
*/


            }
    }

    override fun onSupportNavigateUp(): Boolean {
        return false
    }
}
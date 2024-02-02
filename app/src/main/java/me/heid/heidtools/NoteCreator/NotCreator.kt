package me.heid.heidtools.NoteCreator

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_not_creator.*
import me.heid.heidtools.R

class NotCreator : AppCompatActivity() {
    var i = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_not_creator)
        var channel = NotificationChannel("remineronte","Reminder Notefication",NotificationManager.IMPORTANCE_HIGH)
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        button4.setOnClickListener(View.OnClickListener {
            var note = Notification.Builder(this,"remineronte").setContentTitle("Reminder")
                .setContentText(editText.text.toString()).setSmallIcon(R.drawable.ic_launcher_foreground).setStyle(Notification.BigTextStyle()).build()

            notificationManager.notify(i++,note)

        })
    }
}

package com.yakirarie.chatapp


import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.yakirarie.chatapp.activities.ChatLogActivity
import com.yakirarie.chatapp.activities.MainActivity
import com.yakirarie.chatapp.classObjects.Group
import com.yakirarie.chatapp.classObjects.User
import com.yakirarie.chatapp.fragments.NewMessageFragment
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class MyFirebaseInstanceIdService : FirebaseMessagingService() {
    companion object {
        var senderUser: User? = null
        var currentUser: User? = null
        var group: Group? = null
    }

    override fun onNewToken(s: String) {
        Log.e("NEW_TOKEN", s)
    }


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val params = remoteMessage.data
        Log.d("GroupTest", "remote : ${remoteMessage.data}")

        senderUser = User(
            params["sender_id"]!!,
            params["sender_username"]!!,
            params["sender_img"]!!,
            params["sender_token"]!!
        )
        if (params["group_id"] != null) { // group msg
            val usersList = arrayListOf<User>()
            for (i in 0 until params["group_size"]!!.toInt()) {
                val user = User(
                    params["receiver_id${i + 1}"]!!,
                    params["receiver_username${i + 1}"]!!,
                    params["receiver_img${i + 1}"]!!,
                    params["receiver_token${i + 1}"]!!
                )
                usersList.add(user)
            }
            group = Group(
                params["group_id"]!!,
                params["group_admin_uid"]!!,
                params["group_name"]!!,
                params["group_image"]!!,
                usersList
            )
        } else {  // user to user msg
            currentUser = User(
                params["receiver_id"]!!,
                params["receiver_username"]!!,
                params["receiver_img"]!!,
                params["receiver_token"]!!
            )
        }


        val NOTIFICATION_CHANNEL_ID = "channel"
        val pattern = longArrayOf(0, 1000, 500, 1000)
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "Your Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = ""
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = pattern
            notificationChannel.enableVibration(true)
            mNotificationManager.createNotificationChannel(notificationChannel)
        }
        // to diaplay notification in DND Mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                mNotificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
            channel.canBypassDnd()
        }

        val am =
            this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val cn = am.appTasks[0].taskInfo.topActivity
        val currentActivity = cn?.shortClassName ?: "none"
        Log.d("CN", currentActivity)


        if (currentActivity == ".ChatLogActivity") {
            if (currentUser != null) { // user to user
                if (ChatLogActivity.toUser != null) { // you are in a user chat
                    if (senderUser!!.uid == ChatLogActivity.toUser!!.uid) { // you are in the same chat
                        return
                    }
                }
            } else if (group != null) { // user to group
                if (ChatLogActivity.toGroup != null) //you are in a group chat
                    if (group!!.uid == ChatLogActivity.toGroup!!.uid) { // you are in the same group chat
                        return
                    }
            }
        }


        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)

        resultIntent.putExtra(NewMessageFragment.USER_KEY, senderUser)
        if (currentUser != null)
            resultIntent.putExtra(MainActivity.CURRENT_USER, currentUser)
        else if (group != null) {
            resultIntent.putExtra(
                MainActivity.CURRENT_USER,
                group!!.usersList.find { it.uid == FirebaseAuth.getInstance().uid })
            resultIntent.putExtra(NewMessageFragment.GROUP_KEY, group)
        }
        val resultPendingIntent =
            PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = Builder(this, NOTIFICATION_CHANNEL_ID)
        var imageBitmap: Bitmap? = null
        if (remoteMessage.notification?.imageUrl != null)
            imageBitmap = getBitmapfromUrl(remoteMessage.notification?.imageUrl.toString())

        notificationBuilder.setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setDefaults(Notification.DEFAULT_ALL)
            .setLargeIcon(imageBitmap)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(resultPendingIntent)

        if (imageBitmap != null)
            notificationBuilder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(imageBitmap))

        mNotificationManager.notify(1000, notificationBuilder.build())
    }

    private fun getBitmapfromUrl(imageUrl: String?): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) { // TODO Auto-generated catch block
            e.printStackTrace()
            null
        }
    }

}
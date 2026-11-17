package com.ironwill.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

object RoutineNotifications {
    private const val CHANNEL_ID = "iron_will_daily_routine"
    private const val PREFS = "iron_will_routine"
    private val reminders = listOf(
        Reminder(101, 0, 15, "Morning check-in", "Log sleep, energy and readiness. Your plan adapts to today."),
        Reminder(102, 3, 0, "Movement break", "Take a comfortable walk break. Small bouts add up."),
        Reminder(103, 6, 0, "Hydration check", "Pause and drink some water. Keep hydration steady through the day."),
        Reminder(104, 10, 0, "Training window", "Your planned exercise window is approaching. Check readiness before starting."),
        Reminder(105, 15, 0, "Daily health report", "Your daily health summary is ready to review in IRON WILL.")
    )

    data class Reminder(val id: Int, val afterWakeHours: Int, val extraMinutes: Int, val title: String, val message: String)

    fun scheduleDaily(context: Context, wakeHour: Int = savedWakeHour(context), sleepHour: Int = savedSleepHour(context)) {
        createChannel(context)
        saveRoutine(context, wakeHour, sleepHour)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        reminders.forEach { reminder ->
            val hour = (wakeHour + reminder.afterWakeHours) % 24
            val minute = reminder.extraMinutes
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
            }
            val intent = Intent(context, RoutineReminderReceiver::class.java).apply {
                putExtra("reminder_id", reminder.id)
                putExtra("title", reminder.title)
                putExtra("message", reminder.message)
            }
            val pending = PendingIntent.getBroadcast(
                context, reminder.id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY, pending
            )
        }
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        reminders.forEach { reminder ->
            val intent = Intent(context, RoutineReminderReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context, reminder.id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pending)
            pending.cancel()
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("enabled", false).apply()
    }

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("enabled", false)

    fun savedWakeHour(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("wake_hour", 7)

    fun savedSleepHour(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("sleep_hour", 23)

    fun saveRoutine(context: Context, wakeHour: Int, sleepHour: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt("wake_hour", wakeHour)
            .putInt("sleep_hour", sleepHour)
            .putBoolean("enabled", true)
            .apply()
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Daily health routine", NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Personalized movement, hydration, workout and health report reminders"
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    internal fun showReminder(context: Context, id: Int, title: String, message: String) {
        createChannel(context)
        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context, id, openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // Android 13+ notification permission has not been granted.
        }
    }
}

class RoutineReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!RoutineNotifications.isEnabled(context)) return
        RoutineNotifications.showReminder(
            context,
            intent.getIntExtra("reminder_id", 100),
            intent.getStringExtra("title") ?: "IRON WILL",
            intent.getStringExtra("message") ?: "Your next health action is ready."
        )
    }
}

class RoutineBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && RoutineNotifications.isEnabled(context)) {
            RoutineNotifications.scheduleDaily(
                context,
                RoutineNotifications.savedWakeHour(context),
                RoutineNotifications.savedSleepHour(context)
            )
        }
    }
}

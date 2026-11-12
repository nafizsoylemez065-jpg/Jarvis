package com.nafiz.jarvis.commands

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.BatteryManager
import android.provider.Settings
import android.widget.Toast
import com.nafiz.jarvis.data.NotesStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CommandResult(val ok: Boolean, val speech: String)

class CommandEngine(private val context: Context) {

    private val appLauncher = AppLauncher(context)
    private val notes = NotesStore(context)

    fun tryExecute(rawInput: String): CommandResult? {
        val input = normalize(rawInput)
        if (input.isEmpty()) return null
        return flashlight(input)
            ?: volume(input)
            ?: brightness(input)
            ?: alarm(input)
            ?: timer(input)
            ?: battery(input)
            ?: timeOrDate(input)
            ?: networkSettings(input)
            ?: webSearch(input)
            ?: map(input)
            ?: call(input)
            ?: notesCommand(input)
            ?: openApp(input)
    }

    private fun normalize(text: String): String = AppLauncher.normalize(text)

    private fun has(input: String, vararg words: String) = words.any { input.contains(it) }

    private fun percentIn(input: String): Int? {
        return Regex("(\\d{1,3})").findAll(input)
            .map { it.groupValues[1].toInt() }
            .firstOrNull { it in 0..100 }
    }

    private fun numberIn(input: String): Int? =
        Regex("(\\d{1,4})").findAll(input).firstOrNull()?.groupValues?.get(1)?.toIntOrNull()

    private fun flashlight(input: String): CommandResult? {
        if (!has(input, "fener", "el feneri", "flash", "torch")) return null
        val on = when {
            has(input, "kapat", "söndür", "sondur", "kapa") -> false
            has(input, "aç", "yak") -> true
            else -> null
        }
        val ok = if (on == null) Flashlight.toggle(context) else Flashlight.set(context, on)
        val state = if (on == null) "değiştirildi" else if (on) "açıldı" else "kapatıldı"
        return CommandResult(ok, if (ok) "Fener $state" else "Feneri kontrol edemedim")
    }

    private fun volume(input: String): CommandResult? {
        if (!has(input, "ses")) return null
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val current = audio.getStreamVolume(AudioManager.STREAM_MUSIC)

        val value: Int = when {
            has(input, "kapat", "sustur", "sessiz") -> 0
            has(input, "tam", "full", "maksimum", "en yükseğe", "en yuksege") -> max
            has(input, "yükselt", "yukselt", "artır", "artir") -> (current + max / 5).coerceAtMost(max)
            has(input, "alçalt", "alcalt", "azalt", "kıs", "kis") -> (current - max / 5).coerceAtLeast(0)
            has(input, "aç", "ac") && current == 0 -> max / 2
            else -> percentIn(input)?.let { (it * max) / 100 } ?: return null
        }

        val changed = runCatching {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0)
            true
        }.getOrElse {
            Toast.makeText(context, "Ses ayarlanamadı", Toast.LENGTH_SHORT).show()
            false
        }
        if (!changed) return CommandResult(false, "Ses seviyesini değiştiremedim")
        val pct = (value * 100) / max
        return CommandResult(true, "Ses seviyesi yüzde $pct")
    }

    private fun brightness(input: String): CommandResult? {
        if (!has(input, "parlaklık", "parlaklik")) return null
        if (!Settings.System.canWrite(context)) {
            runCatching {
                context.startActivity(
                    Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .setData(android.net.Uri.parse("package:" + context.packageName))
                )
            }
            return CommandResult(
                false,
                "Parlaklığı kontrol etmem için izin ekranını açtım, izin verip tekrar dener misin?"
            )
        }
        val value = when {
            has(input, "tam", "full", "maksimum", "en yüksek", "en yuksek") -> 100
            else -> percentIn(input)
        } ?: return CommandResult(false, "Parlaklık yüzdesini söyle, örneğin: parlaklığı 50 yap")
        val changed = runCatching {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                (25 + (value * 230) / 100).coerceIn(25, 255)
            )
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            true
        }.getOrDefault(false)
        return if (changed) CommandResult(true, "Parlaklık yüzde $value")
        else CommandResult(false, "Parlaklığı değiştiremedim")
    }

    private fun alarm(input: String): CommandResult? {
        if (!has(input, "alarm")) return null
        val m = Regex("(\\d{1,2})\\s*[:.]\\s*(\\d{2})").find(input)
            ?: return CommandResult(false, "Alarm saatini söylemedin, örneğin: alarm kur 07:30")
        val hour = m.groupValues[1].toIntOrNull()?.takeIf { it in 0..23 }
            ?: return CommandResult(false, "Saat 0 ile 23 arasında olmalı")
        val minute = m.groupValues[2].toIntOrNull()?.takeIf { it in 0..59 }
            ?: return CommandResult(false, "Dakika 0 ile 59 arasında olmalı")
        val ok = Intents.setAlarm(context, hour, minute)
        val time = String.format(Locale("tr"), "%02d:%02d", hour, minute)
        return CommandResult(ok, if (ok) "Alarm $time için kuruldu" else "Alarmı kuramadım, saat uygulaması yanıt vermedi")
    }

    private fun timer(input: String): CommandResult? {
        if (!has(input, "zamanlayıcı", "zamanlayici", "sayaç", "sayac")) return null
        val number = numberIn(input)
            ?: return CommandResult(false, "Süreyi söyle, örneğin: 5 dakika zamanlayıcı kur")
        val seconds = when {
            has(input, "saat") -> number * 3600
            has(input, "saniye") -> number
            else -> number * 60
        }
        val ok = Intents.setTimer(context, seconds)
        return CommandResult(ok, if (ok) "Zamanlayıcı kuruldu" else "Zamanlayıcıyı kuramadım")
    }

    private fun battery(input: String): CommandResult? {
        if (!has(input, "batarya", "pil", "şarj", "sarj")) return null
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?: return CommandResult(false, "Batarya bilgisini alamadım")
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level < 0 || scale <= 0) return CommandResult(false, "Batarya bilgisini alamadım")
        val pct = (level * 100) / scale
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
        return CommandResult(true, "Batarya yüzde $pct" + if (charging) " ve şu an şarj oluyor" else "")
    }

    private fun timeOrDate(input: String): CommandResult? {
        val now = Date()
        if (has(input, "saat kaç", "saat kac", "kaç oldu", "kac oldu")) {
            return CommandResult(true, "Saat şu an " + SimpleDateFormat("HH:mm", Locale("tr")).format(now))
        }
        if (has(input, "tarih", "ayın kaçı", "ayin kaci", "günlerden", "gunlerden")) {
            return CommandResult(true, "Bugün " + SimpleDateFormat("dd MMMM yyyy, EEEE", Locale("tr")).format(now))
        }
        return null
    }

    private fun networkSettings(input: String): CommandResult? {
        if (has(input, "wifi", "wi-fi", "wi fi", "internet ayar", "ağ ayar", "ag ayar")) {
            val ok = Intents.openSettings(context, Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            return CommandResult(
                ok,
                if (ok) "Wi-Fi panelini açtım, oradan açıp kapatabilirsin" else "Wi-Fi ayarlarını açamadım"
            )
        }
        if (has(input, "bluetooth", "blue tooth")) {
            val ok = Intents.openSettings(context, Settings.ACTION_BLUETOOTH_SETTINGS)
            return CommandResult(ok, if (ok) "Bluetooth panelini açtım" else "Bluetooth ayarlarını açamadım")
        }
        return null
    }

    private fun webSearch(input: String): CommandResult? {
        if (!has(input, "google", "internette", "webde", "web'de", "arama yap")) return null
        var query = input
        listOf("google'da", "googleda", "google da", "google", "internette", "webde", "web'de", "arama yap")
            .forEach { query = query.replace(it, " ") }
        query = query
            .replace(Regex("\\b(ara|arat|bak|lütfen|bir)\\b"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (query.isEmpty()) return null
        val ok = Intents.webSearch(context, query)
        return CommandResult(ok, if (ok) "\"$query\" için arama yapıyorum" else "Aramayı başlatamadım")
    }

    private fun map(input: String): CommandResult? {
        if (!has(input, "harita", "yol tarifi", "navigasyon", "nasıl giderim", "nasil giderim")) return null
        val query = input
            .replace(Regex("\\b(haritada|harita|yol tarifi|navigasyon|göster|goster|aç|ac|git|lütfen|bana|nasıl giderim|nasil giderim)\\b"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (query.isEmpty()) return null
        val ok = Intents.map(context, query)
        return CommandResult(ok, if (ok) "\"$query\" için haritayı açıyorum" else "Haritayı açamadım")
    }

    private fun call(input: String): CommandResult? {
        val m = Regex("^(.+?)\\s+(?:'\\s*)?ara(?:mas[ıi]|ma)?\\s*(?:m[ıi]s[ıi]n|musun)?$").find(input)
            ?: return null
        val target = m.groupValues[1].trim()
        if (target.isEmpty() || has(target, "google", "internette", "webde")) return null
        if (!Intents.isDialable(target)) {
            return CommandResult(
                false,
                "\"$target\" için numarayı bilmiyorum, rehberime erişimim yok. Numarayı söylersen çeviririm."
            )
        }
        val number = target.filter { it.isDigit() || it == '+' }
        val ok = Intents.dial(context, number)
        return CommandResult(ok, if (ok) "Çevirme ekranını açtım, onaylaman yeterli" else "Çevirmeyi başlatamadım")
    }

    private fun notesCommand(input: String): CommandResult? {
        val addMatch = Regex("(?:not al|notuma ekle|not ekle)\\s*(.*)").find(input)
        if (addMatch != null) {
            val note = addMatch.groupValues[1].trim()
            if (note.isEmpty()) return CommandResult(false, "Notu söylemedin")
            notes.add(note)
            return CommandResult(true, "Not aldım: $note")
        }
        if (has(input, "notlarım", "notlarim", "notları oku", "notlari oku", "notlarımı oku", "notlarimi oku")) {
            val all = notes.all()
            return if (all.isEmpty()) {
                CommandResult(true, "Henüz hiç not almamışsın")
            } else {
                CommandResult(true, "Notların: " + all.takeLast(5).joinToString("; "))
            }
        }
        return null
    }

    private fun openApp(input: String): CommandResult? {
        if (!has(input, "aç", "başlat", "baslat", "çalıştır", "calistir")) return null
        var query = input
        listOf(
            "lütfen", "bana", "bir", "uygulamasını", "uygulamasini", "uygulaması", "uygulamasi",
            "uygulama", "açar mısın", "acar misin", "açabilir misin", "acabilir misin",
            "başlat", "baslat", "çalıştır", "calistir", "aç", "git"
        ).forEach { query = query.replace(it, " ") }
        query = query
            .replace(Regex("\\s+"), " ")
            .trim()
            .replace(Regex("['’]?(u|ü|i|ı|yu|yü|yi|yı|nu|nü|ni|nı)$"), "")
            .trim()
        if (query.isEmpty()) return null
        val app = appLauncher.find(query)
            ?: return CommandResult(false, "\"$query\" diye bir uygulama bulamadım")
        val ok = appLauncher.launch(app)
        return CommandResult(ok, if (ok) "${app.label} açılıyor" else "${app.label} uygulamasını açamadım")
    }
}

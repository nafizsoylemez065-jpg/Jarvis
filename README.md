# 🤖 Jarvis — Kişisel Android Asistan

Kotlin + Jetpack Compose ile yazılmış, sesli ve yazılı komutlarla çalışan mobil Jarvis uygulaması.

## Özellikler

| Özellik | Açıklama |
|---|---|
| 🎙️ Sesli sohbet | Mikrofona bas & konuş (Türkçe konuşma tanıma + sesli yanıt) |
| 💬 Yazılı sohbet | Mesaj geçmişi kaydedilir; API anahtarı girilince tam sohbet modu |
| ⚡ Komut yürütme | Fener, ses, parlaklık, alarm, zamanlayıcı, batarya, not, Wi-Fi/Bluetooth paneli |
| 📱 Uygulama açma | "YouTube'u aç" → yüklü herhangi bir uygulamayı ismiyle açar |

## Komut Örnekleri

- **Uygulama açma:** `YouTube'u aç`, `Spotify'ı başlat`, `WhatsApp'ı çalıştır`
- **Cihaz:** `Feneri aç`, `Sesi yüzde 50 yap`, `Sesi kıs`, `Parlaklığı 80 yap`
- **Zaman:** `Alarm kur 07:30`, `5 dakika zamanlayıcı kur`, `Saat kaç?`
- **Arama:** `Google'da yapay zeka ara`, `Haritada İstanbul göster`
- **Bilgi:** `Batarya yüzde kaç?`, `Wi-Fi ayarlarını aç`, `Not al yarın ekmek al`

## APK'yı İndirme (en kolay yol)

Android Studio kurmadan APK indirebilirsin:

1. Depo sayfasında **Actions** sekmesine gir
2. **Build APK** iş akışına tıkla
3. Tamamlanınca (yeşil ✓) işin detayından **Artifacts → jarvis-debug-apk** dosyasını indir
4. ZIP'i aç, içindeki `app-debug.apk`'yı telefona at ve kur ("Bilinmeyen kaynaklara izin ver" demen gerekebilir)

## Kurulum (Android Studio ile)

1. Bu depoyu klonla: `git clone https://github.com/nafizsoylemez065-jpg/Jarvis.git`
2. **Android Studio** (Hedgehog veya yenisi) ile projeyi aç
3. Android Studio Gradle'ı otomatik indirir; wrapper sorarsa ekrandaki onay düğmesiyle çözer
4. Telefonu USB ile bağla (USB hata ayıklama açık) veya emülatör başlat
5. **Run ▶** butonuna bas

Gereksinimler: Android Studio, JDK 17 (Android Studio ile gelir), min Android 8.0 (API 26).

## Yapay Zeka Sohbeti (opsiyonel)

Komutlar internet olmadan çalışır. Serbest sohbet için OpenAI uyumlu bir API anahtarı gerekir:

1. **Mistral** ([console.mistral.ai](https://console.mistral.ai) — ücretsiz katman var), OpenAI, OpenRouter veya Groq'tan anahtar al
2. Uygulamada **Ayarlar** sekmesine gir
3. API Adresi, Anahtar ve Model bilgilerini yaz → Kaydet

Varsayılan ayarlar: `https://api.mistral.ai/v1` + `mistral-small-latest`

## Proje Yapısı

```
app/src/main/java/com/nafiz/jarvis/
├── MainActivity.kt          # Compose ana ekran + alt navigasyon + mikrofon izni
├── ChatViewModel.kt         # Sohbet akışı: komut mu, sohbet mi?
├── commands/
│   ├── CommandEngine.kt     # Türkçe komut ayrıştırıcı (kalp)
│   ├── AppLauncher.kt       # Yüklü uygulamaları bulup açar
│   ├── Flashlight.kt        # Kamera fener kontrolü
│   └── (Intents)            # Alarm, timer, dial, harita, web arama
├── voice/
│   ├── VoiceInput.kt        # SpeechRecognizer (tr-TR)
│   └── TtsManager.kt        # TextToSpeech (Türkçe ses)
├── llm/LlmClient.kt         # OpenAI uyumlu API istemcisi
├── data/                    # Ayarlar, sohbet geçmişi, notlar
└── ui/                      # Sohbet, Komutlar, Ayarlar ekranları
```

## İzinler

- **Mikrofon:** sesli komutlar için (kullanımda istenir)
- **Alarm:** cihazın saat uygulamasına alarm/zamanlayıcı kurmak için
- Diğer her şey Android'in standart izin akışıyla çalışır

# KaloriTakip

Offline-first mobil kalori takip uygulaması (Flutter). Üç temel özellik:

1. **Fotoğraftan AI kalori tahmini** — fotoğraf çekilir, cihaz üzerindeki yemek tanıma modeli aday yemekleri önerir; kalori değerleri gömülü yemek veritabanından gelir.
2. **Günlük kalori hedefi** — kullanıcı doğrudan manuel değer girer; istersen Mifflin-St Jeor tabanlı hesaplayıcı öneri üretir, uygulama ancak kullanıcı onaylarsa uygular.
3. **İnternetsiz çalışma** — tüm veriler cihazda SQLite'ta tutulur; hiçbir temel fonksiyon ağ gerektirmez.

## Kurulum

Depoda yalnızca Dart/Flutter kaynak kodu vardır; platform klasörlerini (android, ios) oluşturmak için Flutter SDK ile:

```bash
flutter create . --project-name kaloritakip --platforms android,ios --org com.kaloritakip
flutter pub get
flutter run
```

iOS'ta kamera kullanımı için `ios/Runner/Info.plist` içine şunları ekleyin:

```xml
<key>NSCameraUsageDescription</key>
<string>Yemek fotoğrafı çekmek için kameraya erişim gerekir.</string>
<key>NSPhotoLibraryUsageDescription</key>
<string>Yemek fotoğrafı seçmek için galeriye erişim gerekir.</string>
```

## Test

```bash
flutter test
```

## Mimari

```
lib/
  main.dart                        Uygulama giriş noktası
  models/                          FoodItem, MealEntry, DailyGoal
  data/app_database.dart           SQLite (sqflite) + gömülü Türk yemekleri veritabanı
  services/calorie_calculator.dart Mifflin-St Jeor BMR/TDEE + hedef hesabı
  services/food_recognition_service.dart  AI bağlantı noktası
  state/meal_store.dart            Uygulama durumu (ChangeNotifier)
  screens/                         Home, Yemek Ekle, Günlük Hedef
```

## Fotoğraftan AI entegrasyonu (yol haritası)

`lib/services/food_recognition_service.dart` tek entegrasyon noktasıdır. Şu an stub'tır
(boş aday listesi döner). TensorFlow Lite yemek sınıflandırma modeli eklendiğinde
yalnızca bu sınıfın içi doldurulur:

- Model (Food-101 tabanlı, quantize edilmiş, ~10 MB) `assets/models/` altına konur.
- `recognize()` görselden ilk N aday yemeği döner; adaylar `FoodItem` listesi olarak
  yemek ekranında öneri çipi halinde görünür.
- Kalori modelden değil, cihazdaki `food_items` tablosundan okunur; model yalnızca
  yemeği tanır. Bu, tahmini çok daha güvenilir kılar.

Önerilen paketler: `tflite_flutter` (cihaz içi çıkarım), opsiyonel olarak internet
varken daha güçlü bir vision API (sonuçlar `FoodSource.cache` ile önbelleklenir).

## Yol haritası

- [x] MVP: manuel yemek ekleme, yerel SQLite, günlük hedef (manuel + hesaplayıcı), ana ekran
- [ ] TFLite yemek tanıma modelinin `FoodRecognitionService` içine bağlanması
- [ ] Haftalık/aylık istatistik grafikleri
- [ ] Kullanıcının düzeltmelerinden öğrenme (fotoğraf → doğru yemek eşleşmesi)
- [ ] Opsiyonel bulut senkron (çevrimiçi olunca kuyruktan)

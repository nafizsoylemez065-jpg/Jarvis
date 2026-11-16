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

## Fotoğraftan AI kalori tahmini

`lib/services/food_recognition_service.dart` cihaz üzerinde çalışan tam bir TFLite
çıkarım hattı içerir: görsel çözülür → 224×224'e ölçeklenir → normalize edilir →
model çalıştırılır → softmax sonrası ilk 3 aday öneri çipi olarak gösterilir.
Model dosyası yoksa servis sessizce boş aday listesi döner ve uygulama normal
çalışmaya devam eder (tamamen offline).

Modeli bağlamak için:

1. Eğitilmiş yemek sınıflandırma modelini (örn. Food-101 tabanlı, quantize,
   ~10 MB, girdi 224×224×3) `assets/models/food_model.tflite` olarak koyun.
2. `kFoodLabels` listesini modelinizin sınıf sırasıyla birebir aynı sırada
   düzenleyin; her etiket Türk yemek adı ve 100 g başına kaloriyle eşlenir.
3. `flutter pub get && flutter run` — hepsi bu; arayüz değişikliği gerekmez.

Kalori modelden değil etiket tablosundan gelir; model yalnızca yemeği tanır.
Opsiyonel olarak internet varken daha güçlü bir vision API eklenebilir
(sonuçlar `FoodSource.cache` ile önbelleklenir).

## Yol haritası

- [x] MVP: manuel yemek ekleme, yerel SQLite, günlük hedef (manuel + hesaplayıcı), ana ekran
- [ ] Eğitilmiş model dosyasının `assets/models/food_model.tflite` olarak yerleştirilmesi
- [x] Haftalık istatistik ekranı: 7 günlük kalori grafiği, hedef çizgisi, seri ve ortalama
- [ ] Kullanıcının düzeltmelerinden öğrenme (fotoğraf → doğru yemek eşleşmesi)
- [ ] Opsiyonel bulut senkron (çevrimiçi olunca kuyruktan)

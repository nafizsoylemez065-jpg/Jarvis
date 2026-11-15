import 'dart:io';

import '../models/food_item.dart';

/// Cihaz üzerinde çalışan yemek tanıma servisinin bağlantı noktası.
/// TensorFlow Lite yemek modeli entegre edildiğinde yalnızca bu sınıfın
/// içi doldurulur; arayüz ve akış değişmeden kalır.
class FoodRecognitionService {
  const FoodRecognitionService();

  Future<List<FoodItem>> recognize(File image) async {
    return const [];
  }
}

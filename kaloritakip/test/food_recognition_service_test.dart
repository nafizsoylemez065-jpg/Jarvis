import 'package:flutter_test/flutter_test.dart';
import 'package:kaloritakip/services/food_recognition_service.dart';

void main() {
  group('FoodRecognitionService.softmax', () {
    test('olasılıkların toplamı 1 olur', () {
      final result = FoodRecognitionService.softmax([1.0, 2.0, 3.0]);
      final sum = result.fold<double>(0, (total, value) => total + value);
      expect(sum, closeTo(1.0, 1e-6));
      expect(result[2], greaterThan(result[0]));
    });

    test('boş liste boş döner', () {
      expect(FoodRecognitionService.softmax([]), isEmpty);
    });
  });

  group('FoodRecognitionService.topCandidates', () {
    test('en yüksek skorlu indeksleri sıralı döner', () {
      final top = FoodRecognitionService.topCandidates([0.1, 0.5, 0.3, 0.2], 3);
      expect(top, [1, 2, 3]);
    });

    test('istenen sayıdan az skor varsa hepsini döner', () {
      final top = FoodRecognitionService.topCandidates([0.4, 0.6], 5);
      expect(top.length, 2);
      expect(top.first, 1);
    });
  });
}

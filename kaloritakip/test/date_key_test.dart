import 'package:flutter_test/flutter_test.dart';
import 'package:kaloritakip/utils/date_key.dart';

void main() {
  test('dateKey tarihi yyyy-MM-dd biçimine çevirir', () {
    expect(dateKey(DateTime(2025, 3, 7)), '2025-03-07');
    expect(dateKey(DateTime(2025, 12, 31)), '2025-12-31');
  });

  test('last7Days artan sırada 7 gün döner', () {
    final days = last7Days();
    expect(days.length, 7);
    expect(days, days.toSet().toList());
    final sorted = [...days]..sort();
    expect(days, sorted);
    expect(days.last, todayKey());
  });
}

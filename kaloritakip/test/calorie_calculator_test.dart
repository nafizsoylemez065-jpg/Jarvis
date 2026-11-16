import 'package:flutter_test/flutter_test.dart';
import 'package:kaloritakip/services/calorie_calculator.dart';

void main() {
  group('CalorieCalculator.bmr', () {
    test('erkek için Mifflin-St Jeor', () {
      final bmr = CalorieCalculator.bmr(
        gender: Gender.male,
        age: 30,
        heightCm: 180,
        weightKg: 80,
      );
      expect(bmr, closeTo(1780, 1));
    });

    test('kadın için Mifflin-St Jeor', () {
      final bmr = CalorieCalculator.bmr(
        gender: Gender.female,
        age: 30,
        heightCm: 180,
        weightKg: 80,
      );
      expect(bmr, closeTo(1614, 1));
    });
  });

  group('CalorieCalculator.tdee', () {
    test('aktivite çarpanı uygulanır', () {
      final tdee = CalorieCalculator.tdee(
        gender: Gender.male,
        age: 30,
        heightCm: 180,
        weightKg: 80,
        activity: ActivityLevel.moderate,
      );
      expect(tdee, closeTo(1780 * 1.55, 1));
    });
  });

  group('CalorieCalculator.goalKcal', () {
    test('kilo verme hedefi 500 kcal düşürür', () {
      final goal = CalorieCalculator.goalKcal(
        gender: Gender.male,
        age: 30,
        heightCm: 180,
        weightKg: 80,
        activity: ActivityLevel.moderate,
        adjustment: GoalAdjustment.lose,
      );
      expect(goal, (1780 * 1.55 - 500).round());
    });

    test('kilo alma hedefi 300 kcal yükseltir', () {
      final goal = CalorieCalculator.goalKcal(
        gender: Gender.female,
        age: 25,
        heightCm: 165,
        weightKg: 55,
        activity: ActivityLevel.light,
        adjustment: GoalAdjustment.gain,
      );
      final expected =
          ((10 * 55 + 6.25 * 165 - 5 * 25 - 161) * 1.375 + 300).round();
      expect(goal, expected);
    });
  });
}

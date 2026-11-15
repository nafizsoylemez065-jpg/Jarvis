enum Gender { male, female }

enum ActivityLevel {
  sedentary(1.2, 'Hareketsiz'),
  light(1.375, 'Az hareketli'),
  moderate(1.55, 'Orta hareketli'),
  active(1.725, 'Aktif'),
  veryActive(1.9, 'Çok aktif');

  const ActivityLevel(this.factor, this.label);

  final double factor;
  final String label;
}

enum GoalAdjustment {
  lose(-500, 'Kilo ver'),
  maintain(0, 'Kiloyu koru'),
  gain(300, 'Kilo al');

  const GoalAdjustment(this.delta, this.label);

  final int delta;
  final String label;
}

class CalorieCalculator {
  const CalorieCalculator._();

  static double bmr({
    required Gender gender,
    required int age,
    required double heightCm,
    required double weightKg,
  }) {
    final base = 10 * weightKg + 6.25 * heightCm - 5 * age;
    return gender == Gender.male ? base + 5 : base - 161;
  }

  static double tdee({
    required Gender gender,
    required int age,
    required double heightCm,
    required double weightKg,
    required ActivityLevel activity,
  }) =>
      bmr(
        gender: gender,
        age: age,
        heightCm: heightCm,
        weightKg: weightKg,
      ) *
      activity.factor;

  static int goalKcal({
    required Gender gender,
    required int age,
    required double heightCm,
    required double weightKg,
    required ActivityLevel activity,
    required GoalAdjustment adjustment,
  }) =>
      (tdee(
            gender: gender,
            age: age,
            heightCm: heightCm,
            weightKg: weightKg,
            activity: activity,
          ) +
          adjustment.delta)
          .round();
}

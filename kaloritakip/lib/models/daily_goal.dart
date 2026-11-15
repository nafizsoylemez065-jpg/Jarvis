enum GoalSource { manual, calculated }

class DailyGoal {
  const DailyGoal({
    required this.day,
    required this.targetKcal,
    required this.source,
  });

  final String day;
  final int targetKcal;
  final GoalSource source;

  Map<String, Object?> toMap() => {
        'day': day,
        'target_kcal': targetKcal,
        'source': source.name,
      };

  factory DailyGoal.fromMap(Map<String, Object?> map) => DailyGoal(
        day: map['day'] as String,
        targetKcal: map['target_kcal'] as int,
        source: GoalSource.values.firstWhere(
          (source) => source.name == map['source'],
          orElse: () => GoalSource.manual,
        ),
      );
}

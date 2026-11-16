enum MealType {
  breakfast('Kahvaltı'),
  lunch('Öğle Yemeği'),
  dinner('Akşam Yemeği'),
  snack('Ara Öğün');

  const MealType(this.label);

  final String label;

  static MealType fromName(String name) => MealType.values.firstWhere(
        (type) => type.name == name,
        orElse: () => MealType.snack,
      );
}

class MealEntry {
  const MealEntry({
    this.id,
    required this.day,
    required this.mealType,
    required this.name,
    required this.grams,
    required this.kcalPer100g,
    this.photoPath,
    this.confidence,
  });

  final int? id;
  final String day;
  final MealType mealType;
  final String name;
  final double grams;
  final double kcalPer100g;
  final String? photoPath;
  final double? confidence;

  double get kcalTotal => kcalPer100g * grams / 100;

  MealEntry withId(int id) => MealEntry(
        id: id,
        day: day,
        mealType: mealType,
        name: name,
        grams: grams,
        kcalPer100g: kcalPer100g,
        photoPath: photoPath,
        confidence: confidence,
      );

  Map<String, Object?> toMap() => {
        if (id != null) 'id': id,
        'day': day,
        'meal_type': mealType.name,
        'name': name,
        'grams': grams,
        'kcal_per_100g': kcalPer100g,
        'kcal_total': kcalTotal,
        'photo_path': photoPath,
        'confidence': confidence,
      };

  factory MealEntry.fromMap(Map<String, Object?> map) => MealEntry(
        id: map['id'] as int?,
        day: map['day'] as String,
        mealType: MealType.fromName(map['meal_type'] as String),
        name: map['name'] as String,
        grams: (map['grams'] as num).toDouble(),
        kcalPer100g: (map['kcal_per_100g'] as num).toDouble(),
        photoPath: map['photo_path'] as String?,
        confidence: (map['confidence'] as num?)?.toDouble(),
      );
}

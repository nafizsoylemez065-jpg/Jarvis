enum FoodSource { bundled, user, cache }

class FoodItem {
  const FoodItem({
    this.id,
    required this.name,
    required this.kcalPer100g,
    this.proteinPer100g = 0,
    this.carbPer100g = 0,
    this.fatPer100g = 0,
    this.source = FoodSource.bundled,
  });

  final int? id;
  final String name;
  final double kcalPer100g;
  final double proteinPer100g;
  final double carbPer100g;
  final double fatPer100g;
  final FoodSource source;

  Map<String, Object?> toMap() => {
        if (id != null) 'id': id,
        'name': name,
        'kcal_per_100g': kcalPer100g,
        'protein_per_100g': proteinPer100g,
        'carb_per_100g': carbPer100g,
        'fat_per_100g': fatPer100g,
        'source': source.name,
      };

  factory FoodItem.fromMap(Map<String, Object?> map) => FoodItem(
        id: map['id'] as int?,
        name: map['name'] as String,
        kcalPer100g: (map['kcal_per_100g'] as num).toDouble(),
        proteinPer100g: (map['protein_per_100g'] as num? ?? 0).toDouble(),
        carbPer100g: (map['carb_per_100g'] as num? ?? 0).toDouble(),
        fatPer100g: (map['fat_per_100g'] as num? ?? 0).toDouble(),
        source: FoodSource.values.firstWhere(
          (source) => source.name == map['source'],
          orElse: () => FoodSource.bundled,
        ),
      );
}

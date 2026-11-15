import 'package:flutter/foundation.dart';

import '../data/app_database.dart';
import '../models/daily_goal.dart';
import '../models/food_item.dart';
import '../models/meal_entry.dart';
import '../utils/date_key.dart';

class MealStore extends ChangeNotifier {
  MealStore(this._db);

  final AppDatabase _db;

  DailyGoal? goal;
  List<MealEntry> entries = [];

  int get consumedKcal =>
      entries.fold(0, (sum, entry) => sum + entry.kcalTotal.round());
  int get targetKcal => goal?.targetKcal ?? 2000;

  List<MealEntry> entriesFor(MealType type) =>
      entries.where((entry) => entry.mealType == type).toList();

  Future<void> loadToday() async {
    final day = todayKey();
    goal = await _db.goalForDay(day);
    entries = await _db.mealsForDay(day);
    notifyListeners();
  }

  Future<void> addEntry({
    required MealType mealType,
    required String name,
    required double grams,
    required double kcalPer100g,
    String? photoPath,
    double? confidence,
  }) async {
    final entry = MealEntry(
      day: todayKey(),
      mealType: mealType,
      name: name,
      grams: grams,
      kcalPer100g: kcalPer100g,
      photoPath: photoPath,
      confidence: confidence,
    );
    final id = await _db.insertMealEntry(entry);
    entries = [...entries, entry.withId(id)];
    notifyListeners();
  }

  Future<void> deleteEntry(MealEntry entry) async {
    final id = entry.id;
    if (id == null) return;
    await _db.deleteMealEntry(id);
    entries = entries.where((item) => item.id != id).toList();
    notifyListeners();
  }

  Future<void> setGoal(int targetKcal, GoalSource source) async {
    final newGoal = DailyGoal(
      day: todayKey(),
      targetKcal: targetKcal,
      source: source,
    );
    await _db.upsertGoal(newGoal);
    goal = newGoal;
    notifyListeners();
  }

  Future<List<FoodItem>> searchFoods(String query) =>
      _db.searchFoods(query);

  Future<int> insertFood(FoodItem food) => _db.insertFood(food);
}

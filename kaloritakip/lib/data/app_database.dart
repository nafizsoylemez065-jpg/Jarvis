import 'package:path/path.dart' as p;
import 'package:sqflite/sqflite.dart';

import '../models/daily_goal.dart';
import '../models/food_item.dart';
import '../models/meal_entry.dart';

const List<FoodItem> kBundledFoods = [
  FoodItem(name: 'Mercimek çorbası', kcalPer100g: 86),
  FoodItem(name: 'Ezogelin çorbası', kcalPer100g: 95),
  FoodItem(name: 'Tavuk göğsü (ızgara)', kcalPer100g: 165),
  FoodItem(name: 'Tavuk şiş', kcalPer100g: 190),
  FoodItem(name: 'Pilav (beyaz)', kcalPer100g: 130),
  FoodItem(name: 'Bulgur pilavı', kcalPer100g: 112),
  FoodItem(name: 'Kuru fasulye', kcalPer100g: 127),
  FoodItem(name: 'Nohut yemeği', kcalPer100g: 164),
  FoodItem(name: 'Köfte', kcalPer100g: 250),
  FoodItem(name: 'Adana kebap', kcalPer100g: 240),
  FoodItem(name: 'Döner (et)', kcalPer100g: 215),
  FoodItem(name: 'Lahmacun', kcalPer100g: 260),
  FoodItem(name: 'Pide (kıymalı)', kcalPer100g: 255),
  FoodItem(name: 'Börek (kıymalı)', kcalPer100g: 290),
  FoodItem(name: 'Menemen', kcalPer100g: 130),
  FoodItem(name: 'Omlet (2 yumurta)', kcalPer100g: 154),
  FoodItem(name: 'Makarna (soslu)', kcalPer100g: 158),
  FoodItem(name: 'Mantı', kcalPer100g: 210),
  FoodItem(name: 'İskender', kcalPer100g: 220),
  FoodItem(name: 'Salata (çoban)', kcalPer100g: 60),
  FoodItem(name: 'Yoğurt', kcalPer100g: 61),
  FoodItem(name: 'Ayran', kcalPer100g: 37),
  FoodItem(name: 'Beyaz peynir', kcalPer100g: 264),
  FoodItem(name: 'Zeytin (siyah)', kcalPer100g: 115),
  FoodItem(name: 'Ekmek (beyaz)', kcalPer100g: 265),
  FoodItem(name: 'Elma', kcalPer100g: 52),
  FoodItem(name: 'Muz', kcalPer100g: 89),
  FoodItem(name: 'Portakal', kcalPer100g: 47),
  FoodItem(name: 'Ceviz', kcalPer100g: 654),
  FoodItem(name: 'Fındık', kcalPer100g: 628),
  FoodItem(name: 'Süt (yarım yağlı)', kcalPer100g: 50),
  FoodItem(name: 'Baklava', kcalPer100g: 428),
];

class AppDatabase {
  Database? _database;

  Future<Database> get _db async {
    _database ??= await _open();
    return _database!;
  }

  Future<Database> _open() async {
    final dir = await getDatabasesPath();
    return openDatabase(
      p.join(dir, 'kaloritakip.db'),
      version: 1,
      onCreate: (db, version) async {
        await db.execute('''
          CREATE TABLE food_items(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL UNIQUE,
            kcal_per_100g REAL NOT NULL,
            protein_per_100g REAL NOT NULL DEFAULT 0,
            carb_per_100g REAL NOT NULL DEFAULT 0,
            fat_per_100g REAL NOT NULL DEFAULT 0,
            source TEXT NOT NULL DEFAULT 'bundled'
          )
        ''');
        await db.execute('''
          CREATE TABLE meal_entries(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            day TEXT NOT NULL,
            meal_type TEXT NOT NULL,
            name TEXT NOT NULL,
            grams REAL NOT NULL,
            kcal_per_100g REAL NOT NULL,
            kcal_total REAL NOT NULL,
            photo_path TEXT,
            confidence REAL
          )
        ''');
        await db.execute('''
          CREATE TABLE daily_goals(
            day TEXT PRIMARY KEY,
            target_kcal INTEGER NOT NULL,
            source TEXT NOT NULL
          )
        ''');
        await _seedFoods(db);
      },
    );
  }

  Future<void> _seedFoods(Database db) async {
    for (final food in kBundledFoods) {
      await db.insert('food_items', food.toMap());
    }
  }

  Future<int> insertMealEntry(MealEntry entry) async {
    final db = await _db;
    return db.insert('meal_entries', entry.toMap());
  }

  Future<List<MealEntry>> mealsForDay(String day) async {
    final db = await _db;
    final rows = await db.query(
      'meal_entries',
      where: 'day = ?',
      whereArgs: [day],
      orderBy: 'id',
    );
    return rows.map(MealEntry.fromMap).toList();
  }

  Future<int> deleteMealEntry(int id) async {
    final db = await _db;
    return db.delete('meal_entries', where: 'id = ?', whereArgs: [id]);
  }

  Future<DailyGoal?> goalForDay(String day) async {
    final db = await _db;
    final rows = await db.query(
      'daily_goals',
      where: 'day = ?',
      whereArgs: [day],
      limit: 1,
    );
    if (rows.isEmpty) return null;
    return DailyGoal.fromMap(rows.first);
  }

  Future<void> upsertGoal(DailyGoal goal) async {
    final db = await _db;
    await db.insert(
      'daily_goals',
      goal.toMap(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  Future<List<FoodItem>> searchFoods(String query) async {
    final db = await _db;
    final rows = await db.query(
      'food_items',
      where: 'name LIKE ?',
      whereArgs: ['%$query%'],
      orderBy: 'name',
      limit: 8,
    );
    return rows.map(FoodItem.fromMap).toList();
  }

  Future<int> insertFood(FoodItem food) async {
    final db = await _db;
    return db.insert(
      'food_items',
      food.toMap(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }
}

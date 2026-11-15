import 'dart:io';
import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/meal_entry.dart';
import '../state/meal_store.dart';
import 'add_meal_screen.dart';
import 'goal_settings_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final store = context.watch<MealStore>();
    return Scaffold(
      appBar: AppBar(
        title: const Text('KaloriTakip'),
        actions: [
          IconButton(
            icon: const Icon(Icons.flag_outlined),
            tooltip: 'Günlük hedef',
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute<void>(
                builder: (_) => const GoalSettingsScreen(),
              ),
            ),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Center(
            child: _CalorieRing(
              consumed: store.consumedKcal,
              target: store.targetKcal,
            ),
          ),
          const SizedBox(height: 24),
          for (final type in MealType.values) _MealSection(type: type),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => Navigator.of(context).push(
          MaterialPageRoute<void>(builder: (_) => const AddMealScreen()),
        ),
        icon: const Icon(Icons.add_a_photo_outlined),
        label: const Text('Yemek Ekle'),
      ),
    );
  }
}

class _CalorieRing extends StatelessWidget {
  const _CalorieRing({required this.consumed, required this.target});

  final int consumed;
  final int target;

  @override
  Widget build(BuildContext context) {
    final progress = target <= 0 ? 0.0 : consumed / target;
    final over = consumed > target;
    final remaining = target - consumed;
    return SizedBox(
      width: 240,
      height: 240,
      child: CustomPaint(
        painter: _RingPainter(progress: progress, over: over),
        child: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                '$consumed',
                style: Theme.of(context)
                    .textTheme
                    .headlineMedium
                    ?.copyWith(fontWeight: FontWeight.bold),
              ),
              Text('/ $target kcal',
                  style: Theme.of(context).textTheme.bodyMedium),
              const SizedBox(height: 4),
              Text(
                over ? '$remaining kcal aşıldı' : '$remaining kcal kaldı',
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                      color: over ? Colors.red : Colors.green,
                    ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _RingPainter extends CustomPainter {
  const _RingPainter({required this.progress, required this.over});

  final double progress;
  final bool over;

  @override
  void paint(Canvas canvas, Size size) {
    final center = size.center(Offset.zero);
    final radius = size.shortestSide / 2 - 12;
    final background = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = 16
      ..color = Colors.grey.shade300;
    final foreground = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = 16
      ..strokeCap = StrokeCap.round
      ..color = over ? Colors.red : Colors.green;

    canvas.drawCircle(center, radius, background);
    final sweep = 2 * math.pi * progress.clamp(0.0, 1.0);
    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      -math.pi / 2,
      sweep,
      false,
      foreground,
    );
  }

  @override
  bool shouldRepaint(covariant _RingPainter oldDelegate) =>
      oldDelegate.progress != progress || oldDelegate.over != over;
}

class _MealSection extends StatelessWidget {
  const _MealSection({required this.type});

  final MealType type;

  @override
  Widget build(BuildContext context) {
    final store = context.watch<MealStore>();
    final entries = store.entriesFor(type);
    final totalKcal =
        entries.fold<int>(0, (sum, entry) => sum + entry.kcalTotal.round());
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    child: Text(
                      '${type.label} • $totalKcal kcal',
                      style: Theme.of(context).textTheme.titleSmall,
                    ),
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.add),
                  tooltip: '${type.label} için ekle',
                  onPressed: () => Navigator.of(context).push(
                    MaterialPageRoute<void>(
                      builder: (_) => AddMealScreen(initialMealType: type),
                    ),
                  ),
                ),
              ],
            ),
            for (final entry in entries)
              ListTile(
                leading: entry.photoPath != null
                    ? ClipRRect(
                        borderRadius: BorderRadius.circular(8),
                        child: Image.file(
                          File(entry.photoPath!),
                          width: 48,
                          height: 48,
                          fit: BoxFit.cover,
                        ),
                      )
                    : const Icon(Icons.restaurant_outlined),
                title: Text(entry.name),
                subtitle: Text('${entry.grams.toStringAsFixed(0)} g'),
                trailing: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text('${entry.kcalTotal.toStringAsFixed(0)} kcal'),
                    IconButton(
                      icon: const Icon(Icons.delete_outline),
                      onPressed: () => store.deleteEntry(entry),
                    ),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }
}

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/daily_goal.dart';
import '../services/calorie_calculator.dart';
import '../state/meal_store.dart';

class GoalSettingsScreen extends StatefulWidget {
  const GoalSettingsScreen({super.key});

  @override
  State<GoalSettingsScreen> createState() => _GoalSettingsScreenState();
}

class _GoalSettingsScreenState extends State<GoalSettingsScreen> {
  final _targetController = TextEditingController();
  final _ageController = TextEditingController();
  final _heightController = TextEditingController();
  final _weightController = TextEditingController();

  Gender _gender = Gender.male;
  ActivityLevel _activity = ActivityLevel.moderate;
  GoalAdjustment _adjustment = GoalAdjustment.maintain;
  int? _suggestion;

  @override
  void dispose() {
    _targetController.dispose();
    _ageController.dispose();
    _heightController.dispose();
    _weightController.dispose();
    super.dispose();
  }

  int get _targetValue => int.tryParse(_targetController.text) ?? 0;

  void _calculate() {
    final age = int.tryParse(_ageController.text);
    final height =
        double.tryParse(_heightController.text.replaceAll(',', '.'));
    final weight =
        double.tryParse(_weightController.text.replaceAll(',', '.'));
    if (age == null ||
        age <= 0 ||
        height == null ||
        height <= 0 ||
        weight == null ||
        weight <= 0) {
      setState(() => _suggestion = null);
      return;
    }
    setState(() {
      _suggestion = CalorieCalculator.goalKcal(
        gender: _gender,
        age: age,
        heightCm: height,
        weightKg: weight,
        activity: _activity,
        adjustment: _adjustment,
      );
    });
  }

  Future<void> _save(int target, GoalSource source) async {
    if (target <= 0) return;
    await context.read<MealStore>().setGoal(target, source);
    if (!mounted) return;
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final store = context.watch<MealStore>();
    return Scaffold(
      appBar: AppBar(title: const Text('Günlük Hedef')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Text(
            'Mevcut hedef: ${store.targetKcal} kcal',
            style: Theme.of(context).textTheme.titleMedium,
          ),
          const SizedBox(height: 24),
          Text('Manuel hedef', style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 8),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _targetController,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'Günlük hedef (kcal)',
                    border: OutlineInputBorder(),
                  ),
                ),
              ),
              const SizedBox(width: 8),
              FilledButton(
                onPressed: () => _save(_targetValue, GoalSource.manual),
                child: const Text('Kaydet'),
              ),
            ],
          ),
          const Divider(height: 48),
          Text('Hesaplayıcı (öneri)',
              style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 8),
          SegmentedButton<Gender>(
            segments: const [
              ButtonSegment(value: Gender.male, label: Text('Erkek')),
              ButtonSegment(value: Gender.female, label: Text('Kadın')),
            ],
            selected: {_gender},
            onSelectionChanged: (selection) =>
                setState(() => _gender = selection.first),
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: _ageController,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'Yaş',
                    border: OutlineInputBorder(),
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: TextField(
                  controller: _heightController,
                  keyboardType:
                      const TextInputType.numberWithOptions(decimal: true),
                  decoration: const InputDecoration(
                    labelText: 'Boy (cm)',
                    border: OutlineInputBorder(),
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: TextField(
                  controller: _weightController,
                  keyboardType:
                      const TextInputType.numberWithOptions(decimal: true),
                  decoration: const InputDecoration(
                    labelText: 'Kilo (kg)',
                    border: OutlineInputBorder(),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          DropdownButtonFormField<ActivityLevel>(
            value: _activity,
            decoration: const InputDecoration(
              labelText: 'Aktivite seviyesi',
              border: OutlineInputBorder(),
            ),
            items: [
              for (final level in ActivityLevel.values)
                DropdownMenuItem(value: level, child: Text(level.label)),
            ],
            onChanged: (level) {
              if (level != null) setState(() => _activity = level);
            },
          ),
          const SizedBox(height: 12),
          DropdownButtonFormField<GoalAdjustment>(
            value: _adjustment,
            decoration: const InputDecoration(
              labelText: 'Hedef',
              border: OutlineInputBorder(),
            ),
            items: [
              for (final adjustment in GoalAdjustment.values)
                DropdownMenuItem(
                  value: adjustment,
                  child: Text(adjustment.label),
                ),
            ],
            onChanged: (adjustment) {
              if (adjustment != null) {
                setState(() => _adjustment = adjustment);
              }
            },
          ),
          const SizedBox(height: 12),
          FilledButton.tonal(
            onPressed: _calculate,
            child: const Text('Hesapla'),
          ),
          if (_suggestion != null) ...[
            const SizedBox(height: 12),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Expanded(
                      child: Text(
                        'Öneri: $_suggestion kcal',
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                    ),
                    FilledButton(
                      onPressed: () =>
                          _save(_suggestion!, GoalSource.calculated),
                      child: const Text('Uygula'),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }
}

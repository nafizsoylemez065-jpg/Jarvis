import 'dart:async';
import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../state/meal_store.dart';
import '../utils/date_key.dart';

class StatsScreen extends StatefulWidget {
  const StatsScreen({super.key});

  @override
  State<StatsScreen> createState() => _StatsScreenState();
}

class _StatsScreenState extends State<StatsScreen> {
  static const List<String> _weekdayLabels = [
    'Pt', 'Sa', 'Ça', 'Pe', 'Cu', 'Ct', 'Pz',
  ];

  @override
  void initState() {
    super.initState();
    unawaited(context.read<MealStore>().loadStats());
  }

  @override
  Widget build(BuildContext context) {
    final store = context.watch<MealStore>();
    final days = last7Days();
    final values = [for (final day in days) store.kcalByDay[day] ?? 0];
    final target = store.targetKcal;
    final labels = [
      for (final day in days)
        _weekdayLabels[DateTime.parse('$day 00:00:00').weekday - 1],
    ];
    final loggedValues = values.where((value) => value > 0).toList();
    final average = loggedValues.isEmpty
        ? 0
        : loggedValues.fold<int>(0, (sum, value) => sum + value) ~/
            loggedValues.length;
    final streak = _streak(values, target);

    return Scaffold(
      appBar: AppBar(title: const Text('İstatistik')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Son 7 gün (kcal)',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  const SizedBox(height: 16),
                  SizedBox(
                    height: 200,
                    width: double.infinity,
                    child: CustomPaint(
                      painter: _BarChartPainter(
                        values: values,
                        target: target,
                        labels: labels,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: _StatCard(
                  label: 'Hedefte kalma serisi',
                  value: '$streak gün',
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: _StatCard(
                  label: 'Ortalama (kaydedilen günler)',
                  value: '$average kcal',
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  int _streak(List<int> values, int target) {
    var streak = 0;
    for (var i = values.length - 1; i >= 0; i--) {
      final value = values[i];
      if (value > 0 && value <= target) {
        streak++;
      } else {
        break;
      }
    }
    return streak;
  }
}

class _StatCard extends StatelessWidget {
  const _StatCard({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Text(
              value,
              style: Theme.of(context)
                  .textTheme
                  .titleLarge
                  ?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 4),
            Text(
              label,
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.bodySmall,
            ),
          ],
        ),
      ),
    );
  }
}

class _BarChartPainter extends CustomPainter {
  const _BarChartPainter({
    required this.values,
    required this.target,
    required this.labels,
  });

  final List<int> values;
  final int target;
  final List<String> labels;

  @override
  void paint(Canvas canvas, Size size) {
    final maxValue = [target, ...values].reduce(math.max).clamp(1, 1 << 30);
    final chartHeight = size.height - 24;
    final barWidth = size.width / (values.length * 2);

    final targetY = chartHeight * (1 - target / maxValue);
    canvas.drawLine(
      Offset(0, targetY),
      Offset(size.width, targetY),
      Paint()
        ..color = Colors.green.shade300
        ..strokeWidth = 2,
    );

    final textStyle = TextStyle(fontSize: 11, color: Colors.grey.shade700);
    for (var i = 0; i < values.length; i++) {
      final x = barWidth * (2 * i + 0.5);
      final barHeight = chartHeight * values[i] / maxValue;
      final paint = Paint()
        ..style = PaintingStyle.fill
        ..color = values[i] > target
            ? Colors.red.shade300
            : Colors.green.shade400;
      canvas.drawRRect(
        RRect.fromRectAndRadius(
          Rect.fromLTWH(x, chartHeight - barHeight, barWidth, barHeight),
          const Radius.circular(4),
        ),
        paint,
      );
      final textPainter = TextPainter(
        text: TextSpan(text: labels[i], style: textStyle),
        textDirection: TextDirection.ltr,
      )..layout();
      textPainter.paint(
        canvas,
        Offset(x + barWidth / 2 - textPainter.width / 2, chartHeight + 4),
      );
    }
  }

  @override
  bool shouldRepaint(covariant _BarChartPainter oldDelegate) =>
      oldDelegate.values != values ||
      oldDelegate.target != target ||
      oldDelegate.labels != labels;
}

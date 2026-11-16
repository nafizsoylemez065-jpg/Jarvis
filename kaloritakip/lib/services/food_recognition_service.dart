import 'dart:io';
import 'dart:math' as math;

import 'package:image/image.dart' as img;
import 'package:tflite_flutter/tflite_flutter.dart';

import '../models/food_item.dart';

class FoodLabel {
  const FoodLabel(this.foodName, this.kcalPer100g);

  final String foodName;
  final double kcalPer100g;
}

/// Modelin çıktı etiketleri. Sıra, `assets/models/food_model.tflite`
/// içindeki sınıf sırasıyla birebir aynı olmalıdır.
const List<FoodLabel> kFoodLabels = [
  FoodLabel('Mercimek çorbası', 86),
  FoodLabel('Ezogelin çorbası', 95),
  FoodLabel('Tavuk göğsü (ızgara)', 165),
  FoodLabel('Pilav (beyaz)', 130),
  FoodLabel('Bulgur pilavı', 112),
  FoodLabel('Kuru fasulye', 127),
  FoodLabel('Nohut yemeği', 164),
  FoodLabel('Köfte', 250),
  FoodLabel('Döner (et)', 215),
  FoodLabel('Lahmacun', 260),
  FoodLabel('Pide (kıymalı)', 255),
  FoodLabel('Börek (kıymalı)', 290),
  FoodLabel('Menemen', 130),
  FoodLabel('Makarna (soslu)', 158),
  FoodLabel('Salata (çoban)', 60),
  FoodLabel('Yoğurt', 61),
  FoodLabel('Elma', 52),
  FoodLabel('Muz', 89),
  FoodLabel('Portakal', 47),
  FoodLabel('Baklava', 428),
];

class FoodRecognitionService {
  FoodRecognitionService();

  static const String _modelAsset = 'assets/models/food_model.tflite';
  static const int _inputSize = 224;

  Interpreter? _interpreter;

  bool get isModelLoaded => _interpreter != null;

  Future<void> load() async {
    if (_interpreter != null) return;
    try {
      _interpreter = await Interpreter.fromAsset(_modelAsset);
    } catch (_) {
      _interpreter = null;
    }
  }

  Future<List<FoodItem>> recognize(File image) async {
    final interpreter = _interpreter;
    if (interpreter == null || kFoodLabels.isEmpty) return const [];

    final raw = await image.readAsBytes();
    final decoded = img.decodeImage(raw);
    if (decoded == null) return const [];
    final resized = img.copyResize(
      decoded,
      width: _inputSize,
      height: _inputSize,
    );

    final pixels = List.generate(
      _inputSize,
      (y) => List.generate(
        _inputSize,
        (x) {
          final pixel = resized.getPixel(x, y);
          return <double>[pixel.r / 255.0, pixel.g / 255.0, pixel.b / 255.0];
        },
      ),
    );

    final output = [
      List<double>.filled(kFoodLabels.length, 0.0),
    ];
    interpreter.run([pixels], output);

    final scores = softmax(output[0]);
    return [
      for (final index in topCandidates(scores, 3))
        FoodItem(
          name: kFoodLabels[index].foodName,
          kcalPer100g: kFoodLabels[index].kcalPer100g,
          source: FoodSource.cache,
        ),
    ];
  }

  static List<double> softmax(List<double> values) {
    if (values.isEmpty) return const [];
    final max = values.reduce(math.max);
    final exps = [for (final value in values) math.exp(value - max)];
    final sum = exps.fold<double>(0, (total, value) => total + value);
    return [for (final value in exps) value / sum];
  }

  static List<int> topCandidates(List<double> scores, int count) {
    final indices = List<int>.generate(scores.length, (index) => index);
    indices.sort((a, b) => scores[b].compareTo(scores[a]));
    return indices.take(count).toList();
  }
}

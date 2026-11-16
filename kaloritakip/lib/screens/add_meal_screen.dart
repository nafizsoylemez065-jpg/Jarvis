import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';

import '../models/food_item.dart';
import '../models/meal_entry.dart';
import '../services/food_recognition_service.dart';
import '../state/meal_store.dart';

class AddMealScreen extends StatefulWidget {
  const AddMealScreen({super.key, this.initialMealType = MealType.breakfast});

  final MealType initialMealType;

  @override
  State<AddMealScreen> createState() => _AddMealScreenState();
}

class _AddMealScreenState extends State<AddMealScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _gramsController = TextEditingController(text: '100');
  final _kcalController = TextEditingController();
  final _picker = ImagePicker();
  final _recognizer = FoodRecognitionService();

  MealType _mealType = MealType.breakfast;
  String? _photoPath;
  List<FoodItem> _suggestions = [];
  List<FoodItem> _photoCandidates = [];

  @override
  void initState() {
    super.initState();
    _mealType = widget.initialMealType;
    unawaited(_recognizer.load());
  }

  @override
  void dispose() {
    _nameController.dispose();
    _gramsController.dispose();
    _kcalController.dispose();
    super.dispose();
  }

  double get _grams =>
      double.tryParse(_gramsController.text.replaceAll(',', '.')) ?? 0;
  double get _kcalPer100g =>
      double.tryParse(_kcalController.text.replaceAll(',', '.')) ?? 0;
  double get _previewKcal => _kcalPer100g * _grams / 100;

  Future<void> _pickImage(ImageSource source) async {
    final picked = await _picker.pickImage(
      source: source,
      maxWidth: 1600,
      imageQuality: 80,
    );
    if (picked == null) return;
    final candidates = await _recognizer.recognize(File(picked.path));
    setState(() {
      _photoPath = picked.path;
      _photoCandidates = candidates;
    });
  }

  void _applyFood(FoodItem food) {
    setState(() {
      _nameController.text = food.name;
      _kcalController.text = food.kcalPer100g.toStringAsFixed(0);
      _suggestions = [];
    });
  }

  Future<void> _search(String query) async {
    final trimmed = query.trim();
    final results = trimmed.isEmpty
        ? <FoodItem>[]
        : await context.read<MealStore>().searchFoods(trimmed);
    if (!mounted) return;
    setState(() => _suggestions = results);
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    await context.read<MealStore>().addEntry(
          mealType: _mealType,
          name: _nameController.text.trim(),
          grams: _grams,
          kcalPer100g: _kcalPer100g,
          photoPath: _photoPath,
        );
    if (!mounted) return;
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Yemek Ekle')),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            SegmentedButton<MealType>(
              segments: [
                for (final type in MealType.values)
                  ButtonSegment(value: type, label: Text(type.label)),
              ],
              selected: {_mealType},
              onSelectionChanged: (selection) =>
                  setState(() => _mealType = selection.first),
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => _pickImage(ImageSource.camera),
                    icon: const Icon(Icons.photo_camera_outlined),
                    label: const Text('Fotoğraf Çek'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => _pickImage(ImageSource.gallery),
                    icon: const Icon(Icons.photo_outlined),
                    label: const Text('Galeriden'),
                  ),
                ),
              ],
            ),
            if (_photoPath != null) ...[
              const SizedBox(height: 12),
              ClipRRect(
                borderRadius: BorderRadius.circular(12),
                child: Image.file(
                  File(_photoPath!),
                  height: 180,
                  fit: BoxFit.cover,
                ),
              ),
            ],
            if (_photoCandidates.isNotEmpty) ...[
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                children: [
                  for (final food in _photoCandidates)
                    ActionChip(
                      label: Text(
                        '${food.name} • ${food.kcalPer100g.toStringAsFixed(0)} kcal',
                      ),
                      onPressed: () => _applyFood(food),
                    ),
                ],
              ),
            ] else if (_photoPath != null) ...[
              const SizedBox(height: 8),
              Text(
                'Model aday yemek bulamadı; yemeği elle girin.',
                style: Theme.of(context).textTheme.bodySmall,
              ),
            ],
            const SizedBox(height: 16),
            TextFormField(
              controller: _nameController,
              decoration: const InputDecoration(
                labelText: 'Yemek adı',
                border: OutlineInputBorder(),
              ),
              onChanged: _search,
              validator: (value) => (value == null || value.trim().isEmpty)
                  ? 'Yemek adı gerekli'
                  : null,
            ),
            for (final food in _suggestions)
              ListTile(
                dense: true,
                title: Text(food.name),
                trailing:
                    Text('${food.kcalPer100g.toStringAsFixed(0)} kcal/100g'),
                onTap: () => _applyFood(food),
              ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: TextFormField(
                    controller: _gramsController,
                    keyboardType:
                        const TextInputType.numberWithOptions(decimal: true),
                    decoration: const InputDecoration(
                      labelText: 'Gram',
                      border: OutlineInputBorder(),
                    ),
                    onChanged: (_) => setState(() {}),
                    validator: (value) =>
                        _grams <= 0 ? 'Geçerli bir gram girin' : null,
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: TextFormField(
                    controller: _kcalController,
                    keyboardType:
                        const TextInputType.numberWithOptions(decimal: true),
                    decoration: const InputDecoration(
                      labelText: 'kcal / 100 g',
                      border: OutlineInputBorder(),
                    ),
                    onChanged: (_) => setState(() {}),
                    validator: (value) =>
                        _kcalPer100g < 0 ? 'Geçerli bir değer girin' : null,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const Text('Toplam'),
                    Text(
                      '${_previewKcal.toStringAsFixed(0)} kcal',
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 8),
            FilledButton(
              onPressed: _save,
              child: const Text('Kaydet'),
            ),
          ],
        ),
      ),
    );
  }
}

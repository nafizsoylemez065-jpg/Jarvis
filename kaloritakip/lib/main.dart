import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'data/app_database.dart';
import 'screens/home_screen.dart';
import 'state/meal_store.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final database = AppDatabase();
  final store = MealStore(database);
  await store.loadToday();
  runApp(KaloriTakipApp(store: store));
}

class KaloriTakipApp extends StatelessWidget {
  const KaloriTakipApp({super.key, required this.store});

  final MealStore store;

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider.value(
      value: store,
      child: MaterialApp(
        title: 'KaloriTakip',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          colorSchemeSeed: Colors.green,
          useMaterial3: true,
        ),
        home: const HomeScreen(),
      ),
    );
  }
}

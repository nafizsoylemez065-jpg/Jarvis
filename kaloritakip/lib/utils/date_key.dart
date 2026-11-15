String dateKey(DateTime date) {
  final month = date.month.toString().padLeft(2, '0');
  final day = date.day.toString().padLeft(2, '0');
  return '${date.year}-$month-$day';
}

String todayKey() => dateKey(DateTime.now());

List<String> last7Days() {
  final now = DateTime.now();
  return [
    for (var i = 6; i >= 0; i--) dateKey(now.subtract(Duration(days: i))),
  ];
}

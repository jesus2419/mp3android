import 'package:flutter/material.dart';

class MetricsScreen extends StatelessWidget {
  const MetricsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    // Aquí se mostrarán las métricas de uso/reproducción
    return Scaffold(
      appBar: AppBar(title: const Text('Métricas')),
      body: const Center(child: Text('Métricas (por implementar)')),
    );
  }
}

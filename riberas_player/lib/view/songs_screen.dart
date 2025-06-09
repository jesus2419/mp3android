import 'package:flutter/material.dart';

class SongsScreen extends StatelessWidget {
  const SongsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    // Aquí se listarán los archivos mp3/mp4 encontrados en el dispositivo
    return Scaffold(
      appBar: AppBar(title: const Text('Canciones')),
      body: const Center(child: Text('Lista de canciones (por implementar)')),
    );
  }
}

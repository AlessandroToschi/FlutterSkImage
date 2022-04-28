import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' as ui;
import 'package:flutter/material.dart';
import 'dart:ffi' as ffi;

class t_result extends ffi.Struct {
  external ffi.Pointer<ffi.Uint8> pointer;
  @ffi.Uint64()
  external int width;
  @ffi.Uint64()
  external int height;
  @ffi.Uint64()
  external int bytesPerRow;
}

class p_result extends ffi.Struct {
  external ffi.Pointer<t_result> textures;

  @ffi.Uint64()
  external int length;
}

final nativeLib = Platform.isAndroid ? ffi.DynamicLibrary.open('libffi_bridge.so') : ffi.DynamicLibrary.process();

final pointerBridge = nativeLib.lookupFunction<t_result Function(), t_result Function()>('pointer_bridge');
final multipleTexturesBridge =
    nativeLib.lookupFunction<p_result Function(), p_result Function()>('multiple_textures_bridge');

final image = ValueNotifier<ui.Image?>(null);
final image2 = ValueNotifier<ui.Image?>(null);

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key, required this.title}) : super(key: key);

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;

  void _incrementCounter() async {
    /*
    final res = pointerBridge();
    final texturePointer = res.pointer.address;
    final s = DateTime.now().microsecondsSinceEpoch;
    final startTime = DateTime.now().microsecondsSinceEpoch;
    print('T1 ${DateTime.now().microsecondsSinceEpoch - startTime}');
    image.value = await ui.Image.fromTexture(texturePointer, res.width, res.height);
    print('TF ${DateTime.now().microsecondsSinceEpoch - s}');
     */
    final startTime = DateTime.now().microsecondsSinceEpoch;
    final p_result = multipleTexturesBridge();
    final textureDescriptors = <ui.TextureDescriptor>[];
    for (var i = 0; i < p_result.length; i++) {
      final t_res = p_result.textures[i];
      textureDescriptors.add(
        ui.TextureDescriptor(
          pointer: t_res.pointer.address,
          width: t_res.width,
          height: t_res.height,
        ),
      );
    }
    final textures = ui.Image.fromTextures(textureDescriptors);
    image.value = textures[0];
    //image2.value = textures[1];
    print('TF ${DateTime.now().microsecondsSinceEpoch - startTime}');
  }

  Future<ui.Image> _decodeImageFromPixels(
      Uint8List pixels, int width, int height, int rowBytes, ui.PixelFormat format) async {
    final startTime = DateTime.now().microsecondsSinceEpoch;
    final buffer = await ui.ImmutableBuffer.fromUint8List(pixels);
    print('T1 ${DateTime.now().microsecondsSinceEpoch - startTime}');
    final descriptor = ui.ImageDescriptor.raw(
      buffer,
      width: width,
      height: height,
      rowBytes: rowBytes,
      pixelFormat: format,
    );

    final codec = await descriptor.instantiateCodec(
      targetWidth: width,
      targetHeight: height,
    );
    print('T3 ${DateTime.now().microsecondsSinceEpoch - startTime}');

    final frameInfo = await codec.getNextFrame();
    print('T4 ${DateTime.now().microsecondsSinceEpoch - startTime}');

    codec.dispose();
    buffer.dispose();
    descriptor.dispose();

    print('T5 ${DateTime.now().microsecondsSinceEpoch - startTime}');

    return frameInfo.image;
  }

  @override
  Widget build(BuildContext context) {
    // This method is rerun every time setState is called, for instance as done
    // by the _incrementCounter method above.
    //
    // The Flutter framework has been optimized to make rerunning build methods
    // fast, so that you can just rebuild anything that needs updating rather
    // than having to individually change instances of widgets.
    return Scaffold(
      appBar: AppBar(
        // Here we take the value from the MyHomePage object that was created by
        // the App.build method, and use it to set our appbar title.
        title: Text(widget.title),
      ),
      body: Center(
        // Center is a layout widget. It takes a single child and positions it
        // in the middle of the parent.
        child: Column(
          children: [
            ValueListenableBuilder<ui.Image?>(
              valueListenable: image,
              builder: (context, image, child) => RawImage(image: image),
            ),
            ValueListenableBuilder<ui.Image?>(
              valueListenable: image2,
              builder: (context, image, child) => RawImage(image: image),
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _incrementCounter,
        tooltip: 'Increment',
        child: const Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}

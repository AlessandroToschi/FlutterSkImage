import UIKit
import Flutter
import Metal
import MetalKit

struct Texture {
    var texture: MTLTexture?
    var width: Int
    var height: Int
}

var ptr: UnsafeRawPointer? = nil
var width: Int = 0
var height: Int = 0
var bytesPerRow: Int = 0

var image: UIImage?
var cgImage: CGImage?
var pixelBuffer: CVPixelBuffer?
var data: Data?

var texture: MTLTexture?
var texture2: MTLTexture?

let device = MTLCreateSystemDefaultDevice()!
let textureLoader = MTKTextureLoader(device: device)

var t1: Texture?
var t2: Texture?

func loadTextures() {
    
    guard let _image = UIImage(named: "texture"),
          let _cgImage = _image.cgImage
    else { return }
    
    t1 = Texture(
        texture: try! textureLoader.newTexture(cgImage: _cgImage, options: [.SRGB: false]),
        width: _cgImage.width,
        height: _cgImage.height
    )

    guard let _image = UIImage(named: "texture2"),
          let _cgImage = _image.cgImage
    else { return }
    
    t2 = Texture(
        texture: try! textureLoader.newTexture(cgImage: _cgImage, options: [.SRGB: false]),
        width: _cgImage.width,
        height: _cgImage.height
    )
    
}


@_cdecl("pointer_bridge")
func passPointer() -> t_result {
    
    print(texture!.width)
    
    DispatchQueue.main.async {
        
        texture = try! textureLoader.newTexture(cgImage: cgImage!, options: [.SRGB: false])
    }
    
    return t_result(ptr: UnsafeRawPointer(Unmanaged.passRetained(texture!).toOpaque()), width: UInt64(width), height: UInt64(height), bytesPerRow: UInt64(bytesPerRow))
}

@_cdecl("multiple_textures_bridge")
func texturesBridge() -> p_result {
    
    let pointer = UnsafeMutablePointer<t_result>.allocate(capacity: 2)
    let bufferPointer = UnsafeMutableBufferPointer(start: pointer, count: 2)
    
    bufferPointer[0] = t_result(
        ptr: UnsafeRawPointer(Unmanaged.passRetained(t1!.texture!).toOpaque()),
        width: UInt64(t1!.width),
        height: UInt64(t1!.height),
        bytesPerRow: 0
    )
    
    bufferPointer[1] = t_result(
        ptr: UnsafeRawPointer(Unmanaged.passRetained(t2!.texture!).toOpaque()),
        width: UInt64(t2!.width),
        height: UInt64(t2!.height),
        bytesPerRow: 0
    )
    
    
    return p_result(textures: pointer, length: 2)
    
}


@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    
    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        GeneratedPluginRegistrant.register(with: self)
        loadTextures()
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }

}

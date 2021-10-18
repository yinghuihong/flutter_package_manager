package dev.wurikiji.flutter_package_manager

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import androidx.annotation.NonNull
import io.flutter.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.JSONMethodCodec
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import org.json.JSONArray
import java.io.ByteArrayOutputStream
import java.lang.Exception



//const val METHOD_CHANNEL = "dev.wurikiji.flutter_package_manager.method_channel"
const val TAG = "Flutter Package Manager"
class FlutterPackageManagerPlugin: FlutterPlugin, MethodCallHandler {

  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var sContext: Context

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_package_manager", JSONMethodCodec.INSTANCE)
    channel.setMethodCallHandler(this)
    sContext = flutterPluginBinding.applicationContext
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

//  companion object {
//    var sContext: Context? = null
//    @JvmStatic
//    fun registerWith(registrar: Registrar) {
//      val channel = MethodChannel(registrar.messenger(),
//              METHOD_CHANNEL,
//              JSONMethodCodec.INSTANCE)
//      channel.setMethodCallHandler(FlutterPackageManagerPlugin())
//      sContext = registrar.context().applicationContext
//      Log.i(TAG, "Register with ${registrar.context().packageName}")
//    }
//  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when(call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      "getPackageInfo" -> {
          val args = call.arguments as JSONArray
          result.success(getPackageInfo(args[0] as String))
      }
      "getInstalledPackages" -> {
          result.success(getInstalledPackages())
      }
      "getUserInstalledPackages" -> {
        result.success(getInstalledPackages(true))
      }
      else -> {
        result.notImplemented()
      }
    }
  }

    /// get all installed packages's package name
  private fun getInstalledPackages(userInstalled: Boolean = false): ArrayList<String> {
    val ret = ArrayList<String>()
    sContext
            .packageManager
            .getInstalledPackages(0)
            .forEach {
                var pName: String? = it.packageName
                if (userInstalled) {
                  val isSystemApp = (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) !== 0
                  if (isSystemApp) pName = null
                }
              if (pName != null)
                ret.add(pName)
            }
    return ret
  }

  /// get package name, app name, app icon
  private fun getPackageInfo(packageName: String) : java.util.HashMap<String, Any?>? {
    try {
      val info: java.util.HashMap<String, Any?> = java.util.HashMap()
      val appInfo: ApplicationInfo = sContext.packageManager
              .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
      val appName: String = sContext.packageManager.getApplicationLabel(appInfo).toString()
      val appIcon: Drawable = sContext.packageManager.getApplicationIcon(appInfo.packageName)
      val byteImage = drawableToBase64String(appIcon)

      info["packageName"] = appInfo.packageName
      info["appName"] = appName
      info["appIcon"] = byteImage
      Log.i(TAG, "xxx get the Package $packageName Info $info")
      return info
    } catch (e: Exception) {
      Log.e(TAG, "xxx $packageName not installed", e)
        return null
    }
  }

  /// get bitmap style drawable
  private fun drawableToBitmap(drawable: Drawable) : Bitmap {
    val bitmap: Bitmap?

    if (drawable is BitmapDrawable) {
      if (drawable.bitmap != null) {
        return drawable.bitmap
      }
    }

    if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
      bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
    } else {
      bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    }

    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
  }

    /// get base64 encoded string from drawable
  private fun drawableToBase64String(drawable: Drawable) : String{
    val bitmap: Bitmap = drawableToBitmap(drawable)
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
    val b = baos.toByteArray()
    return Base64.encodeToString(b, Base64.DEFAULT)
  }
}

import 'dart:async';

import 'package:flutter/services.dart';

import 'src/package_info.dart';

export 'src/package_info.dart';

/// The reflection of PackageManager on Android
class FlutterPackageManager {
  /// Method channel
  static const MethodChannel _channel = const MethodChannel('flutter_package_manager', JSONMethodCodec());

  /// Get platform version of the device
  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  /// Get package information of the `name` package
  /// Return: `PackageInfo` class
  static Future<PackageInfo?> getPackageInfo(String name) async {
    Map? result = await _channel.invokeMethod('getPackageInfo', <dynamic>[name]);
    return result != null ? PackageInfo.fromMap(result) : null;
  }

  /// Get the `List<String>` of the installed applications.
  /// This includes the system apps.
  /// You can use this name as a parameter of `getPackageInfo()` call.
  static Future<List?> getInstalledPackages() async {
    return await _channel.invokeMethod('getInstalledPackages');
  }

  /// Get the `List<String>` of the ***user installed*** applications.
  /// This includes the system apps.
  /// You can use this name as a parameter of `getPackageInfo()` call.
  static Future<List?> getUserInstalledPackages() async {
    return await _channel.invokeMethod('getUserInstalledPackages');
  }
}

# Keep DeviceAdminReceiver and AccessibilityService entry points intact for R8 release builds
-keep class com.mim.controller.admin.MimDeviceAdminReceiver { *; }
-keep class com.mim.controller.service.MimAccessibilityService { *; }
-keep class com.mim.controller.service.MimFloatingService { *; }

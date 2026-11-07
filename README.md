# Aether Launcher

Early Android launcher MVP for TECNO SPARK 40 Pro+.

## Build APK with GitHub Actions
1. Upload the contents of this folder to your GitHub repository, replacing existing files when prompted.
2. Open **Actions** and allow workflows if GitHub asks.
3. Select **Build Aether Launcher APK** and press **Run workflow**. A push to `main` or `master` also starts a build.
4. Open the completed run and download **Aether-Launcher-debug-apk** under Artifacts.
5. Extract the ZIP and install `app-debug.apk` on your phone. You may need to allow installation from your browser/file manager.
6. Press Home and choose Aether Launcher.

## Current MVP
- Real Android HOME/launcher activity.
- Installed-app drawer with search and app launching.
- Home screen with quick actions.
- Theme selection screen (visual presets).
- Wallpaper picker and apply to system wallpaper.
- Settings screen and shortcut to Android's default Home app settings.

## Build workflow fix
The GitHub Actions workflow installs only valid SDK packages (`platform-tools`, `platforms;android-35`, and `build-tools;35.0.0`). It does not request the obsolete SDK package named `tools`, which caused the previous failure.

## Important limitations
This is a working starter app, not the full feature set in the original design brief. Custom free-position/resizable widgets, full lock-screen replacement, depth-clock subject masking, and advanced folder editing still need implementation. Android does not allow a third-party launcher to replace secure PIN/password/biometric lock-screen handling. The secure lock screen remains controlled by Android/HiOS.

Debug APK is unsigned for Play Store distribution. For personal installation, Android may show a warning because it is not Play-signed.

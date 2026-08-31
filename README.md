# Float Keys

A tiny Android floating shortcut pad designed for tablet workflows such as Figma in a browser.

## What this build does

- Four configurable shortcut buttons.
- Ctrl / Shift / Alt / Meta modifiers.
- Common keyboard keys A-Z, 0-9, arrows, Enter, Esc, Delete, brackets, etc.
- Circular buttons arranged in an arc.
- Draggable centre hub.
- Overlay stays above normal apps.
- The overlay windows do not take keyboard focus.
- Samsung Keyboard / Gboard can remain your normal text keyboard.
- Uses Shizuku to inject real Android key combinations.
- No Android Studio is required to build the APK.

## Important compatibility requirement

The shortcut injection uses:

`input keycombination <keycode> <keycode> ...`

This command is available on Android 14+.

For a recent Samsung Galaxy Tab running Android 14/15/16 this is the intended path.

## Build entirely in GitHub

1. Create a new GitHub repository.
2. Upload the contents of this folder to the repository root.
3. Open the repository's **Actions** tab.
4. Select **Build Android APK**.
5. Click **Run workflow**.
6. When the job finishes, open it and download the `FloatKeys-debug-apk` artifact.
7. Unzip the downloaded artifact. Inside is `app-debug.apk`.
8. Send the APK to your tablet and install it. Android may ask you to allow installs from your browser/files app.

The workflow also builds automatically whenever you push to `main`.

## Tablet setup

### 1. Install and start Shizuku

Install the official Shizuku app.

On Samsung / Android 11+ the convenient no-PC method is Wireless debugging:

1. Settings → About tablet → Software information.
2. Tap **Build number** seven times to enable Developer options.
3. Settings → Developer options → Wireless debugging → ON.
4. Open Shizuku and choose **Start via Wireless debugging**.
5. Follow Shizuku's pairing-code steps.
6. Start the Shizuku service.

Shizuku normally has to be started again after a reboot.

### 2. Open Float Keys

The app shows two setup statuses:

- **Overlay permission**
- **Shizuku**

Tap **Allow display over other apps** and enable Float Keys.

Then tap **Connect Shizuku** and approve Shizuku's permission dialog.

### 3. Configure the four keys

Tap any of the four shortcut rows.

Example mappings for Figma:

- Key 1: Ctrl + Z
- Key 2: Ctrl + Shift + Z
- Key 3: V
- Key 4: I

You can change these at any time.

### 4. Start the overlay

Tap **Start floating keys**.

The app moves to the background and the circular controls remain visible.

- Drag the dark centre button to move the whole control.
- Tap the centre button to collapse / expand the arc.
- Tap `×` to close it.
- Use the normal Samsung Keyboard whenever a real text field needs typing.

## Why Shizuku is used

Android intentionally prevents ordinary third-party apps from injecting arbitrary keyboard events into other apps.

Turning Float Keys into an Input Method Editor would make it compete with Samsung Keyboard. That is not the desired UX.

Shizuku runs a user service with Android shell privileges. This starter app uses that privileged process only to run Android's built-in `input keycombination` command.

## Privacy

This starter does not contain Internet permission, analytics, ads, accounts, or keyboard logging.

It stores only your four shortcut mappings in local SharedPreferences.

## One UI styling

This project uses a One UI-inspired visual direction: large page title, roomy vertical rhythm, rounded white settings cards, circular floating controls, restrained blue accent.

It does not use proprietary Samsung UI code.

## Current MVP limitations

- Requires Android 14+ for `input keycombination`.
- Shizuku must be running.
- The arc currently opens toward the upper-left of the centre button. Move the hub if it gets near an edge.
- There are four shortcut slots only.
- Mouse right-click is not implemented in this first build.
- This is a debug APK. It is fine for personal sideloading, but a public Play Store release needs release signing, policy declarations, icon/branding work, testing, and foreground-service review.

## Suggested next improvements

- Profiles, e.g. Figma / Browser / Blender Remote.
- Per-app auto profiles.
- Arc-direction detection near screen edges.
- 4 / 6 / 8-button layouts.
- Holdable modifier mode: tap Ctrl once, tap another floating key next.
- Right-click / mouse action exploration.
- Haptic feedback.
- Transparency and size controls.
- Import/export shortcut profiles.

# Ionosonde

**Solar & Radio Propagation Conditions for Amateur Radio Operators**

Ionosonde is an Android app that displays real-time solar and radio propagation data at a glance. Designed for amateur (ham) radio operators, it presents solar indices, HF/VHF band conditions, K-index history, SFI forecasts, aurora predictions, and a live solar terminator world map.

## Features

### Dashboard
- **Solar indices**: SFI, Sunspot Number, A-index, K-index, X-ray class — all colour-coded (green/amber/red) by standard thresholds
- **Space environment**: Proton flux, electron flux, solar wind speed, Bz, geomagnetic status, signal noise, aurora activity and estimated visible latitude
- **HF band conditions**: 80–40m, 30–20m, 17–15m, 12–10m with Day/Night status (Poor/Fair/Good)
- **VHF conditions**: Aurora, 6m/4m/2m Es (EU & NA), EME degradation, MUF
- **Solar flare probability**
- Tap any index label for a plain-English explanation

### K-Index History
- Bar chart of the last 48 hours of planetary K-index values
- Bars colour-coded by severity (green K0–2, amber K3–4, red K5+)

### 27-Day SFI Forecast
- Line graph with horizontal reference lines at SFI 100 and 150

### Aurora Tab
- Current K-index and estimated aurora visibility latitude
- 24-hour K-index forecast bar chart
- Plain-text aurora visibility assessment for mid-latitudes

### Sunlit World Map
- Interactive map using OSMDroid with OpenStreetMap tiles
- Real-time solar terminator overlay (refreshed every 60 seconds, pure time calculation)
- Subsolar point marker
- MUF values at 8 reference stations (Boulder, Alaska, Sondrestrom, Tromsø, Athens, Ascension, Hermanus, Darwin)
- Tile caching for offline use

### Home Screen Widgets (Jetpack Glance)
- **Small (2×1)**: K-index value with colour-coded background and geomagnetic status
- **Medium (4×2)**: SFI, SN, A-index, K-index + HF band conditions grid
- **Large (4×4)**: Full dashboard data + VHF conditions + simplified solar terminator map
- Customisable background colour/opacity and text colour

### Settings
- Refresh interval: 15 min / 30 min / 1 hour / manual only
- Notification thresholds: K-index level, flare class, proton event toggle
- Widget appearance: background colour with alpha slider, text colour picker, fixed vs custom condition colours
- Time format: UTC only or local + UTC
- Theme: light / dark / system default

### Notifications
- Space Weather Alerts channel
- Alerts for K-index threshold, X-ray flare class threshold, and proton events
- Each notification includes a plain-English explanation of the impact on radio conditions

## Data Sources & Attribution

- **N0NBH Solar Data Feed** — Solar and band condition data provided by Paul L. Herrman, N0NBH, via the [HamQSL.com](https://www.hamqsl.com/) XML data feed. This application uses only parsed data values and does not reproduce or embed any copyrighted imagery.
- **NOAA Space Weather Prediction Center (SWPC)** — Planetary K-index history, 27-day SFI forecast, and aurora K-index forecast from [services.swpc.noaa.gov](https://services.swpc.noaa.gov/).
- **OpenStreetMap** — Map tiles © OpenStreetMap contributors, licensed under the [Open Data Commons Open Database License (ODbL)](https://opendatacommons.org/licenses/odbl/).

## Build Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1) or later
- JDK 17
- Android SDK with API level 35 installed

### Build
```bash
# Clone the repository
git clone https://github.com/hiraethclub/ionosonde.git
cd ionosonde

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### Project Structure
```
app/src/main/java/club/hiraeth/ionosonde/
├── IonosondeApp.kt              # Application class
├── data/
│   ├── local/                   # Room database & DAO
│   ├── model/                   # Data entities
│   ├── preferences/             # DataStore user preferences
│   ├── remote/                  # Retrofit API services & parsers
│   ├── repository/              # Single source of truth
│   └── worker/                  # WorkManager background refresh
├── notification/                # Space weather alert notifications
├── ui/
│   ├── MainActivity.kt
│   ├── navigation/              # Bottom nav & routes
│   ├── theme/                   # Material 3 theme
│   ├── components/              # Shared UI components
│   ├── dashboard/               # Home screen
│   ├── kindex/                  # K-index chart
│   ├── sfi/                     # SFI forecast chart
│   ├── aurora/                  # Aurora tab
│   ├── map/                     # Sunlit world map (OSMDroid)
│   ├── settings/                # Settings screen
│   └── about/                   # About & attribution
└── widget/                      # Glance widgets (small, medium, large)
```

## Widget Configuration Notes

- **Adding widgets**: Long-press your home screen → Widgets → Ionosonde → choose Small, Medium, or Large
- **Customising appearance**: Open the app → Settings → Widget Appearance
  - Background opacity slider (0–100%) controls transparency over your wallpaper
  - Default: semi-transparent dark (~60% opacity black) for readability on varied wallpapers
  - Text colour defaults to white; choose from the palette or use the accent colour option
- **Update frequency**: Widgets update on the same schedule as the app's background data refresh (default 15 minutes)
- **Tapping a widget** opens the main app dashboard

## Tech Stack

- Kotlin, Jetpack Compose (Material 3)
- Jetpack Glance for home screen widgets
- WorkManager for periodic background data refresh
- Room for local data caching
- Retrofit + OkHttp for networking
- XmlPullParser for N0NBH XML feed parsing
- Kotlin Serialization for JSON parsing
- OSMDroid 6.x for interactive maps
- Canvas-based charting (Compose Canvas API)
- DataStore for user preferences
- Minimum SDK 26 (Android 8.0), Target SDK 35

## Licence

See [LICENSE](LICENSE) for details.

# Ionosonde Viz - Amiga Audio Visualizer

A WinAmp-style audio visualizer for Amiga 1200 + PiStorm. Displays real-time
oscilloscope waveforms and spectrum analyzer bars for any sounds playing through
Workbench.

## Features

- **Oscilloscope** - Real-time waveform display with filled area
- **Spectrum Analyzer** - 32-bar frequency display with WinAmp-style gradient
  (green -> yellow -> red) and falling peak dots
- **Three display modes** - Dual (scope+bars), Scope only, Bars only
- **Audio capture** - Hooks audio.device to capture sound from any OS-friendly
  application; detects hardware-banging audio via DMA status
- **Double-buffered** - Smooth flicker-free animation
- **AGA colors** - 256-color palette for rich gradients
- **Idle animation** - Gentle sine wave when no audio is playing

## Requirements

- Amiga 1200 (or 68020+ Amiga with AGA)
- PiStorm recommended (runs fine without, just slower)
- Kickstart 3.0+ (V39)
- 1MB Chip RAM

## Controls

| Key        | Action            |
|------------|-------------------|
| ESC        | Quit              |
| M          | Cycle display mode|
| Left Click | Cycle display mode|

## Building

### Option 1: VBCC Cross-Compiler (recommended)

Install VBCC from http://www.compilers.de/vbcc.html with the 68k AmigaOS target.

```bash
export NDK_INC=/path/to/ndk3.9/include
make
```

### Option 2: Bebbo's GCC Cross-Compiler

Install from https://github.com/bebbo/amiga-gcc

```bash
export NDK_INC=/path/to/ndk/include
make TOOLCHAIN=gcc
```

### Option 3: Native compilation on Amiga

Copy the `src/` directory to your Amiga and compile with SAS/C or VBCC native:

```
vc -O2 -cpu=68020 -o ionoviz src/main.c src/fft.c src/audio_capture.c -lamiga
```

## How It Works

### Audio Capture

The program uses two strategies to capture audio:

1. **audio.device hook** - Patches the BeginIO vector of audio.device to
   intercept CMD_WRITE requests. This captures the sample pointer, length,
   period, and volume for each channel. Works with any program that plays audio
   through the OS (system sounds, MUI apps, etc.)

2. **Paula DMA detection** - Reads the DMACONR register to detect if audio DMA
   channels are active. This detects hardware-banging players (ProTracker, etc.)
   but cannot capture their waveform data directly.

### Visualization

- Captured audio from all 4 Paula channels is mixed into a single buffer
- A 256-point fixed-point FFT transforms the time-domain signal into frequency
  bins
- The oscilloscope displays the raw waveform
- The spectrum analyzer groups FFT bins into 32 bars with smoothed animation
  and falling peak indicators

## Known Limitations

- Programs that bypass audio.device (most tracker players, games) will show
  as "active" but won't display accurate waveforms
- The audio.device patch uses SetFunction which may conflict with other tools
  that patch the same vector
- Requires V39 (Kickstart 3.0) for AGA and tag-based screen/window functions

## License

Part of the Ionosonde project. See LICENSE in the repository root.

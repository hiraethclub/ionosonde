/*
 * Ionosonde Viz - WinAmp-style Audio Visualizer for Amiga
 *
 * Target: A1200 + PiStorm (AGA chipset, fast 68k)
 * Displays oscilloscope waveform and spectrum analyzer bars
 * for any audio playing through Workbench.
 *
 * Build with VBCC:  vc +kick13 -O2 -o ionoviz main.c fft.c audio_capture.c -lamiga
 * Or see Makefile for cross-compilation setup.
 */

#include <exec/exec.h>
#include <exec/memory.h>
#include <intuition/intuition.h>
#include <intuition/screens.h>
#include <graphics/gfx.h>
#include <graphics/gfxbase.h>
#include <graphics/rastport.h>
#include <graphics/view.h>
#include <devices/timer.h>

#include <proto/exec.h>
#include <proto/intuition.h>
#include <proto/graphics.h>
#include <proto/dos.h>

#include "fft.h"
#include "audio_capture.h"

/* Screen dimensions */
#define SCR_WIDTH  320
#define SCR_HEIGHT 256
#define SCR_DEPTH  8     /* 256 colors for AGA */

/* Visualization layout */
#define SCOPE_Y      16   /* Oscilloscope Y start */
#define SCOPE_HEIGHT 80   /* Oscilloscope height */
#define SPEC_Y       120  /* Spectrum analyzer Y start */
#define SPEC_HEIGHT  120  /* Spectrum analyzer height */
#define NUM_BARS     32   /* Number of spectrum bars */
#define BAR_WIDTH    8    /* Width of each bar */
#define BAR_GAP      2    /* Gap between bars */
#define TITLE_Y      4    /* Title text Y position */

/* Color indices */
#define COL_BACKGROUND  0
#define COL_SCOPE_LINE  1
#define COL_SCOPE_FILL  2
#define COL_TEXT        3
#define COL_GRID        4
#define COL_PEAK        5
#define COL_BAR_START   16   /* 16 colors for bar gradient */
#define COL_BAR_END     31

/* Library bases */
struct IntuitionBase *IntuitionBase = NULL;
struct GfxBase       *GfxBase = NULL;

/* Screen and windows */
static struct Screen *screen = NULL;
static struct Window *window = NULL;
static struct RastPort *rp = NULL;

/* Double buffering */
static struct BitMap *bitmap[2] = { NULL, NULL };
static struct RastPort rastport[2];
static int current_buffer = 0;

/* Visualization state */
static LONG fft_real[FFT_SIZE];
static LONG fft_imag[FFT_SIZE];
static LONG spectrum[FFT_SIZE / 2];
static LONG peak_heights[NUM_BARS];   /* Peak dot positions */
static LONG bar_heights[NUM_BARS];    /* Current bar heights */
static LONG prev_bar_heights[NUM_BARS]; /* For smoothing */

/* Audio capture */
static struct AudioCapture audio_cap;

/* Visualization mode */
#define MODE_SCOPE_AND_BARS 0
#define MODE_SCOPE_ONLY     1
#define MODE_BARS_ONLY      2
#define MODE_COUNT          3
static int vis_mode = MODE_SCOPE_AND_BARS;

/* Forward declarations */
static BOOL setup_screen(void);
static void cleanup_screen(void);
static void setup_palette(void);
static void draw_background(struct RastPort *rp);
static void draw_oscilloscope(struct RastPort *rp, BYTE *samples, int num_samples);
static void draw_spectrum(struct RastPort *rp, LONG *magnitudes, int num_bars);
static void swap_buffers(void);
LONG sin_table_lookup(int phase);

/* ------------------------------------------------------------------
 * Setup AGA screen with double buffering
 * ------------------------------------------------------------------ */
static BOOL setup_screen(void)
{
    int i;

    /* Allocate bitmaps for double buffering */
    for (i = 0; i < 2; i++) {
        bitmap[i] = AllocBitMap(SCR_WIDTH, SCR_HEIGHT, SCR_DEPTH,
                                BMF_DISPLAYABLE | BMF_CLEAR, NULL);
        if (!bitmap[i]) return FALSE;

        InitRastPort(&rastport[i]);
        rastport[i].BitMap = bitmap[i];
    }

    /* Open a custom screen */
    screen = OpenScreenTags(NULL,
        SA_Width,      SCR_WIDTH,
        SA_Height,     SCR_HEIGHT,
        SA_Depth,      SCR_DEPTH,
        SA_DisplayID,  0x00000000,  /* Default PAL lowres */
        SA_BitMap,     (ULONG)bitmap[0],
        SA_ShowTitle,  FALSE,
        SA_Quiet,      TRUE,
        SA_Type,       CUSTOMSCREEN,
        SA_Title,      (ULONG)"Ionosonde Viz",
        TAG_DONE);

    if (!screen) return FALSE;

    /* Open a backdrop window to capture input */
    window = OpenWindowTags(NULL,
        WA_CustomScreen,  (ULONG)screen,
        WA_Left,          0,
        WA_Top,           0,
        WA_Width,         SCR_WIDTH,
        WA_Height,        SCR_HEIGHT,
        WA_Flags,         WFLG_BACKDROP | WFLG_BORDERLESS | WFLG_ACTIVATE
                          | WFLG_RMBTRAP | WFLG_NOCAREREFRESH,
        WA_IDCMP,         IDCMP_RAWKEY | IDCMP_MOUSEBUTTONS,
        TAG_DONE);

    if (!window) return FALSE;

    setup_palette();
    return TRUE;
}

static void cleanup_screen(void)
{
    int i;

    if (window) {
        CloseWindow(window);
        window = NULL;
    }
    if (screen) {
        CloseScreen(screen);
        screen = NULL;
    }
    for (i = 0; i < 2; i++) {
        if (bitmap[i]) {
            FreeBitMap(bitmap[i]);
            bitmap[i] = NULL;
        }
    }
}

/* ------------------------------------------------------------------
 * WinAmp-inspired color palette
 * ------------------------------------------------------------------ */
static void setup_palette(void)
{
    int i;
    struct ViewPort *vp = &screen->ViewPort;

    /* Background: dark navy/black */
    SetRGB32(vp, COL_BACKGROUND, 0x08080800, 0x08081800, 0x18182800);

    /* Oscilloscope line: bright green */
    SetRGB32(vp, COL_SCOPE_LINE, 0x20FF0000, 0xFFFF0000, 0x20FF0000);

    /* Oscilloscope fill: dim green */
    SetRGB32(vp, COL_SCOPE_FILL, 0x08400000, 0x40800000, 0x08400000);

    /* Text: warm white */
    SetRGB32(vp, COL_TEXT, 0xD0D0D000, 0xD0D0D000, 0xC0C0C000);

    /* Grid lines: subtle grey */
    SetRGB32(vp, COL_GRID, 0x20202000, 0x20203000, 0x30304000);

    /* Peak markers: white */
    SetRGB32(vp, COL_PEAK, 0xFFFFFF00, 0xFFFFFF00, 0xFFFFFF00);

    /* Spectrum bar gradient: green -> yellow -> orange -> red */
    for (i = 0; i < 16; i++) {
        ULONG r, g, b;
        if (i < 6) {
            /* Green to yellow */
            r = (i * 0x2A000000);
            g = 0xFF000000;
            b = 0x00000000;
        } else if (i < 11) {
            /* Yellow to orange */
            r = 0xFF000000;
            g = 0xFF000000 - ((i - 6) * 0x1A000000);
            b = 0x00000000;
        } else {
            /* Orange to red */
            r = 0xFF000000;
            g = 0x80000000 - ((i - 11) * 0x18000000);
            b = 0x00000000;
        }
        SetRGB32(vp, COL_BAR_START + i, r, g, b);
    }

    /* Additional colors for decorative elements */
    SetRGB32(vp, 6, 0x40408000, 0x40406000, 0x80809000); /* Border color */
    SetRGB32(vp, 7, 0x00608000, 0x60A0C000, 0x80D0FF00); /* Accent blue */
}

/* ------------------------------------------------------------------
 * Drawing routines
 * ------------------------------------------------------------------ */
static void draw_background(struct RastPort *rp)
{
    int x;

    /* Clear to background */
    SetAPen(rp, COL_BACKGROUND);
    RectFill(rp, 0, 0, SCR_WIDTH - 1, SCR_HEIGHT - 1);

    /* Title */
    SetAPen(rp, COL_TEXT);
    SetBPen(rp, COL_BACKGROUND);
    Move(rp, 8, TITLE_Y + 8);
    Text(rp, "IONOSONDE VIZ", 13);

    /* Mode indicator */
    Move(rp, SCR_WIDTH - 80, TITLE_Y + 8);
    switch (vis_mode) {
    case MODE_SCOPE_AND_BARS:
        Text(rp, "[DUAL]", 6);
        break;
    case MODE_SCOPE_ONLY:
        Text(rp, "[SCOPE]", 7);
        break;
    case MODE_BARS_ONLY:
        Text(rp, "[BARS]", 6);
        break;
    }

    /* Grid lines for oscilloscope */
    if (vis_mode != MODE_BARS_ONLY) {
        SetAPen(rp, COL_GRID);
        /* Center line */
        Move(rp, 0, SCOPE_Y + SCOPE_HEIGHT / 2);
        Draw(rp, SCR_WIDTH - 1, SCOPE_Y + SCOPE_HEIGHT / 2);
        /* Quarter lines */
        SetDrPt(rp, 0xAAAA); /* Dotted */
        Move(rp, 0, SCOPE_Y + SCOPE_HEIGHT / 4);
        Draw(rp, SCR_WIDTH - 1, SCOPE_Y + SCOPE_HEIGHT / 4);
        Move(rp, 0, SCOPE_Y + 3 * SCOPE_HEIGHT / 4);
        Draw(rp, SCR_WIDTH - 1, SCOPE_Y + 3 * SCOPE_HEIGHT / 4);
        SetDrPt(rp, 0xFFFF); /* Solid */
    }

    /* Spectrum base line */
    if (vis_mode != MODE_SCOPE_ONLY) {
        SetAPen(rp, COL_GRID);
        Move(rp, 0, SPEC_Y + SPEC_HEIGHT);
        Draw(rp, SCR_WIDTH - 1, SPEC_Y + SPEC_HEIGHT);
    }

    /* Key hints */
    SetAPen(rp, COL_GRID);
    Move(rp, 8, SCR_HEIGHT - 4);
    Text(rp, "ESC:Quit  M:Mode", 16);
}

static void draw_oscilloscope(struct RastPort *rp, BYTE *samples, int num_samples)
{
    int x, y, prev_y;
    int scope_y_start, scope_height;

    if (vis_mode == MODE_SCOPE_ONLY) {
        scope_y_start = 20;
        scope_height = SCR_HEIGHT - 40;
    } else {
        scope_y_start = SCOPE_Y;
        scope_height = SCOPE_HEIGHT;
    }

    /* Draw waveform */
    SetAPen(rp, COL_SCOPE_LINE);

    for (x = 0; x < SCR_WIDTH; x++) {
        /* Map screen X to sample index */
        int sample_idx = (x * num_samples) / SCR_WIDTH;
        if (sample_idx >= num_samples) sample_idx = num_samples - 1;

        /* Map sample (-128..127) to screen Y */
        LONG sample = (LONG)samples[sample_idx];
        y = scope_y_start + scope_height / 2 - (sample * scope_height) / 256;

        if (y < scope_y_start) y = scope_y_start;
        if (y >= scope_y_start + scope_height) y = scope_y_start + scope_height - 1;

        if (x == 0) {
            Move(rp, x, y);
        } else {
            Draw(rp, x, y);
        }

        /* Draw filled area from center line to waveform */
        if (x % 2 == 0) { /* Every other pixel for performance */
            int center = scope_y_start + scope_height / 2;
            SetAPen(rp, COL_SCOPE_FILL);
            if (y < center) {
                Move(rp, x, y + 1);
                Draw(rp, x, center);
            } else if (y > center) {
                Move(rp, x, center);
                Draw(rp, x, y - 1);
            }
            SetAPen(rp, COL_SCOPE_LINE);
        }
    }
}

static void draw_spectrum(struct RastPort *rp, LONG *magnitudes, int num_bars)
{
    int i, x, y;
    int spec_y_start, spec_height;
    LONG max_mag = 1;

    if (vis_mode == MODE_BARS_ONLY) {
        spec_y_start = 20;
        spec_height = SCR_HEIGHT - 40;
    } else {
        spec_y_start = SPEC_Y;
        spec_height = SPEC_HEIGHT;
    }

    /* Find peak magnitude for normalization */
    for (i = 0; i < FFT_SIZE / 2; i++) {
        if (magnitudes[i] > max_mag) max_mag = magnitudes[i];
    }

    /* Compute bar heights by averaging frequency bins */
    for (i = 0; i < num_bars; i++) {
        LONG sum = 0;
        int bin_start = (i * (FFT_SIZE / 2)) / num_bars;
        int bin_end = ((i + 1) * (FFT_SIZE / 2)) / num_bars;
        int j, count = 0;

        /* Use logarithmic-ish bin grouping for more natural look */
        for (j = bin_start; j < bin_end; j++) {
            sum += magnitudes[j];
            count++;
        }
        if (count > 0) sum /= count;

        /* Normalize to bar height */
        LONG target = (sum * spec_height) / max_mag;
        if (target > spec_height) target = spec_height;

        /* Smooth animation: bars fall gradually */
        if (target > prev_bar_heights[i]) {
            bar_heights[i] = target;
        } else {
            /* Gravity fall effect */
            bar_heights[i] = prev_bar_heights[i] - 3;
            if (bar_heights[i] < 0) bar_heights[i] = 0;
            if (bar_heights[i] < target) bar_heights[i] = target;
        }
        prev_bar_heights[i] = bar_heights[i];

        /* Update peak dots (fall slowly) */
        if (bar_heights[i] > peak_heights[i]) {
            peak_heights[i] = bar_heights[i];
        } else {
            peak_heights[i] -= 1;
            if (peak_heights[i] < 0) peak_heights[i] = 0;
        }
    }

    /* Draw bars */
    for (i = 0; i < num_bars; i++) {
        int bar_x = 8 + i * (BAR_WIDTH + BAR_GAP);
        int bar_h = (int)bar_heights[i];
        int bar_bottom = spec_y_start + spec_height;
        int bar_top = bar_bottom - bar_h;
        int row;

        if (bar_h <= 0) continue;

        /* Draw gradient bar from bottom to top */
        for (row = bar_bottom; row >= bar_top; row--) {
            /* Color based on height position */
            int height_pct = ((bar_bottom - row) * 15) / spec_height;
            if (height_pct > 15) height_pct = 15;

            SetAPen(rp, COL_BAR_START + height_pct);
            Move(rp, bar_x, row);
            Draw(rp, bar_x + BAR_WIDTH - 1, row);
        }

        /* Draw peak dot */
        if (peak_heights[i] > 0) {
            int peak_y = bar_bottom - (int)peak_heights[i];
            if (peak_y >= spec_y_start && peak_y < bar_bottom) {
                SetAPen(rp, COL_PEAK);
                Move(rp, bar_x, peak_y);
                Draw(rp, bar_x + BAR_WIDTH - 1, peak_y);
            }
        }
    }
}

static void swap_buffers(void)
{
    struct ViewPort *vp = &screen->ViewPort;

    current_buffer ^= 1;

    /* Install the new bitmap into the screen */
    screen->RastPort.BitMap = bitmap[current_buffer];
    screen->ViewPort.RasInfo->BitMap = bitmap[current_buffer];

    /* Signal the display to switch */
    MakeScreen(screen);
    RethinkDisplay();
}

/* ------------------------------------------------------------------
 * Main program
 * ------------------------------------------------------------------ */
int main(int argc, char **argv)
{
    struct IntuiMessage *msg;
    BOOL running = TRUE;
    int i;

    /* Open libraries */
    IntuitionBase = (struct IntuitionBase *)OpenLibrary("intuition.library", 39);
    GfxBase = (struct GfxBase *)OpenLibrary("graphics.library", 39);

    if (!IntuitionBase || !GfxBase) {
        if (IntuitionBase) CloseLibrary((struct Library *)IntuitionBase);
        if (GfxBase) CloseLibrary((struct Library *)GfxBase);
        return 20;
    }

    /* Initialize FFT tables */
    fft_init();

    /* Initialize bar state */
    for (i = 0; i < NUM_BARS; i++) {
        bar_heights[i] = 0;
        prev_bar_heights[i] = 0;
        peak_heights[i] = 0;
    }

    /* Setup display */
    if (!setup_screen()) {
        cleanup_screen();
        CloseLibrary((struct Library *)GfxBase);
        CloseLibrary((struct Library *)IntuitionBase);
        return 20;
    }

    /* Initialize audio capture */
    if (audio_capture_init(&audio_cap) != 0) {
        /* If audio capture fails, continue anyway with demo mode */
        audio_cap.active = FALSE;
    }

    /* Main loop */
    while (running) {
        struct RastPort *draw_rp = &rastport[current_buffer ^ 1];

        /* Process input events */
        while ((msg = (struct IntuiMessage *)GetMsg(window->UserPort)) != NULL) {
            ULONG class = msg->Class;
            UWORD code = msg->Code;
            ReplyMsg((struct Message *)msg);

            switch (class) {
            case IDCMP_RAWKEY:
                if (code == 0x45) {  /* ESC */
                    running = FALSE;
                } else if (code == 0x37) {  /* M key */
                    vis_mode = (vis_mode + 1) % MODE_COUNT;
                }
                break;
            case IDCMP_MOUSEBUTTONS:
                /* Left click cycles mode too */
                if (code == SELECTDOWN) {
                    vis_mode = (vis_mode + 1) % MODE_COUNT;
                }
                break;
            }
        }

        /* Update audio capture */
        audio_capture_update(&audio_cap);

        /* If no audio playing, generate a subtle idle animation */
        if (!audio_cap.active) {
            static int idle_phase = 0;
            for (i = 0; i < CAPTURE_BUFFER_SIZE; i++) {
                /* Gentle sine wave idle animation */
                int phase = (i * 8 + idle_phase) % FFT_SIZE;
                audio_cap.buffer[i] = (BYTE)(
                    (sin_table_lookup(phase) * 20) >> FP_SHIFT);
            }
            idle_phase += 3;
        }

        /* Perform FFT for spectrum analysis */
        for (i = 0; i < FFT_SIZE; i++) {
            fft_real[i] = (LONG)audio_cap.buffer[i % audio_cap.buffer_size] << 8;
            fft_imag[i] = 0;
        }
        fft_perform(fft_real, fft_imag);
        fft_magnitude(fft_real, fft_imag, spectrum);

        /* Draw frame to back buffer */
        draw_background(draw_rp);

        switch (vis_mode) {
        case MODE_SCOPE_AND_BARS:
            draw_oscilloscope(draw_rp, audio_cap.buffer, audio_cap.buffer_size);
            draw_spectrum(draw_rp, spectrum, NUM_BARS);
            break;
        case MODE_SCOPE_ONLY:
            draw_oscilloscope(draw_rp, audio_cap.buffer, audio_cap.buffer_size);
            break;
        case MODE_BARS_ONLY:
            draw_spectrum(draw_rp, spectrum, NUM_BARS);
            break;
        }

        /* Swap display buffers */
        swap_buffers();

        /* Brief delay to target ~25fps (PAL-friendly) */
        /* PiStorm is fast enough that we're mostly display-bound */
        WaitTOF();
    }

    /* Cleanup */
    audio_capture_cleanup(&audio_cap);
    cleanup_screen();
    CloseLibrary((struct Library *)GfxBase);
    CloseLibrary((struct Library *)IntuitionBase);

    return 0;
}

/* Sine table lookup for idle animation (triangle wave approximation) */
LONG sin_table_lookup(int phase)
{
    /* Use a simple sine approximation for idle mode */
    phase = phase & (FFT_SIZE - 1);
    /* Rough sine: triangle wave approximation scaled */
    if (phase < FFT_SIZE / 4) {
        return (phase * FP_ONE) / (FFT_SIZE / 4);
    } else if (phase < FFT_SIZE / 2) {
        return ((FFT_SIZE / 2 - phase) * FP_ONE) / (FFT_SIZE / 4);
    } else if (phase < 3 * FFT_SIZE / 4) {
        return -((phase - FFT_SIZE / 2) * FP_ONE) / (FFT_SIZE / 4);
    } else {
        return -((FFT_SIZE - phase) * FP_ONE) / (FFT_SIZE / 4);
    }
}

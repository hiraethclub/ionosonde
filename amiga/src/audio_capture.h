#ifndef AUDIO_CAPTURE_H
#define AUDIO_CAPTURE_H

/*
 * Audio capture for Amiga visualization
 * Intercepts audio.device to capture waveform data from any
 * application playing sound through the OS.
 */

#include <exec/types.h>

#define CAPTURE_BUFFER_SIZE 512
#define NUM_CHANNELS 4

/* Captured audio state */
struct AudioCapture {
    BYTE   *buffer;          /* Mixed sample buffer for visualization */
    UWORD   buffer_size;     /* Current valid samples in buffer */
    BOOL    active;          /* TRUE if audio is currently playing */

    /* Per-channel tracking */
    BYTE   *chan_data[NUM_CHANNELS];  /* Pointer to each channel's current data */
    ULONG   chan_length[NUM_CHANNELS]; /* Length of each channel's buffer */
    UWORD   chan_period[NUM_CHANNELS]; /* Period (playback rate) */
    UWORD   chan_volume[NUM_CHANNELS]; /* Volume (0-64) */
    BOOL    chan_active[NUM_CHANNELS]; /* Is this channel playing? */
};

/* Install audio.device hook - returns 0 on success */
LONG audio_capture_init(struct AudioCapture *cap);

/* Remove audio.device hook and clean up */
void audio_capture_cleanup(struct AudioCapture *cap);

/* Read current audio state from Paula hardware registers.
 * Call this each frame to snapshot the current waveform.
 * Mixes all active channels into cap->buffer.
 */
void audio_capture_update(struct AudioCapture *cap);

#endif /* AUDIO_CAPTURE_H */

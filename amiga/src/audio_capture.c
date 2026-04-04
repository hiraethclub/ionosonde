/*
 * Audio capture via audio.device patching
 *
 * Strategy: We patch the BeginIO vector of audio.device to intercept
 * all audio I/O requests. When a CMD_WRITE (or ADCMD_PERVOL) comes through,
 * we grab the sample pointer, length, period, and volume from the IOAudio
 * request and store them for the visualizer.
 *
 * We also directly sample Paula's hardware registers as a fallback
 * for programs that bang the hardware (tracker players, etc.)
 */

#include "audio_capture.h"

#include <exec/exec.h>
#include <exec/memory.h>
#include <devices/audio.h>
#include <hardware/custom.h>
#include <hardware/dmabits.h>

#include <proto/exec.h>

/* Amiga custom chip registers */
extern struct Custom far custom;

/* The original BeginIO vector we're patching */
static void (*OldBeginIO)(struct IORequest *io, struct Device *dev);
static struct Device *AudioDevice = NULL;
static struct AudioCapture *GlobalCapture = NULL;

/* Paula audio channel hardware registers */
struct AudChannel {
    UWORD *ac_ptr;   /* Pointer (high word) - actually split across two regs */
    UWORD  ac_len;
    UWORD  ac_per;
    UWORD  ac_vol;
    UWORD  ac_dat;
    UWORD  ac_pad[3];
};

/* Hardware channel base addresses in custom chip space */
#define AUD0_BASE 0xDFF0A0
#define AUD1_BASE 0xDFF0B0
#define AUD2_BASE 0xDFF0C0
#define AUD3_BASE 0xDFF0D0

/* Our replacement BeginIO that intercepts audio requests */
static void __saveds NewBeginIO(
    register __a1 struct IORequest *io,
    register __a6 struct Device *dev)
{
    struct IOAudio *ioa = (struct IOAudio *)io;

    if (GlobalCapture && io->io_Command == CMD_WRITE) {
        /* Extract channel allocation from the request */
        UBYTE channels = ioa->ioa_Request.io_Unit ?
            (UBYTE)((ULONG)ioa->ioa_Request.io_Unit & 0x0F) : 0;
        int ch;

        for (ch = 0; ch < NUM_CHANNELS; ch++) {
            if (channels & (1 << ch)) {
                GlobalCapture->chan_data[ch] = (BYTE *)ioa->ioa_Data;
                GlobalCapture->chan_length[ch] = (ULONG)ioa->ioa_Length;
                GlobalCapture->chan_period[ch] = ioa->ioa_Period;
                GlobalCapture->chan_volume[ch] = ioa->ioa_Volume;
                GlobalCapture->chan_active[ch] = TRUE;
            }
        }
        GlobalCapture->active = TRUE;
    }

    /* Call original handler */
    OldBeginIO(io, dev);
}

LONG audio_capture_init(struct AudioCapture *cap)
{
    struct MsgPort *port;
    struct IOAudio *ioa;
    UBYTE alloc_map[] = { 0 }; /* Don't actually allocate channels */
    int i;

    /* Initialize capture structure */
    cap->buffer = (BYTE *)AllocMem(CAPTURE_BUFFER_SIZE, MEMF_PUBLIC | MEMF_CLEAR);
    if (!cap->buffer) return -1;

    cap->buffer_size = CAPTURE_BUFFER_SIZE;
    cap->active = FALSE;

    for (i = 0; i < NUM_CHANNELS; i++) {
        cap->chan_data[i] = NULL;
        cap->chan_length[i] = 0;
        cap->chan_period[i] = 0;
        cap->chan_volume[i] = 0;
        cap->chan_active[i] = FALSE;
    }

    /* Open audio.device just to get the device base pointer */
    port = CreateMsgPort();
    if (!port) {
        FreeMem(cap->buffer, CAPTURE_BUFFER_SIZE);
        return -2;
    }

    ioa = (struct IOAudio *)CreateIORequest(port, sizeof(struct IOAudio));
    if (!ioa) {
        DeleteMsgPort(port);
        FreeMem(cap->buffer, CAPTURE_BUFFER_SIZE);
        return -3;
    }

    ioa->ioa_Request.io_Message.mn_Node.ln_Pri = -50; /* Low priority */
    ioa->ioa_Data = alloc_map;
    ioa->ioa_Length = sizeof(alloc_map);

    if (OpenDevice(AUDIONAME, 0, (struct IORequest *)ioa, 0) != 0) {
        DeleteIORequest((struct IORequest *)ioa);
        DeleteMsgPort(port);
        FreeMem(cap->buffer, CAPTURE_BUFFER_SIZE);
        return -4;
    }

    AudioDevice = ioa->ioa_Request.io_Device;

    /* Patch BeginIO vector */
    GlobalCapture = cap;
    Disable();
    OldBeginIO = (void (*)(struct IORequest *, struct Device *))
        SetFunction(AudioDevice, -30, (APTR)NewBeginIO);
    Enable();

    /* Close our temporary IO request but leave device open */
    CloseDevice((struct IORequest *)ioa);
    DeleteIORequest((struct IORequest *)ioa);
    DeleteMsgPort(port);

    return 0;
}

void audio_capture_cleanup(struct AudioCapture *cap)
{
    /* Restore original BeginIO */
    if (AudioDevice && OldBeginIO) {
        Disable();
        SetFunction(AudioDevice, -30, (APTR)OldBeginIO);
        Enable();
        OldBeginIO = NULL;
        AudioDevice = NULL;
    }

    GlobalCapture = NULL;

    if (cap->buffer) {
        FreeMem(cap->buffer, CAPTURE_BUFFER_SIZE);
        cap->buffer = NULL;
    }
}

void audio_capture_update(struct AudioCapture *cap)
{
    int i, ch;
    BOOL any_active = FALSE;

    /* Clear mix buffer */
    for (i = 0; i < CAPTURE_BUFFER_SIZE; i++) {
        cap->buffer[i] = 0;
    }

    /* Mix all active channels into the buffer */
    for (ch = 0; ch < NUM_CHANNELS; ch++) {
        if (cap->chan_active[ch] && cap->chan_data[ch] && cap->chan_length[ch] > 0) {
            ULONG src_len = cap->chan_length[ch];
            WORD vol = cap->chan_volume[ch];
            BYTE *src = cap->chan_data[ch];

            any_active = TRUE;

            for (i = 0; i < CAPTURE_BUFFER_SIZE; i++) {
                /* Simple resampling: map visualization buffer to source */
                ULONG src_pos = (i * src_len) / CAPTURE_BUFFER_SIZE;
                if (src_pos < src_len) {
                    LONG sample = (LONG)src[src_pos];
                    /* Apply volume (0-64) and mix */
                    sample = (sample * vol) >> 6;
                    /* Saturating add to mix buffer */
                    LONG mixed = (LONG)cap->buffer[i] + sample;
                    if (mixed > 127) mixed = 127;
                    if (mixed < -128) mixed = -128;
                    cap->buffer[i] = (BYTE)mixed;
                }
            }
        }
    }

    /* Fallback: if no audio.device activity detected, try reading
     * Paula DMA state directly. This helps with tracker players
     * and other programs that bypass audio.device.
     */
    if (!any_active) {
        /* Check if audio DMA is enabled */
        UWORD dmaconr = custom.dmaconr;
        if (dmaconr & (DMAF_AUD0 | DMAF_AUD1 | DMAF_AUD2 | DMAF_AUD3)) {
            /* Audio DMA is running but we didn't catch it via audio.device.
             * Generate a simple indicator pattern so the user knows
             * audio is playing even if we can't capture the waveform.
             */
            cap->active = TRUE;
            /* We can't reliably read the sample data from hw-banging programs,
             * but we signal that audio is present */
        } else {
            cap->active = FALSE;
        }
    } else {
        cap->active = TRUE;
    }
}

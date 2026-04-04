#ifndef FFT_H
#define FFT_H

/*
 * Fixed-point FFT for Amiga audio visualization
 * Uses 16.16 fixed-point arithmetic for speed on 68k
 */

#include <exec/types.h>

#define FFT_SIZE 256
#define FFT_LOG2 8

/* Fixed-point scaling: 16.16 format */
#define FP_SHIFT 14
#define FP_ONE   (1 << FP_SHIFT)

/* Initialize sine/cosine lookup tables */
void fft_init(void);

/* Perform in-place FFT on sample data.
 * Input:  real[] = audio samples (8-bit signed, will be promoted)
 *         imag[] = should be zeroed before call
 * Output: real[] and imag[] contain frequency domain data
 */
void fft_perform(LONG *real, LONG *imag);

/* Compute magnitude spectrum from FFT output.
 * Output magnitudes are in magnitude[] (0..FFT_SIZE/2-1)
 * Returns values suitable for direct use as bar heights.
 */
void fft_magnitude(LONG *real, LONG *imag, LONG *magnitude);

#endif /* FFT_H */

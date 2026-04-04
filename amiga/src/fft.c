/*
 * Fixed-point FFT implementation for Amiga
 * Optimized for 68020+ with 32-bit multiply
 */

#include "fft.h"

/* Precomputed sine table (quarter wave, FP_SHIFT precision) */
static LONG sin_table[FFT_SIZE];
static LONG cos_table[FFT_SIZE];

/* Integer square root (good enough for visualization) */
static LONG isqrt(LONG val)
{
    LONG root = 0;
    LONG bit = 1L << 30;

    if (val <= 0) return 0;

    while (bit > val) bit >>= 2;

    while (bit != 0) {
        if (val >= root + bit) {
            val -= root + bit;
            root = (root >> 1) + bit;
        } else {
            root >>= 1;
        }
        bit >>= 2;
    }
    return root;
}

void fft_init(void)
{
    int i;
    /* Build sine/cosine tables using a simple recursive approach.
     * We precompute for FFT_SIZE points around the unit circle.
     * Values in fixed-point FP_SHIFT format.
     */
    static const LONG sine_quarter[65] = {
        0, 402, 804, 1205, 1606, 2006, 2404, 2801,
        3196, 3590, 3981, 4370, 4756, 5139, 5520, 5897,
        6270, 6639, 7005, 7366, 7723, 8076, 8423, 8765,
        9102, 9434, 9760, 10080, 10394, 10702, 11003, 11297,
        11585, 11866, 12140, 12406, 12665, 12916, 13160, 13395,
        13623, 13842, 14053, 14256, 14449, 14635, 14811, 14978,
        15137, 15286, 15426, 15557, 15679, 15791, 15893, 15986,
        16069, 16143, 16207, 16261, 16305, 16340, 16364, 16379,
        16384
    };

    /* FFT_SIZE=256, so we need 256 entries */
    for (i = 0; i < FFT_SIZE; i++) {
        int idx = i * 64 / (FFT_SIZE / 4);  /* map to quarter table */
        int quadrant = (i * 4) / FFT_SIZE;
        int pos = (i * 256 / FFT_SIZE) % 64;

        /* Map index into quarter-wave table */
        int quarter_pos = i % (FFT_SIZE / 4);
        int quarter_idx = quarter_pos * 64 / (FFT_SIZE / 4);

        switch (quadrant) {
        case 0:
            sin_table[i] = sine_quarter[quarter_idx];
            break;
        case 1:
            sin_table[i] = sine_quarter[64 - quarter_idx];
            break;
        case 2:
            sin_table[i] = -sine_quarter[quarter_idx];
            break;
        case 3:
            sin_table[i] = -sine_quarter[64 - quarter_idx];
            break;
        }
        /* Cosine is sine shifted by 90 degrees */
        cos_table[i] = sin_table[(i + FFT_SIZE / 4) % FFT_SIZE];
    }

    /* Fix cosine table - compute directly for accuracy */
    for (i = 0; i < FFT_SIZE; i++) {
        int j = (i + FFT_SIZE / 4) % FFT_SIZE;
        cos_table[i] = sin_table[j];
    }
}

/* Bit-reverse an index for FFT butterfly reordering */
static ULONG bit_reverse(ULONG x, int bits)
{
    ULONG result = 0;
    int i;
    for (i = 0; i < bits; i++) {
        result = (result << 1) | (x & 1);
        x >>= 1;
    }
    return result;
}

void fft_perform(LONG *real, LONG *imag)
{
    int stage, pair, j;
    int pairs_in_group, num_groups, group;

    /* Bit-reversal permutation */
    for (j = 0; j < FFT_SIZE; j++) {
        int rev = (int)bit_reverse((ULONG)j, FFT_LOG2);
        if (rev > j) {
            LONG tmp;
            tmp = real[j]; real[j] = real[rev]; real[rev] = tmp;
            tmp = imag[j]; imag[j] = imag[rev]; imag[rev] = tmp;
        }
    }

    /* FFT butterfly stages */
    pairs_in_group = 1;
    for (stage = 0; stage < FFT_LOG2; stage++) {
        int group_size = pairs_in_group << 1;
        num_groups = FFT_SIZE / group_size;

        for (group = 0; group < num_groups; group++) {
            int base = group * group_size;
            for (pair = 0; pair < pairs_in_group; pair++) {
                int i1 = base + pair;
                int i2 = i1 + pairs_in_group;

                /* Twiddle factor index */
                int tw = (pair * FFT_SIZE) / group_size;
                LONG wr = cos_table[tw];
                LONG wi = -sin_table[tw];

                /* Butterfly */
                LONG tr = (wr * real[i2] - wi * imag[i2]) >> FP_SHIFT;
                LONG ti = (wr * imag[i2] + wi * real[i2]) >> FP_SHIFT;

                real[i2] = real[i1] - tr;
                imag[i2] = imag[i1] - ti;
                real[i1] = real[i1] + tr;
                imag[i1] = imag[i1] + ti;
            }
        }
        pairs_in_group <<= 1;
    }
}

void fft_magnitude(LONG *real, LONG *imag, LONG *magnitude)
{
    int i;
    /* Only compute first half (positive frequencies) */
    for (i = 0; i < FFT_SIZE / 2; i++) {
        LONG r = real[i] >> 4;  /* Scale down to prevent overflow in multiply */
        LONG im = imag[i] >> 4;
        LONG mag_sq = r * r + im * im;
        magnitude[i] = isqrt(mag_sq);
    }
}

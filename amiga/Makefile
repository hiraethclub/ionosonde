# Ionosonde Viz - Amiga Audio Visualizer
# Build with VBCC cross-compiler
#
# Prerequisites:
#   - VBCC cross-compiler for 68k (http://www.compilers.de/vbcc.html)
#   - Amiga NDK 3.9 includes
#
# Alternatively, build with Bebbo's GCC cross-compiler:
#   make CC=m68k-amigaos-gcc TOOLCHAIN=gcc

# Toolchain selection: vbcc (default) or gcc
TOOLCHAIN ?= vbcc

# --- VBCC settings ---
ifeq ($(TOOLCHAIN),vbcc)
CC      = vc
CFLAGS  = +kick13 -O2 -cpu=68020 -c99 -I$(NDK_INC)
LDFLAGS = -lamiga
TARGET  = ionoviz
endif

# --- Bebbo GCC settings ---
ifeq ($(TOOLCHAIN),gcc)
CC      = m68k-amigaos-gcc
CFLAGS  = -m68020 -O2 -Wall -noixemul -I$(NDK_INC)
LDFLAGS = -lamiga -noixemul
TARGET  = ionoviz
endif

# NDK include path (set NDK_INC environment variable or edit here)
NDK_INC ?= /opt/amiga/ndk/include

# Source files
SRCDIR  = src
SOURCES = $(SRCDIR)/main.c $(SRCDIR)/fft.c $(SRCDIR)/audio_capture.c
OBJECTS = $(SOURCES:.c=.o)

# Build rules
all: $(TARGET)

$(TARGET): $(OBJECTS)
	$(CC) $(CFLAGS) -o $@ $(OBJECTS) $(LDFLAGS)

$(SRCDIR)/%.o: $(SRCDIR)/%.c
	$(CC) $(CFLAGS) -c -o $@ $<

clean:
	rm -f $(OBJECTS) $(TARGET)

# Quick build (single compilation unit for faster builds)
quick:
	$(CC) $(CFLAGS) -o $(TARGET) $(SOURCES) $(LDFLAGS)

.PHONY: all clean quick

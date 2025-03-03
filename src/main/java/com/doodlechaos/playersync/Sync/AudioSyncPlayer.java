package com.doodlechaos.playersync.Sync;

import com.doodlechaos.playersync.PlayerSync;
import org.lwjgl.openal.AL10;
import org.lwjgl.system.MemoryUtil;

import static com.doodlechaos.playersync.PlayerSync.LOGGER;
import static org.lwjgl.stb.STBVorbis.*;
import org.lwjgl.stb.STBVorbisInfo;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AudioSyncPlayer {

    // Constant for sample offset property (in frames)
    private static final int AL_SEC_OFFSET = 0x1024;
    private static final int AL_SAMPLE_OFFSET = 0x1025; // 4133 in decimal

    private static int bufferId;
    private static int sourceId;
    private static boolean loaded = false;
    private static int sampleRate;

    /**
     * Loads an OGG file from the given file path, decodes it using STB Vorbis,
     * and creates an OpenAL buffer and source.
     *
     * @param filePath the path to the .ogg file
     * @return true if loading succeeded, false otherwise
     */
    public static boolean loadAudio(String filePath) {
        ByteBuffer fileBuffer = null;
        try {
            // Read the entire file into a byte array and allocate a direct ByteBuffer
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            fileBuffer = MemoryUtil.memAlloc(bytes.length);
            fileBuffer.put(bytes).flip();

            // Allocate an IntBuffer for error code
            IntBuffer errorBuffer = MemoryUtil.memAllocInt(1);

            // Open the OGG stream from the ByteBuffer
            long decoder = stb_vorbis_open_memory(fileBuffer, errorBuffer, null);
            if (decoder == MemoryUtil.NULL) {
                System.err.println("Failed to open OGG file: " + filePath + " Error: " + errorBuffer.get(0));
                MemoryUtil.memFree(errorBuffer);
                return false;
            }

            // Retrieve audio information using STBVorbisInfo
            STBVorbisInfo info = STBVorbisInfo.malloc();
            stb_vorbis_get_info(decoder, info);
            int channels = info.channels();
            sampleRate = info.sample_rate();

            // Get total number of samples
            int totalSamples = stb_vorbis_stream_length_in_samples(decoder);

            // Allocate a ShortBuffer to hold the decoded PCM data
            ShortBuffer pcm = MemoryUtil.memAllocShort(totalSamples * channels);

            // Decode the OGG file (interleaved if stereo)
            int samplesDecoded = stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);
            if (samplesDecoded <= 0) {
                System.err.println("Failed to decode OGG file: " + filePath);
                stb_vorbis_close(decoder);
                MemoryUtil.memFree(errorBuffer);
                MemoryUtil.memFree(pcm);
                info.free();
                return false;
            }
            pcm.limit(samplesDecoded * channels);

            // Clean up decoder and temporary buffers
            stb_vorbis_close(decoder);
            MemoryUtil.memFree(errorBuffer);
            MemoryUtil.memFree(fileBuffer);
            info.free();

            // Generate an OpenAL buffer and fill it with the decoded PCM data.
            bufferId = AL10.alGenBuffers();
            int format = (channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16);
            AL10.alBufferData(bufferId, format, pcm, sampleRate);
            MemoryUtil.memFree(pcm);

            // Generate an OpenAL source and attach the buffer to it.
            sourceId = AL10.alGenSources();
            AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferId);

            loaded = true;
            LOGGER.info("Loaded audio successfully");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            if (fileBuffer != null) {
                MemoryUtil.memFree(fileBuffer);
            }
            return false;
        }
    }

    /**
     * Plays the loaded audio file.
     */
    public static void playAudio() {
        if (!loaded) {
            System.err.println("Audio not loaded. Call loadAudio() first.");
            return;
        }
        AL10.alSourcePlay(sourceId);
    }

    public static void pauseAudio(){
        if (!loaded) {
            System.err.println("Audio not loaded. Call loadAudio() first.");
            return;
        }
        AL10.alSourcePause(sourceId);
    }
    public static float getCurrentPlayheadTime() {
        if (!loaded) {
            return 0f;
        }
        return AL10.alGetSourcef(sourceId, AL_SEC_OFFSET);
    }

    public static void syncAudio(){

        // Check if the audio is playing
        int state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
        if (state != AL10.AL_PLAYING) {
            // If not playing, there's no need to sync.
            return;
        }

        float expectedTime = PlayerSync.playbackIndex / 60.0f;
        float currAudioTime = getCurrentPlayheadTime();
        if(Math.abs(expectedTime - currAudioTime) > 0.0333f) // 2/60th of a second
        {
            setPlayheadTime(expectedTime);
            LOGGER.info("Audio desync detected. Adjusting playhead from " + currAudioTime + " to " + expectedTime);
        }
    }
    /**
     * Changes the playhead position (seek) to the given time (in seconds).
     * Note: This method stops playback, seeks, then resumes playback.
     *
     * @param seconds the new playhead position in seconds.
     */
    public static void setPlayheadTime(float seconds) {
        if (!loaded) {
            System.err.println("Audio not loaded. Call loadAudio() first.");
            return;
        }
        // Compute the sample offset (in frames) from the time in seconds.
        int sampleOffset = (int)(seconds * sampleRate);
        AL10.alSourceStop(sourceId);
        AL10.alSourcei(sourceId, AL_SAMPLE_OFFSET, sampleOffset);
        AL10.alSourcePlay(sourceId);
    }

    /**
     * Cleans up the OpenAL resources.
     */
    public static void cleanup() {
        if (loaded) {
            AL10.alDeleteSources(sourceId);
            AL10.alDeleteBuffers(bufferId);
            loaded = false;
        }
    }
}

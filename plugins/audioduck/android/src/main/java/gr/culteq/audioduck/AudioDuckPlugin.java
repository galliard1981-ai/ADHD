package gr.culteq.audioduck;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Ζητά από το Android «εστίαση ήχου».
 * mode "duck": οι άλλες εφαρμογές (π.χ. Spotify) χαμηλώνουν όσο μιλάει η εφαρμογή.
 * mode "exclusive": οι άλλες εφαρμογές σταματούν προσωρινά και συνεχίζουν μόνες τους μετά.
 */
@CapacitorPlugin(name = "AudioDuck")
public class AudioDuckPlugin extends Plugin {

    private AudioManager audioManager;
    private AudioFocusRequest focusRequest; // API 26+
    private boolean held = false;

    private final AudioManager.OnAudioFocusChangeListener listener = change -> { };

    private AudioManager am() {
        if (audioManager == null) {
            audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        }
        return audioManager;
    }

    private void release() {
        if (!held) return;
        held = false;
        AudioManager manager = am();
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (focusRequest != null) manager.abandonAudioFocusRequest(focusRequest);
            focusRequest = null;
        } else {
            manager.abandonAudioFocus(listener);
        }
    }

    @PluginMethod
    public void request(PluginCall call) {
        String mode = call.getString("mode", "duck");
        boolean exclusive = "exclusive".equals(mode);
        int gain = exclusive
                ? AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
                : AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
        release();
        AudioManager manager = am();
        if (manager == null) {
            call.resolve();
            return;
        }
        int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setUsage(exclusive
                            ? AudioAttributes.USAGE_ASSISTANT
                            : AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            focusRequest = new AudioFocusRequest.Builder(gain)
                    .setAudioAttributes(attrs)
                    .setOnAudioFocusChangeListener(listener)
                    .build();
            result = manager.requestAudioFocus(focusRequest);
        } else {
            result = manager.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, gain);
        }
        held = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        call.resolve();
    }

    @PluginMethod
    public void abandon(PluginCall call) {
        release();
        call.resolve();
    }

    @Override
    protected void handleOnDestroy() {
        release();
        super.handleOnDestroy();
    }
}

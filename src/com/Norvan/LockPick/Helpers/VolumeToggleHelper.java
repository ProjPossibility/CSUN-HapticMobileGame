package com.Norvan.LockPick.Helpers;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.Norvan.LockPick.R;

/**
 * Created by IntelliJ IDEA.
 * User: ngorgi
 * Date: 2/8/12
 * Time: 9:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class VolumeToggleHelper {
    private AudioManager audioManager;
    private boolean isMuted;
    private ImageButton toggleImage;
    public VolumeToggleHelper(Context context, ImageButton toggleImage) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        isMuted = isVolumeMuted();
        this.toggleImage = toggleImage;
        setVolumeToggleImage(isMuted);

    }

    private void setVolumeToggleImage(boolean isMute) {
        toggleImage.setImageResource(isMute ? R.drawable.ic_audio_vol_mute : R.drawable.ic_audio_vol);
    }

    public boolean toggleMute(){
        isMuted = isVolumeMuted();
        if (isMuted) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        } else {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        }

        isMuted = !isMuted;
        setVolumeToggleImage(isMuted);
        return isMuted;
    }

    private boolean isVolumeMuted(){
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            return true;
        }
        return false;
    }
    

    
    public void shutDown(){
        
    }
    
    
}

package com.Norvan.LockPick.Helpers;

import android.content.Context;
import android.media.AudioManager;
import android.widget.ImageButton;
import com.Norvan.LockPick.R;


/**
 * @author Norvan Gorgi
 *         Abstracts out the task of toggling the media volume.
 *         Stores the AudioManager object used to get and set the media volume level.
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

    /**
     * Toggles the media volume between 0% (muted) and 50%
     *
     * @return whether the volume is muted now.
     */

    public boolean toggleMute() {
        isMuted = isVolumeMuted();
        if (isMuted) {
            //If muted, sets the volume at max/2 to avoid making headphone users go deaf.
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0);
        } else {
            //If not muted, mutes the volume.
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        }

        isMuted = !isMuted;
        setVolumeToggleImage(isMuted);


        return isMuted;
    }

    //Sets the appropriate image graphic based on whether the volume is muted or not.
    private void setVolumeToggleImage(boolean isMute) {
        toggleImage.setImageResource(isMute ? R.drawable.ic_audio_vol_mute : R.drawable.ic_audio_vol);
    }

    private boolean isVolumeMuted() {
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            return true;
        }
        return false;
    }


}

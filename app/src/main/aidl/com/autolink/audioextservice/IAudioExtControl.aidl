// IAudioExtControl.aidl
package com.autolink.audioextservice;

import com.autolink.audioextservice.IVisualizerCallback;

interface IAudioExtControl {

    String toMusicTacticsJson();
    String toVoiceTacticsJson();
    float getMusicAntiShake();
    float getVoiceAntiShake();
    void setMusicAntiShake(float antiShake);
    void setVoiceAntiShake(float antiShake);
    int setMusicGain(int key, float value);
    int setVoiceGain(int key, float value);
    void registerDataCallback(IVisualizerCallback callback);
    void unregisterDataCallback(IVisualizerCallback callback);
}
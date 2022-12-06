package com.autolink.lightshowcontrolpanel;

import android.app.PictureInPictureParams;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.autolink.audioextservice.IAudioExtControl;
import com.autolink.lightshowcontrolpanel.ui.MusicPanelFragment;
import com.autolink.lightshowcontrolpanel.ui.VoicePanelFragment;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.toString();
    public static final Gson GSON_INSTANCE;
    static {
        GSON_INSTANCE = new Gson();
    }

    public static final String UNKNOWN_STRING = null;
    public static final int STATE_SUCCESS = 0;
    public static final int STATE_ERROR = -1;

    private static final int INVOKE_GET_MUSIC_TACTIC = 0b000;
    private static final int INVOKE_GET_VOICE_TACTIC = 0b001;
    private static final int INVOKE_GET_MUSIC_SHAKE = 0b010;
    private static final int INVOKE_GET_VOICE_SHAKE = 0b100;
    public static final int INVOKE_SET_MUSIC_SHAKE = 0b110;
    public static final int INVOKE_SET_VOICE_SHAKE = 0b101;
    public static final int INVOKE_SET_MUSIC_GAIN = 0b011;
    public static final int INVOKE_SET_VOICE_GAIN = 0b111;

    public SharedPreferences mSharedPreferences;
    private ServiceConnection mConnection;
    public IAudioExtControl mBinder;
    private ArrayList<Fragment> mListFragment = new ArrayList<>();
    private PagerAdapter mViewPagerAdapter;
    private ViewPager2 mViewPager2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent();
        intent.setPackage("com.autolink.audioextservice");
        intent.setAction("com.autolink.audioextservice.AudioExtControlService");
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBinder = IAudioExtControl.Stub.asInterface(service);
                DATA_STORE.sMusicAntiShake = mSharedPreferences.getFloat("music@shake", remoteInvoker(INVOKE_GET_MUSIC_SHAKE, null));
                DATA_STORE.sVoiceAntiShake = mSharedPreferences.getFloat("voice@shake", remoteInvoker(INVOKE_GET_MUSIC_SHAKE, null));
                List<List<Float>> musicTactic = GSON_INSTANCE.fromJson((String) remoteInvoker(INVOKE_GET_MUSIC_TACTIC, null), ArrayList.class);
                List<List<Float>> voiceTactic = GSON_INSTANCE.fromJson((String) remoteInvoker(INVOKE_GET_VOICE_TACTIC, null), ArrayList.class);
                DATA_STORE.sMusicTactic = new float[musicTactic.size()][];
                DATA_STORE.sVoiceTactic = new float[voiceTactic.size()][];
                remoteInvoker(INVOKE_SET_MUSIC_SHAKE, DATA_STORE.sMusicAntiShake);
                remoteInvoker(INVOKE_SET_VOICE_SHAKE, DATA_STORE.sVoiceAntiShake);
                for (int i = 0; i < musicTactic.size(); i++){
                    float[] tac = new float[musicTactic.get(i).size() + 1];
                    tac[0] = 1;
                    for (int j = 0; j < musicTactic.get(i).size(); ++j){
                        tac[j+1] = ((Double)(Object)(musicTactic.get(i).get(j))).floatValue();
                    }
                    DATA_STORE.sMusicTactic[i] = tac;
                }
                for (int i = 0; i < voiceTactic.size(); i++){
                    float[] tac = new float[voiceTactic.get(i).size() + 1];
                    tac[0] = 1;
                    for (int j = 0; j < voiceTactic.get(i).size(); ++j){
                        tac[j + 1] = ((Double)(Object)(voiceTactic.get(i).get(j))).floatValue();
                    }
                    DATA_STORE.sVoiceTactic[i] = tac;
                }
                mViewPager2.setAdapter(mViewPagerAdapter);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        mListFragment.add(MusicPanelFragment.newInstance());
        mListFragment.add(VoicePanelFragment.newInstance());
        mViewPager2 = findViewById(R.id.view_container);
        mViewPagerAdapter = new PagerAdapter(getSupportFragmentManager(), getLifecycle());
        bindService(intent, mConnection, Service.BIND_AUTO_CREATE);
        mSharedPreferences = getSharedPreferences("policy_history", MODE_PRIVATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    public <T, R> T remoteInvoker(int invoke, R obj){
        T res = null;
        try {
            switch (invoke){
                case INVOKE_GET_MUSIC_TACTIC:
                    res = (T) mBinder.getMusicTactics();
                    break;
                case INVOKE_GET_VOICE_TACTIC:
                    res = (T) mBinder.getVoiceTactics();
                    break;
                case INVOKE_GET_MUSIC_SHAKE:
                    res = (T) (Float)mBinder.getMusicAntiShake();
                    break;
                case INVOKE_GET_VOICE_SHAKE:
                    res = (T) (Float)mBinder.getVoiceAntiShake();
                    break;
                case INVOKE_SET_MUSIC_SHAKE:
                    mBinder.setMusicAntiShake((float)obj);
                    break;
                case INVOKE_SET_VOICE_SHAKE:
                    mBinder.setVoiceAntiShake((float)obj);
                    break;
                case INVOKE_SET_MUSIC_GAIN:
                    Object[] p1 = (Object[]) obj;
                    mBinder.setMusicGain((int)p1[0], (float)p1[1]);
                    break;
                case INVOKE_SET_VOICE_GAIN:
                    Object[] p2 = (Object[]) obj;
                    mBinder.setVoiceGain((int)p2[0], (float)p2[1]);
                    break;
                default:
            }
        }catch (Exception exception){
            exception.printStackTrace();
        }
        return res;
    }

    private class PagerAdapter extends FragmentStateAdapter {

        public PagerAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @Override
        public Fragment createFragment(int position) {
            return mListFragment.get(position);
        }

        @Override
        public int getItemCount() {
            return mListFragment.size();
        }
    }
}

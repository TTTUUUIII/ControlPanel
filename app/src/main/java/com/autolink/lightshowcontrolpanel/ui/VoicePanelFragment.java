package com.autolink.lightshowcontrolpanel.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.autolink.audioextservice.IVisualizerCallback;
import com.autolink.lightshowcontrolpanel.DATA_STORE;
import com.autolink.lightshowcontrolpanel.MainActivity;
import com.autolink.lightshowcontrolpanel.R;
import com.autolink.lightshowcontrolpanel.ui.iview.ChartWebView;
import com.autolink.lightshowcontrolpanel.ui.iview.SpectrumDefaultView;


public class VoicePanelFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = VoicePanelFragment.class.getSimpleName();
    private static final int OFFSET = 6;
    private static final int SIZE = 7;

    private int timing = 0;
    private boolean mShowReview = false;
    private boolean mPageFinished = false;

    private float[][] mData;
    private byte[] mChartByte = new byte[SIZE];

    private MainActivity mParent;
    private Handler mHandler;
    private EditText mAntiShakeEditText;
    private SpectrumDefaultView mSpectrumView;
    private ChartWebView mChartWebView;
    private RecyclerView mRecyclerView;
    private VoicePanelFragment.RecyclerViewAdapter mViewAdapter;
    private Switch mReviewSwitch;
    private Button mSaveButton;
    private IVisualizerCallback.Stub mDataCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataCallback = new IVisualizerCallback.Stub() {
            @Override
            public void onDataChanged(byte[] sentByte) {
                if (mShowReview && timing % (20 * 2) == 0) {
                    System.arraycopy(sentByte, OFFSET, mChartByte, 0, mChartByte.length);
                    mHandler.post(() -> {
                        mSpectrumView.refresh(mChartByte);
                    });
                    if (timing % (20 * 140) == 0 && mPageFinished) {
                        mHandler.post(() -> {
                            mChartWebView.loadChart(new LineOption(SIZE).setData(mChartByte).toString());
                        });
                        timing = 0;
                    }
                }
                timing += 20;
            }
        };

        mData = DATA_STORE.sVoiceTactic;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_voice_control_panel, container);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAntiShakeEditText = view.findViewById(R.id.edit_text_anti_shake);
        mRecyclerView = view.findViewById(R.id.voice_recycler_view);
        mSpectrumView = view.findViewById(R.id.spectrum_view);
        mChartWebView = view.findViewById(R.id.chart_view);
        mReviewSwitch = view.findViewById(R.id.switch_review);
        mSaveButton = view.findViewById(R.id.save_btn);
        mSaveButton.setOnClickListener(this);
        mViewAdapter = new VoicePanelFragment.RecyclerViewAdapter(mParent.mSharedPreferences);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRecyclerView.setAdapter(mViewAdapter);
        mHandler = new Handler(Looper.getMainLooper());
        mAntiShakeEditText.setText("" + DATA_STORE.sVoiceAntiShake);
        mChartWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mPageFinished = true;
                mChartWebView.loadChart(new LineOption(SIZE).setData(new byte[SIZE]).toString());
            }
        });
        mReviewSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mShowReview = isChecked;
            if (!mShowReview) {
                mHandler.post(() -> {
                    mSpectrumView.refresh(new byte[SIZE]);
                    mChartWebView.loadChart(new LineOption(SIZE).setData(new byte[SIZE]).toString());
                });
            }
        });
        mAntiShakeEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    float antiShake = Float.parseFloat(mAntiShakeEditText.getText().toString());
                    DATA_STORE.sVoiceAntiShake = antiShake;
                    mParent.remoteInvoker(MainActivity.INVOKE_SET_VOICE_SHAKE, antiShake);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(VoicePanelFragment.this.requireContext(), "failed to apply change.", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "FocusChanged, shake=" + DATA_STORE.sVoiceAntiShake);
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mParent = (MainActivity) context;
    }

    @Override
    public void onStart() {
        super.onStart();
        registerDataCallback();
    }

    @Override
    public void onStop() {
        super.onStop();
        mViewAdapter.saveHistory();
//        String msg = mViewAdapter.saveHistory() == MainActivity.STATE_SUCCESS ? "写入配置成功" : "写入配置失败!";
//        Toast.makeText(mParent.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        unregisterDataCallback();
    }

    private void registerDataCallback() {
        try {
            mParent.mBinder.registerDataCallback(mDataCallback);
        } catch (RemoteException remoteException) {
            remoteException.printStackTrace();
        }
    }

    private void unregisterDataCallback() {
        try {
            mParent.mBinder.unregisterDataCallback(mDataCallback);
        } catch (RemoteException remoteException) {
            remoteException.printStackTrace();
        }
    }

    public static VoicePanelFragment newInstance() {

        Bundle args = new Bundle();
        VoicePanelFragment fragment = new VoicePanelFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_btn:
                String msg = mViewAdapter.saveHistory() == MainActivity.STATE_SUCCESS ? "保存成功" : "保存失败！";
                Toast.makeText(mParent.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private static class RecyclerViewHolder extends RecyclerView.ViewHolder {

        public RecyclerViewHolder(View itemView) {
            super(itemView);
        }

        public static RecyclerViewHolder createViewHolder(ViewGroup viewGroup) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_voice, viewGroup, false);
            return new RecyclerViewHolder(view);
        }
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
        private SharedPreferences sp;

        RecyclerViewAdapter(SharedPreferences preferences) {
            sp = preferences;
        }

        @Override
        public VoicePanelFragment.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return VoicePanelFragment.RecyclerViewHolder.createViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(VoicePanelFragment.RecyclerViewHolder holder, int position) {

            View itemView = holder.itemView;
            float[] data = mData[position + OFFSET];
            CheckBox checkBox = itemView.findViewById(R.id.is_show);
            TextView indexTextView = itemView.findViewById(R.id.text_view_index);
            EditText offsetTextView = itemView.findViewById(R.id.edit_text_offset);
            EditText gainTextView = itemView.findViewById(R.id.edit_text_gain);
            indexTextView.setText(String.format("%02d", position));
            String history = sp.getString("voice@" + (position + OFFSET), MainActivity.UNKNOWN_STRING);
            if (history != MainActivity.UNKNOWN_STRING) {
                try {
                    String[] hs = history.split(",");
                    int isChecked = (int) Float.parseFloat(hs[0]);
                    float gain = Float.parseFloat(hs[4]);
                    data[0] = isChecked;
                    data[4] = gain;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "failed to parse the history position=" + (position + OFFSET));
                }
            }
            offsetTextView.setText("" + data[3]);
            gainTextView.setText("" + data[4]);
            checkBox.setChecked(data[0] == 1 ? true : false);
            gainTextView.setEnabled(checkBox.isChecked());
            mParent.remoteInvoker(MainActivity.INVOKE_SET_VOICE_GAIN, new Object[]{position + OFFSET, data[0] == 1 ? data[4] : 0.0f});
            checkBox.setOnClickListener(v -> {
                float gain = 0.0f;
                data[0] = ((CheckBox) v).isChecked() ? 1 : 0;
                gainTextView.setEnabled(data[0] == 1 ? true : false);
                if (data[0] != 0) {
                    try {
                        gain = data[4];
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                };
                gainTextView.setEnabled(data[0] == 1 ? true : false);
                mParent.remoteInvoker(MainActivity.INVOKE_SET_VOICE_GAIN, new Object[]{position + OFFSET, gain});
            });
            gainTextView.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    try {
                        data[4] = Float.parseFloat(((EditText) (v)).getText().toString());
                        mParent.remoteInvoker(MainActivity.INVOKE_SET_VOICE_GAIN, new Object[]{position + OFFSET, data[4]});
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(mParent.getApplicationContext(), "应用失败，数据不合法！", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return SIZE;
        }

        public int saveHistory() {
            int retState = MainActivity.STATE_SUCCESS;
            try {
                sp.edit()
                        .putFloat("voice@shake", DATA_STORE.sVoiceAntiShake)
                        .apply();
                for (int i = OFFSET; i < OFFSET + SIZE; ++i) {
                    sp.edit()
                            .putString("voice@" + i, String.format("%f,%f,%f,%f,%f", mData[i][0], mData[i][1], mData[i][2], mData[i][3], mData[i][4]))
                            .apply();

                }
            } catch (Exception e) {
                e.printStackTrace();
                retState = MainActivity.STATE_ERROR;
            }
            return retState;
        }
    }
}

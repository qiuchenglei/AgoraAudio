package io.agora.audiocustomization.activity;

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.agora.audiocustomization.R;
import io.agora.audiocustomization.adapter.RcvCheckAdapter;
import io.agora.audiocustomization.adapter.RcvMusicListAdapter;
import io.agora.audiocustomization.adapter.RcvPrivateParameterListAdapter;
import io.agora.audiocustomization.model.MusicBean;
import io.agora.audiocustomization.model.PrivateParameterBean;
import io.agora.audiocustomization.util.SpUtils;

/**
 * Created by ChengleiQiu on 2018/1/23.
 */

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static Preference.OnPreferenceChangeListener mOnPfChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(newValue.toString());
                if (index < 0)
                    return false;
                listPreference.setSummary(listPreference.getEntries()[index]);
            } else {
                preference.setSummary(newValue.toString());
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(mOnPfChangeListener);

        mOnPfChangeListener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || MuteAudioPfFragment.class.getName().equals(fragmentName)
                || GatherAndPlayPfFragment.class.getName().equals(fragmentName)
                || ChannelAndRolePfFragment.class.getName().equals(fragmentName)
                || RawDataPfFragment.class.getName().equals(fragmentName)
                || MusicPlayAndMixingPfFragment.class.getName().equals(fragmentName)
                || PrivateParameterPfFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    //mute & disable Audio
    public static class MuteAudioPfFragment extends MyPreferenceFragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_mute_audio);
        }
    }

    public static class GatherAndPlayPfFragment extends MyPreferenceFragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_gather_and_play);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_gather_and_play_list_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sampling_rate_list_key)));
        }
    }

    public static class ChannelAndRolePfFragment extends MyPreferenceFragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_channel_and_role);
        }
    }

    public static class RawDataPfFragment extends MyPreferenceFragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_raw_data);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_samples_per_call_list_key)));
        }
    }

    public static class MusicPlayAndMixingPfFragment extends MyPreferenceFragment {
        private Activity activity;
        private RecyclerView rcvMusic;
        private TextView tvBtnDelete, tvBtnAdd;
        private RcvMusicListAdapter adapter;

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            activity = getActivity();
            View view = inflater.inflate(R.layout.pf_music_play_and_mixing, container, false);
            init(view);
            initListener();
            return view;
        }

        private void initListener() {
            adapter.setOnItemToggleCheckListener(new RcvCheckAdapter.OnItemToggleCheckListener() {
                @Override
                public void onToggleChecked(boolean isChecked, int position) {
                    int i = adapter.getSelectCount();
                    if (i > 0) {
                        tvBtnDelete.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                        tvBtnDelete.setText("Delete(" + i + ")");
                    } else {
                        tvBtnDelete.setTextColor(ContextCompat.getColor(activity, R.color.black));
                        tvBtnDelete.setText("Delete");
                    }
                }
            });
            tvBtnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.addItem(new MusicBean());
                }
            });
            tvBtnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.deleteSelect();
                    tvBtnDelete.setTextColor(ContextCompat.getColor(activity, R.color.black));
                    tvBtnDelete.setText("Delete");
                }
            });
        }

        private void init(View v) {
            rcvMusic = v.findViewById(R.id.rcv_music_list);
            tvBtnDelete = v.findViewById(R.id.tv_btn_delete);
            tvBtnAdd = v.findViewById(R.id.tv_btn_add);

            adapter = new RcvMusicListAdapter();
            rcvMusic.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
            rcvMusic.setAdapter(adapter);
            adapter.syncFromSp();
        }

        @Override
        public void onPause() {
            super.onPause();
            adapter.syncToSp();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                adapter.syncToSp();
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class PrivateParameterPfFragment extends MyPreferenceFragment {
        private Activity activity;
        private RecyclerView rcvPrivateParemeter;
        private TextView tvBtnDelete, tvBtnAdd;
        private RcvPrivateParameterListAdapter adapter;

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            activity = getActivity();
            View view = inflater.inflate(R.layout.pf_private_parameter, container, false);
            init(view);
            initListener();
            return view;
        }

        private void initListener() {
            adapter.setOnItemToggleCheckListener(new RcvCheckAdapter.OnItemToggleCheckListener() {
                @Override
                public void onToggleChecked(boolean isChecked, int position) {
                    int i = adapter.getSelectCount();
                    if (i > 0) {
                        tvBtnDelete.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));
                        tvBtnDelete.setText("Delete(" + i + ")");
                    } else {
                        tvBtnDelete.setTextColor(ContextCompat.getColor(activity, R.color.black));
                        tvBtnDelete.setText("Delete");
                    }
                }
            });
            tvBtnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.addItem(new PrivateParameterBean());
                }
            });
            tvBtnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.deleteSelect();
                    tvBtnDelete.setTextColor(ContextCompat.getColor(activity, R.color.black));
                    tvBtnDelete.setText("Delete");
                }
            });
        }

        private void init(View v) {
            rcvPrivateParemeter = v.findViewById(R.id.rcv_private_parameter_list);
            tvBtnDelete = v.findViewById(R.id.tv_btn_delete);
            tvBtnAdd = v.findViewById(R.id.tv_btn_add);

            adapter = new RcvPrivateParameterListAdapter();
            rcvPrivateParemeter.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false));
            rcvPrivateParemeter.setAdapter(adapter);
            adapter.syncFromSp();
        }

        @Override
        public void onPause() {
            super.onPause();
            adapter.syncToSp();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                adapter.syncToSp();
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(false);
        }

        @Override
        public boolean onContextItemSelected(MenuItem item) {
            return super.onContextItemSelected(item);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                getActivity().onBackPressed();
            }
            return super.onOptionsItemSelected(item);
        }
    }

}

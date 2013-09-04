package com.fusionx.lightirc.serversettings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.fusionx.common.PreferenceKeys;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.preferences.edittext.ServerTitleEditTextPreference;
import com.fusionx.lightirc.preferences.nick.NickPreference;
import com.fusionx.lightirc.views.MustBeCompleteView;

import org.apache.commons.lang3.StringUtils;

import static com.fusionx.common.PreferenceKeys.Title;
import static com.fusionx.common.PreferenceKeys.URL;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class BaseServerSettingsFragment extends PreferenceFragment implements Preference
        .OnPreferenceChangeListener {
    private MustBeCompleteView mCompleteView = null;
    private ServerTitleEditTextPreference mTitle = null;
    private EditTextPreference mUrl = null;
    private IServerSettings mCallback = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (IServerSettings) activity;
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);
        getPreferenceManager().setSharedPreferencesName(mCallback.getFileName());

        addPreferencesFromResource(R.xml.activty_server_settings_prefs);

        mTitle = (ServerTitleEditTextPreference) findPreference(Title);
        mTitle.setOnPreferenceChangeListener(this);
        mTitle.setListOfExistingServers(getActivity().getIntent().getStringArrayListExtra("list"));

        // URL of server
        mCompleteView = (MustBeCompleteView) findPreference("must_be_complete");

        // URL of server
        mUrl = (EditTextPreference) findPreference(URL);
        mUrl.setOnPreferenceChangeListener(this);

        Preference preference = findPreference("pref_autojoin_intent");
        assert preference != null;
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                final Intent intent = new Intent(getActivity(),
                        ChannelListActivity.class);
                intent.putExtra("filename", mCallback.getFileName());
                startActivity(intent);
                return false;
            }
        });

        if (!mCallback.canSaveChanges()) {
            setupNewServer();
            mCompleteView.setInitialText(mTitle.getTitle().toString());
        } else {
            getPreferenceScreen().removePreference(mCompleteView);
        }
    }

    private void setupNewServer() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences
                (getActivity());
        final String firstNick = preferences.getString(PreferenceKeys.DefaultFirstNick, "holoirc");
        final String secondNick = preferences.getString(PreferenceKeys.DefaultSecondNick, "");
        final String thirdNick = preferences.getString(PreferenceKeys.DefaultThirdNick, "");

        final String realName = preferences.getString(PreferenceKeys.DefaultRealName,
                "HoloIRCUser");
        final boolean autoNick = preferences.getBoolean(PreferenceKeys.DefaultAutoNickChange, true);

        final NickPreference nickPreference = (NickPreference) getPreferenceManager()
                .findPreference("pref_nick_storage");
        nickPreference.setFirstChoice(firstNick);
        nickPreference.setSecondChoice(secondNick);
        nickPreference.setThirdChoice(thirdNick);

        final EditTextPreference realNamePref = (EditTextPreference) getPreferenceManager()
                .findPreference(PreferenceKeys.RealName);
        realNamePref.setText(realName);
        final CheckBoxPreference autoNickPref = (CheckBoxPreference) getPreferenceManager()
                .findPreference(PreferenceKeys.AutoNickChange);
        autoNickPref.setChecked(autoNick);
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object newValue) {
        if (!mCallback.canSaveChanges()) {
            if (StringUtils.isEmpty(mTitle.getText())) {
                mCompleteView.setInitialText(mTitle.getTitle().toString());
                mCallback.setCanSaveChanges(false);
            } else if (StringUtils.isEmpty(mTitle.getText())) {
                mCompleteView.setInitialText(mUrl.getTitle().toString());
                mCallback.setCanSaveChanges(false);
            } else {
                mCallback.setCanSaveChanges(true);
                getPreferenceScreen().removePreference(mCompleteView);
            }
        }
        return true;
    }
}
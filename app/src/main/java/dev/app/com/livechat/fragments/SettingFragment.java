package dev.app.com.livechat.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v14.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import dev.app.com.livechat.R;
import dev.app.com.livechat.utils.PreferenceHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private ListPreference listPreference;
    private Spinner spinner;
    private CheckBox checkMute;
    private int[] frameRateArray;
    private String[] frameRateStringArray;
    private int frameRateSet;
    private boolean muteSet;
    private int frameRate;
    private boolean mute;
    private Button buttonSave;
    private PreferenceHelper preferencesHelper;

    public SettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        spinner = (Spinner) view.findViewById(R.id.spinner_framerate);
        checkMute = (CheckBox) view.findViewById(R.id.check_mute);
        buttonSave = (Button) view.findViewById(R.id.button_save);
        frameRateArray = getResources().getIntArray(R.array.frame_rate_values_list_preference);
        frameRateStringArray = getResources().getStringArray(R.array.frame_rate_list_preference);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (frameRateArray != null && frameRateArray.length >= position){
                    frameRate = frameRateArray[position];
                }
                toggleButton();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                toggleButton();
            }
        });
        checkMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mute = checkMute.isChecked();
                toggleButton();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preferencesHelper.store("framerate_set", frameRate);
                preferencesHelper.store("mute_set", mute);

                frameRateSet = frameRate;
                muteSet = mute;
                buttonSave.setEnabled(false);
                Snackbar.make(view, R.string.your_setting_saved_succesfully, Snackbar.LENGTH_LONG).show();
            }
        });

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        preferencesHelper = new PreferenceHelper(getContext(), "setting");
        frameRateSet = preferencesHelper.retrieve("framerate_set", 15);
        muteSet = preferencesHelper.retrieve("mute_set", false);

        spinner.setSelection(getIndex(frameRateSet));
        checkMute.setChecked(muteSet);
    }

    private int getIndex(int frameRateSet) {
        if (frameRateArray != null && frameRateArray.length > 0){
            for (int i=0; i < frameRateArray.length; i++){
                if (frameRateArray[i] == frameRateSet){
                    return i;
                }
            }
        }
        return 0;
    }

    private void toggleButton(){
        buttonSave.setEnabled(frameRateSet != frameRate || muteSet != mute);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

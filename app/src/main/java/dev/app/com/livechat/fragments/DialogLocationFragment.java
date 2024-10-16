package dev.app.com.livechat.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import dev.app.com.livechat.R;

public class DialogLocationFragment extends DialogFragment {

    private EditText textLocation;

    public static DialogLocationFragment newInstance(String location) {
//        EditNameDialogFragment frag = new EditNameDialogFragment();
//        Bundle args = new Bundle();
//        args.putString("title", title);
//        frag.setArguments(args);
//        return frag;

        Bundle args = new Bundle();
        
        DialogLocationFragment fragment = new DialogLocationFragment();
        args.putString("location", location);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_dialog_location, container);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textLocation = (EditText) view.findViewById(R.id.text_location);
        textLocation.requestFocus();
        String location = getArguments().getString("location", "");
        textLocation.setText(location);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }
}

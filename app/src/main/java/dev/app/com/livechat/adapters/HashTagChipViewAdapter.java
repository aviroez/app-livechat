package dev.app.com.livechat.adapters;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.plumillonforge.android.chipview.ChipViewAdapter;

import dev.app.com.livechat.R;
import dev.app.com.livechat.entities.HashTag;

public class HashTagChipViewAdapter extends ChipViewAdapter {
    private boolean hideHashTag = false;
    public HashTagChipViewAdapter(Context context) {
        super(context);
    }

    public HashTagChipViewAdapter(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public HashTagChipViewAdapter(Context context, boolean hideHashTag) {
        super(context);
        this.hideHashTag = hideHashTag;
    }

    @Override
    public int getLayoutRes(int position) {
        return R.layout.chip;
    }

    @Override
    public int getBackgroundRes(int position) {
        return 0;
    }

    @Override
    public int getBackgroundColor(int position) {
        return getColor(R.color.transparent_black);
    }

    @Override
    public int getBackgroundColorSelected(int position) {
        return 0;
    }

    @Override
    public void onLayout(View view, int position) {
        HashTag hashTag = (HashTag) getChip(position);
        TextView text1 = ((TextView) view.findViewById(android.R.id.text1));
        String showText = hashTag.getText() + " " + hashTag.getHashMap().size();
        if (!hideHashTag) {
            showText = "#" + showText;
        }
        text1.setText(showText);
    }
}

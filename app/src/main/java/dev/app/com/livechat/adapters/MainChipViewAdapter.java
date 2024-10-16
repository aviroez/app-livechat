package dev.app.com.livechat.adapters;

import android.view.View;

import com.plumillonforge.android.chipview.ChipViewAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.plumillonforge.android.chipview.Chip;
import com.plumillonforge.android.chipview.ChipViewAdapter;

import dev.app.com.livechat.R;
import dev.app.com.livechat.entities.Tag;

public class MainChipViewAdapter extends ChipViewAdapter {

    private int layout;

    public MainChipViewAdapter(Context context) {
        super(context);
    }

    public MainChipViewAdapter(Context context, int layout) {
        super(context);
        this.layout = layout;
    }

    @Override
    public int getLayoutRes(int position) {
        Tag tag = (Tag) getChip(position);

        if (layout > 0){
            return layout;
        }
        return R.layout.chip_close;
    }

    @Override
    public int getBackgroundColor(int position) {
        Tag tag = (Tag) getChip(position);

        return getColor(R.color.transparent_black);
    }

    @Override
    public int getBackgroundColorSelected(int position) {
        return 0;
    }

    @Override
    public int getBackgroundRes(int position) {
        return 0;
    }

    @Override
    public void onLayout(View view, int position) {
        Tag tag = (Tag) getChip(position);

        if (tag.getType() == 2) {
            ((TextView) view.findViewById(android.R.id.text1)).setTextColor(getColor(R.color.colorPrimary));
        }
    }
}

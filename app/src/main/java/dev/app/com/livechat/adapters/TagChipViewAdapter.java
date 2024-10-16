package dev.app.com.livechat.adapters;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.plumillonforge.android.chipview.Chip;
import com.plumillonforge.android.chipview.ChipViewAdapter;

import java.util.ArrayList;
import java.util.List;

import dev.app.com.livechat.R;
import dev.app.com.livechat.entities.Tag;

public class TagChipViewAdapter extends ChipViewAdapter {
    private Context context;
    private List<Chip> listChip = new ArrayList<>();
    private ImageButton buttonClose;

    public TagChipViewAdapter(Context context) {
        super(context);
        this.context = context;
    }

    public TagChipViewAdapter(Context context, List<Chip> listChip) {
        super(context);
        this.context = context;
        this.listChip = listChip;
    }

    public TagChipViewAdapter(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void add(Chip chip){
        listChip.add(chip);
    }

    @Override
    public int count() {
        return listChip.size();
    }

    @Override
    public int getLayoutRes(int position) {
        return R.layout.chip_close;
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
        final Tag tag = (Tag) getChip(position);
//        final Chip chip = listChip.get(position);
        TextView text = view.findViewById(android.R.id.text1);
        buttonClose = view.findViewById(R.id.button_close);
        text.setText(tag.getText());
    }
}

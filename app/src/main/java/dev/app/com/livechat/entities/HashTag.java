package dev.app.com.livechat.entities;

import com.plumillonforge.android.chipview.Chip;

import java.util.HashMap;

public class HashTag implements Chip {
    private String text;
    private HashMap<String, Object> hashMap = new HashMap<>();

    public HashTag() {
    }

    public HashTag(String text, HashMap<String, Object> hashMap) {
        this.text = text;
        this.hashMap = hashMap;
    }

    public void setText(String text) {
        this.text = text;
    }

    public HashMap<String, Object> getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap<String, Object> hashMap) {
        this.hashMap = hashMap;
    }

    public void addHashMap(String key, Object object) {
        if (hashMap != null){
            hashMap.put(key, object);
        }
    }

    @Override
    public String getText() {
        return text;
    }
}

package com.example.piculinary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AlphabetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_SECTION = 0;
    public static final int TYPE_ITEM = 1;

    private final List<Item> itemList;
    private final List<Item> originalItemList; // To keep track of original data
    private final OnItemClickListener listener;
    private OnDataChangedListener dataChangedListener;

    public AlphabetAdapter(List<RecipeItem> items, OnItemClickListener listener) {
        this.listener = listener;
        this.originalItemList = new ArrayList<>();
        this.itemList = new ArrayList<>();

        // Add all items and headers initially
        for (RecipeItem item : items) {
            originalItemList.add(new Item(item.getName(), TYPE_ITEM, item.getCategory()));
        }
        updateItemList();
    }

    public interface OnItemClickListener {
        void onItemClicked(RecipeItem item);
    }

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public List<Item> getOriginalItems() {
        return originalItemList;
    }

    public void setOnDataChangedListener(OnDataChangedListener listener) {
        this.dataChangedListener = listener;
    }

    public void filterByCategory(String category) {
        itemList.clear();
        if ("All".equals(category)) {
            updateItemList(); // Show all items with headers
        } else {
            String currentSection = "";
            for (Item item : originalItemList) {
                if (item.category.equals(category)) {
                    String firstChar = item.text.substring(0, 1).toUpperCase();
                    if (!firstChar.equals(currentSection)) {
                        currentSection = firstChar;
                        itemList.add(new Item(currentSection, TYPE_SECTION, ""));
                    }
                    itemList.add(item);
                }
            }
        }
        if (dataChangedListener != null) {
            dataChangedListener.onDataChanged();
        }
        notifyDataSetChanged();
    }

    private void updateItemList() {
        String currentSection = "";
        for (Item item : originalItemList) {
            String firstChar = item.text.substring(0, 1).toUpperCase();
            if (!firstChar.equals(currentSection)) {
                currentSection = firstChar;
                itemList.add(new Item(currentSection, TYPE_SECTION, ""));
            }
            itemList.add(item);
        }
    }

    public int getSectionPosition(String section) {
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).type == TYPE_SECTION && itemList.get(i).text.equals(section)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SECTION) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_section, parent, false);
            return new SectionViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = itemList.get(position);
        if (item.type == TYPE_SECTION) {
            ((SectionViewHolder) holder).sectionTitle.setText(item.text);
        } else {
            ((ItemViewHolder) holder).itemText.setText(item.text);
            holder.itemView.setOnClickListener(v -> listener.onItemClicked(new RecipeItem(item.text, item.category)));
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView sectionTitle;

        SectionViewHolder(View itemView) {
            super(itemView);
            sectionTitle = itemView.findViewById(R.id.sectionTitle);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView itemText;

        ItemViewHolder(View itemView) {
            super(itemView);
            itemText = itemView.findViewById(R.id.itemText);
        }
    }

    public static class Item {
        String text;
        int type;
        String category; // New field for category

        Item(String text, int type, String category) {
            this.text = text;
            this.type = type;
            this.category = category;
        }
    }
}

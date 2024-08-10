package com.example.piculinary;

public class RecipeItem {
    private final String name;
    private final String category;

    public RecipeItem(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}

package com.example.scbcchoi.eatemup.inventory;

//items to display on the inventory list;
//

public class InventoryListItem implements Comparable<InventoryListItem> {

    private String name;
    private int date;

    public InventoryListItem(String name, int date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return Integer.toString(date);
    }

    public int getDateInt(){
        return date;
    }

    @Override
    public int compareTo(InventoryListItem o) {
        return (this.date < o.date ? -1 : (this.date == o.date ? 0 : 1));
    }
}

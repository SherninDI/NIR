package com.example.nir;

public class ItemEpt implements Comparable<ItemEpt> {
    private String eptNameText;
    private String eptValueText;
    Integer eptValue;

    public Integer getEptValue() {
        return eptValue;
    }

    public String getEptNameText() {
        return eptNameText;
    }

    public String getEptValueText() {
        return eptValueText;
    }

    public String getEpt() {
        return eptValueText + " " + eptNameText;
    }


    public ItemEpt(String name, int value){
        this.eptNameText = name;
        this.eptValue = value;
        if ((value > 0) && (value < 1000)) {
            eptValueText = "F"+ value;
        } else if((value > 4000) && (value < 5000)) {
            value %= 1000;
            eptValueText = "A"+ value;
        }
    }


    @Override
    public int compareTo(ItemEpt o) {
        return this.eptValue.compareTo(o.eptValue);
    }
}

package com.example.nir;

public class ItemEpt {
    private String eptNameText;
    private String eptValueText;
    private int eptValue;

    public int getEptValue() {
        return eptValue;
    }

    public String getEptNameText() {
        return eptNameText;
    }

    public String getEptValueText() {
        return eptValueText;
    }

    public void setEptNameText(String eptNameText) {
        this.eptNameText = eptNameText;
    }

    public void setEptValue(int eptValue) {
        this.eptValue = eptValue;
    }

    public void setEptValueText(String eptValueText) {
        this.eptValueText = eptValueText;
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
}

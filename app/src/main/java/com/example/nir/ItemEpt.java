package com.example.nir;

public class ItemEpt implements Comparable<ItemEpt> {
    private String eptNameText;
    private String eptValueText;
    private String eptTypeText;
    private int eptAmpl;
    private int eptTime;
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

    public String getEptType() {
        return eptTypeText;
    }

    public int getEptAmpl() {
        return eptAmpl;
    }

    public int getEptTime() {
        return eptTime;
    }

    public ItemEpt(String name, int value, String type, int ampl, int time){
        this.eptNameText = name;
        this.eptValue = value;
        this.eptTypeText = type;
        this.eptAmpl = ampl;
        this.eptTime = time;
        if ((value > 0) && (value < 1000)) {
            eptValueText = "F"+ value;
        } else if((value > 4000) && (value < 5000)) {
            value %= 1000;
            eptValueText = "A"+ value;
        }
    }
    public ItemEpt(String name, int value, String type){
        this.eptNameText = name;
        this.eptValue = value;
        this.eptTypeText = type;
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

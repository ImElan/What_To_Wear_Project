package app.android.WhatToWear;

public class WardRobeClass
{
    public String category;
    public String climate;
    public String image_url;
    public String name;
    public String type;

    public WardRobeClass() {

    }

    public WardRobeClass(String category, String climate, String image_url, String name, String type) {
        this.category = category;
        this.climate = climate;
        this.image_url = image_url;
        this.name = name;
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getClimate() {
        return climate;
    }

    public void setClimate(String climate) {
        this.climate = climate;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

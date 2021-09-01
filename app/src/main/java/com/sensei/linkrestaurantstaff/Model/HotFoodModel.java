package com.sensei.linkrestaurantstaff.Model;

import java.util.List;

public class HotFoodModel {
    private boolean success;
    private String message;
    private List<HotFood> result;


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<HotFood> getResult() {
        return result;
    }

    public void setResult(List<HotFood> result) {
        this.result = result;
    }
}

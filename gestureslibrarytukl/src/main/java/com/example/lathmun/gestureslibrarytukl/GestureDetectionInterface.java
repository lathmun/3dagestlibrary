package com.example.lathmun.gestureslibrarytukl;

/**
 * Created by Lathmun on 1/6/16.
 */
public interface GestureDetectionInterface {
    public void registrationError(String errorMessage);
    public void GestureType(String gesture);
    public void continuosValues(String gesture, double []initialValues, double []newValues);


}

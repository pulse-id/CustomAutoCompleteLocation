package com.shishank.autocompletelocationview.interfaces;

import com.google.android.gms.location.places.Place;

/**
 * This class is used to provide data of places for geofences
 */
public interface OnQueryCompleteListener {

    void onTextClear();

    void onPlaceSelected(Place selectedPlace);
}

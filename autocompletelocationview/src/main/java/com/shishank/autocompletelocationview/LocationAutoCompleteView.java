package com.shishank.autocompletelocationview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;

import com.android.autocompletelocationview.R;
import com.shishank.autocompletelocationview.interfaces.OnTextCompleteListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

/**
 * Custom AutoCompleteView class to handle the adapter items for locations list.
 */
public class LocationAutoCompleteView extends android.support.v7.widget.AppCompatAutoCompleteTextView {

    private GoogleApiClient googleApiClient;
    private AutoPlaceCompleteAdapter autoPlaceCompleteAdapter;
    private OnTextCompleteListener onTextCompleteListener;

    public LocationAutoCompleteView(Context context) {
        this(context, null, -1);
    }

    public LocationAutoCompleteView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public LocationAutoCompleteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        googleApiClient = new GoogleApiClient.Builder(context).addApi(Places.GEO_DATA_API)
                .build();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        googleApiClient.connect();
        autoPlaceCompleteAdapter = new AutoPlaceCompleteAdapter(getContext(), googleApiClient);
        this.addTextChangedListener(textWatcher);
        this.setOnItemClickListener(onItemClickListener);
        this.setHint(R.string.place_autocomplete_search_hint);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.white_bg);
        this.setBackground(drawable);
        this.setAdapter(autoPlaceCompleteAdapter);
        setEnabled(true);
        setSingleLine(true);
        setEllipsize(TextUtils.TruncateAt.END);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    public void setOnTextCompleteListener(OnTextCompleteListener onTextCompleteListener) {
        this.onTextCompleteListener = onTextCompleteListener;
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (onTextCompleteListener != null) {
                onTextCompleteListener.onTextClear();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AutocompletePrediction autocompletePrediction = autoPlaceCompleteAdapter.getItem(position);
            if (autocompletePrediction != null) {
                PendingResult<PlaceBuffer> placeBufferPendingResult = Places.GeoDataApi.
                        getPlaceById(googleApiClient, autocompletePrediction.getPlaceId());
                placeBufferPendingResult.setResultCallback(updatePlaceCallback);
            }
        }
    };

    private ResultCallback<PlaceBuffer> updatePlaceCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (places.getStatus().isSuccess()) {
                final Place place = places.get(0);
                if (onTextCompleteListener != null) {
                    onTextCompleteListener.onPlaceSelected(place);
                }
                places.release();
            }
        }
    };
}

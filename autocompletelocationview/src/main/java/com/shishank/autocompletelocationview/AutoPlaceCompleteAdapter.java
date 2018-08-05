package com.shishank.autocompletelocationview;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AutoPlaceCompleteAdapter extends ArrayAdapter<AutocompletePrediction> implements Filterable {

    private final GoogleApiClient googleApiClient;
    private List<AutocompletePrediction> predictionList;

    public AutoPlaceCompleteAdapter(@NonNull Context context, GoogleApiClient googleApiClient) {
        super(context, android.R.layout.simple_expandable_list_item_2, android.R.id.text1);
        this.googleApiClient = googleApiClient;
        predictionList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return predictionList.size();
    }

    @Nullable
    @Override
    public AutocompletePrediction getItem(int position) {
        return predictionList.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                List<AutocompletePrediction> filterList = new ArrayList<>();
                if (!TextUtils.isEmpty(constraint)) {
                    filterList = getPredictionList(constraint);
                }
                if (filterList != null) {
                    filterResults.values = filterList;
                    filterResults.count = filterList.size();
                } else {
                    filterResults.count = 0;
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    predictionList = (ArrayList<AutocompletePrediction>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if (resultValue instanceof AutocompletePrediction) {
                    return ((AutocompletePrediction) resultValue).getFullText(null);
                }
                return super.convertResultToString(resultValue);
            }
        };
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        AutocompletePrediction autocompletePrediction = getItem(position);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
        TextView tvPrimary = view.findViewById(android.R.id.text1);
        TextView tvSecondary = view.findViewById(android.R.id.text2);
        tvPrimary.setMaxLines(1);
        tvPrimary.setEllipsize(TextUtils.TruncateAt.END);
        tvSecondary.setMaxLines(1);
        tvSecondary.setEllipsize(TextUtils.TruncateAt.END);
        if (autocompletePrediction != null) {
            tvPrimary.setText(autocompletePrediction.getPrimaryText(new StyleSpan(Typeface.BOLD)));
            tvSecondary.setText(autocompletePrediction.getSecondaryText(new StyleSpan(Typeface.BOLD)));
        }
        return view;
    }

    /**
     * @param constraint the sequence of characters to matched with places
     * @return list of predictions matched from the constraint
     */
    private List<AutocompletePrediction> getPredictionList(CharSequence constraint) {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            PendingResult<AutocompletePredictionBuffer> pendingResult = Places.GeoDataApi.
                    getAutocompletePredictions(googleApiClient, constraint.toString(), null, null);
            AutocompletePredictionBuffer autocompletePredictions = pendingResult.await(60, TimeUnit.SECONDS);
            Status status = autocompletePredictions.getStatus();
            if (status.isSuccess()) {
                return DataBufferUtils.freezeAndClose(autocompletePredictions);
            } else {
                Log.d("AutoCompleteAdapter", "Might be dev error" + status.getStatusMessage());
                return null;
            }
        }
        return null;
    }
}

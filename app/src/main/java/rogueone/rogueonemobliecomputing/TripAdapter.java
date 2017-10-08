package rogueone.rogueonemobliecomputing;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;

import java.io.Serializable;
import java.util.List;

import fisk.chipcloud.ChipCloud;
import fisk.chipcloud.ChipCloudConfig;
import rogueone.rogueonemobliecomputing.Interfaces.APIClient;
import rogueone.rogueonemobliecomputing.Models.Trip;
import rogueone.rogueonemobliecomputing.Models.TripMate;

/**
 * Created by jayas on 6/10/2017.
 */

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder>{
        private List<Trip> mDataset;
        private static APIClient client;
        private static Context mcontext;
        private String mToken;
        public TripAdapter(List<Trip> myDataset, APIClient Contextclient, Context context,String token) {
            mDataset = myDataset;
            client = Contextclient;
            mcontext = context;
            mToken = token;
        }

public class ViewHolder extends RecyclerView.ViewHolder {
    // each data item is just a string in this case
    public RelativeLayout mRelativeLayout;
    public TextView mTripNameTextView;
    public TextView mDestinationTextView;
    public TextView mStartDateTextView;
    public FlexboxLayout mMatesFlexboxLayout;
    public TextView mDurationTextView;
    public TextView mDescriptionTextView;
    public Button mTripEntriesTextView;
/*
    public ChipView mChipView;
*/
    public ViewHolder(RelativeLayout v) {
        super(v);
        mTripNameTextView = (TextView) v.findViewById(R.id.my_trip_name);
        mDestinationTextView = (TextView) v.findViewById(R.id.destination);
        mMatesFlexboxLayout = (FlexboxLayout) v.findViewById(R.id.mates);
        mStartDateTextView = (TextView) v.findViewById(R.id.start_date);
        mDurationTextView = (TextView) v.findViewById(R.id.duration);
        mDescriptionTextView = (TextView) v.findViewById(R.id.description);
        mTripEntriesTextView = (Button) v.findViewById(R.id.trip_entries);
/*
        mChipView = (ChipView) v.findViewById(R.id.mate);
*/
        mRelativeLayout = v;
    }
}
    public static void showErrorToast(Throwable t,Context context){
        Toast.makeText(context,t.getLocalizedMessage(),Toast.LENGTH_LONG).show();
    }
    @Override
    public TripAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trip_view, parent,false);

        //set the view's size, margins, paddings and layout parameters...
        TripAdapter.ViewHolder vh = new TripAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(TripAdapter.ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTripNameTextView.setText(mDataset.get(position).TripName);
        holder.mDestinationTextView.setText(mDataset.get(position).Destination);
        holder.mStartDateTextView.setText(mDataset.get(position).StartDate);
        holder.mDurationTextView.setText(mDataset.get(position).PlannedDuration);
        holder.mDescriptionTextView.setText(mDataset.get(position).Description);
        holder.mTripEntriesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mcontext,DiaryActivity.class);
                intent.putExtra("token",mToken);
                intent.putExtra("entries",(Serializable)mDataset.get(position).TripEntries);
                mcontext.startActivity(intent);
            }
        });
        ChipCloudConfig config = new ChipCloudConfig()
                .selectMode(ChipCloud.SelectMode.multi)
                .checkedChipColor(Color.parseColor("#ddaa00"))
                .checkedTextColor(Color.parseColor("#ffffff"))
                .uncheckedChipColor(Color.parseColor("#ddaa00"))
                .uncheckedTextColor(Color.parseColor("#ffffff"))
                .useInsetPadding(true);
        ChipCloud chipCloud = new ChipCloud(mcontext, holder.mMatesFlexboxLayout,config);
        for (TripMate mate:mDataset.get(position).TripMates) {
            chipCloud.addChip(mate.Name+"\n"+mate.PhoneNumber);
        }
    }
    @Override
    public int getItemCount() {return mDataset.size();}
}

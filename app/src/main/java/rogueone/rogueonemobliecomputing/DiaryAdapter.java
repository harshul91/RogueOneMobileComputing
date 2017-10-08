package rogueone.rogueonemobliecomputing;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

import fisk.chipcloud.ChipCloud;
import fisk.chipcloud.ChipCloudConfig;
import rogueone.rogueonemobliecomputing.Interfaces.APIClient;
import rogueone.rogueonemobliecomputing.Models.LocationEntry;

/**
 * Created by jayas on 6/10/2017.
 */

class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.ViewHolder> {
    private List<LocationEntry> mDataset;
    private static APIClient client;
    private static Context mcontext;
    public DiaryAdapter(List<LocationEntry> myDataset, APIClient Contextclient, Context context) {
        mDataset = myDataset;
        client = Contextclient;
        mcontext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public RelativeLayout mRelativeLayout;
        public TextView mAddressTextView;
        public TextView mFirstCheckInTextView;
        /*public ChipsInput mEntryTagsTextView;*/
        public TextView mComments;
        public TextView mCheckIns;
        public FlexboxLayout mFlexboxLayout;
        public ViewHolder(RelativeLayout v) {
            super(v);
            mAddressTextView = (TextView) v.findViewById(R.id.address);
            mFirstCheckInTextView = (TextView) v.findViewById(R.id.first_checkin);
            mFlexboxLayout = (FlexboxLayout) v.findViewById(R.id.entry_tags);
            mComments = (TextView) v.findViewById(R.id.comments);
            mCheckIns = (TextView) v.findViewById(R.id.all_checkins);
            mRelativeLayout = v;
        }
    }
    public static void showErrorToast(Throwable t,Context context){
        Toast.makeText(context,t.getLocalizedMessage(),Toast.LENGTH_LONG).show();
    }
    @Override
    public DiaryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.diary_view, parent,false);

        //set the view's size, margins, paddings and layout parameters...
        DiaryAdapter.ViewHolder vh = new DiaryAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(DiaryAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mAddressTextView.setText(mDataset.get(position).Address);
        holder.mFirstCheckInTextView.setText(mDataset.get(position).DateCreated);
        int no_of_checkins = mDataset.get(position).CheckIns.size();
        holder.mCheckIns.setText(Integer.toString(no_of_checkins));
        holder.mComments.setText(mDataset.get(position).Comments);
        if(mDataset.get(position).BadgeNames.size()==0){
            holder.mFlexboxLayout.setVisibility(View.GONE);
        }else{
            /*for(String label:mDataset.get(position).BadgeNames){
                holder.mEntryTagsTextView.addChip(label,null);
            }*/
            ChipCloudConfig config = new ChipCloudConfig()
                    .selectMode(ChipCloud.SelectMode.multi)
                    .checkedChipColor(Color.parseColor("#ddaa00"))
                    .checkedTextColor(Color.parseColor("#ffffff"))
                    .uncheckedChipColor(Color.parseColor("#ddaa00"))
                    .uncheckedTextColor(Color.parseColor("#ffffff"))
                    .useInsetPadding(true);
            ChipCloud chipCloud = new ChipCloud(mcontext, holder.mFlexboxLayout,config);
            for (String badge:mDataset.get(position).BadgeNames) {
                chipCloud.addChip(badge);
            }
        }

    }

    @Override
    public int getItemCount() {return mDataset.size();}
}

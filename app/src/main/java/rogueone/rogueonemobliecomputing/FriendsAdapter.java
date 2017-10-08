package rogueone.rogueonemobliecomputing;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import rogueone.rogueonemobliecomputing.Interfaces.APIClient;

/**
 * Created by jayas on 5/10/2017.
 */

class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {
    private List<String> mDataset;
    private static APIClient client;
    private static Context mcontext;
    private String mToken;
    public FriendsAdapter(List<String> myDataset, APIClient Contextclient, Context context,String token) {
        mDataset = myDataset;
        client = Contextclient;
        mcontext = context;
        mToken = token;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public RelativeLayout mRelativeLayout;
        public TextView mTextView;
        public Button button;
        public ViewHolder(RelativeLayout v) {
            super(v);
            button = (Button) v.findViewById(R.id.go_to_profile);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mcontext,ProfileActivity.class);
                    intent.putExtra("token",mToken);
                    intent.putExtra("userEmail",mTextView.getText().toString());
                    mcontext.startActivity(intent);
                }
            });
            mTextView = (TextView) v.findViewById(R.id.friend);
            mRelativeLayout = v;
        }
    }
    public static void showErrorToast(Throwable t,Context context){
        Toast.makeText(context,t.getLocalizedMessage(),Toast.LENGTH_LONG).show();
    }
    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_view, parent,false);

        //set the view's size, margins, paddings and layout parameters...
        FriendsAdapter.ViewHolder vh = new FriendsAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(FriendsAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mDataset.get(position));
    }

    @Override
    public int getItemCount() {return mDataset.size();}
}

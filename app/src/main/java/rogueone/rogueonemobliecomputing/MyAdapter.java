package rogueone.rogueonemobliecomputing;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rogueone.rogueonemobliecomputing.Interfaces.APIClient;

/**
 * Created by jayas on 5/10/2017.
 */

class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>  {
    private List<String> mDataset;
    private static APIClient client;
    private static Context mcontext;
    public MyAdapter(List<String> myDataset, APIClient Contextclient, Context context) {
        mDataset = myDataset;
        client = Contextclient;
        mcontext = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public RelativeLayout mRelativeLayout;
        public TextView mTextView;
        public Button button;
        public Button decline;
        public ViewHolder(RelativeLayout v) {
            super(v);
            button = (Button) v.findViewById(R.id.accept);
            mTextView = (TextView) v.findViewById(R.id.request);
            decline = (Button) v.findViewById(R.id.decline);
            decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String Username = mTextView.getText().toString();
                    final int position = getAdapterPosition();
                    Call<ResponseBody> call = client.DeclineRequest(Username);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Toast.makeText(mcontext,"Declined successfully",Toast.LENGTH_LONG).show();
                            removeAt(position);
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            showErrorToast(t,mcontext);
                        }
                    });
                }
            });
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String Username = mTextView.getText().toString();
                    final int position = getAdapterPosition();
                    Call<ResponseBody> call = client.ConfirmRequest(Username);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if(response.isSuccessful()){
                                Toast.makeText(mcontext,"Accepted successfully",Toast.LENGTH_LONG).show();
                                removeAt(position);
                            }else {
                                showErrorToast(new Throwable("Error occured while accepting the request"),mcontext);
                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            showErrorToast(t,mcontext);
                        }
                    });
                }
            });
            mRelativeLayout = v;
        }
    }
    public static void showErrorToast(Throwable t,Context context){
        Toast.makeText(context,t.getLocalizedMessage(),Toast.LENGTH_LONG).show();
    }
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.request_view, parent,false);

        //set the view's size, margins, paddings and layout parameters...
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mDataset.get(position));
    }

    @Override
    public int getItemCount() {return mDataset.size();}
    public void removeAt(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mDataset.size());
    }
}

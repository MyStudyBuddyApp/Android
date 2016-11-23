package com.appnew.afonsohometracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String[] devStates = {"Sewer & Slab",
            "Wall & Roof frame",
            "Roofing",
            "Lock up of home",
            "Bricklaying",
            "Internal Plastering",
            "Kitchen & fixout",
            "Painting",
            "Internal tiling",
            "Floor coverings",
            "Fittings & Fixtures",
            "Home Completion"};

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ImageButton back;
    private ImageButton forward;
    private SeekBar devStateBar;
    private TextView devStateTextView;
    private TextView devStateSummaryTextView;
    private LinearLayout imagesLayout;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDB;
    private FirebaseStorage mStore;

    private User CurUser;

    public int devState = 0;
    public int maxDevState = 0;
    public ArrayList<ArrayList<Bitmap>> images = new ArrayList<ArrayList<Bitmap>>();

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseDatabase.getInstance();
        mStore = FirebaseStorage.getInstance();

        for(int n=0; n<=11; n++) {
            images.add(new ArrayList<Bitmap>());
        }

        LoadData();
    }

    private void LoadData(){
        mDB.getReference().child("users").child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                SetInfo(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void SetInfo(User user){
        maxDevState = user.homeDev;
        devState = maxDevState;
        CurUser = user;
        InitInfo();
        UpdateInfo();
    }

    private void UpdateInfo(){
        devStateBar.setProgress(Double.valueOf((Double.valueOf(devState) / 11 * 100)).intValue());
        devStateTextView.setText(devStates[devState]);
        UpdateLinearLayout();
    }

    public void InitInfo(){
        int n = 0;

        for(n=0; n<=maxDevState; n++) {
            final int finalN = n;
            mDB.getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("pictures").child("" + finalN)
                    .addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    String downloadURL = dataSnapshot.getValue().toString();
                    StorageReference storageReference = mStore.getReferenceFromUrl(downloadURL);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            images.get(finalN).add(bmp);
                            Log.d("Image", "loaded" + finalN + ", " + images.get(finalN).size());
                            UpdateLinearLayout();
                        }
                    });
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void UpdateLinearLayout(){
        imagesLayout.removeAllViews();
        for(int n=0; n < images.get(devState).size(); n++) {
            ImageView imageView = new ImageView(getActivity());
            imageView.setImageBitmap(images.get(devState).get(n));
            imagesLayout.addView(imageView);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        back = (ImageButton) view.findViewById(R.id.backbutton);
        forward = (ImageButton) view.findViewById(R.id.forwardbutton);
        devStateBar = (SeekBar) view.findViewById(R.id.seekBar);
        devStateTextView = (TextView) view.findViewById(R.id.devStateText);
        devStateSummaryTextView = (TextView) view.findViewById(R.id.DevSummary);
        imagesLayout = (LinearLayout) view.findViewById(R.id.ImagesView);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (devState - 1 >= 0){
                    devState -= 1;
                    UpdateInfo();
                }
            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (devState + 1 <= maxDevState){
                    devState += 1;
                    UpdateInfo();
                }
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

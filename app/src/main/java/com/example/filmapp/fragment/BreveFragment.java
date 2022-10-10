package com.example.filmapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmapp.R;
import com.example.filmapp.adapter.AdapterBreve;
import com.example.filmapp.helper.FirebaseHelper;
import com.example.filmapp.model.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BreveFragment extends Fragment {

	private ProgressBar progressBar;
	private RecyclerView rvPosts;

	private List<Post> postList = new ArrayList<>();
	private AdapterBreve adapterBreve = new AdapterBreve(postList);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_breve, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		iniciaComponentes(view);
		configRv();
		recuperaPost();
	}

	private void iniciaComponentes(View view){
		rvPosts = view.findViewById(R.id.rvPosts);
		progressBar = view.findViewById(R.id.progressBar);
	}

	private void recuperaPost() {
		DatabaseReference postRef = FirebaseHelper.getDatabaseReference()
				.child("posts");
		postRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				for (DataSnapshot ds : snapshot.getChildren()){
					postList.add(ds.getValue(Post.class));
				}
				progressBar.setVisibility(View.GONE);
				adapterBreve.notifyDataSetChanged();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {

			}
		});
	}

	private void configRv(){
		rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
		rvPosts.setHasFixedSize(true);
		adapterBreve = new AdapterBreve(postList);
		rvPosts.setAdapter(adapterBreve);
	}

}
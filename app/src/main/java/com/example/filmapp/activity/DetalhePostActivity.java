package com.example.filmapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filmapp.R;
import com.example.filmapp.adapter.AdapterPost;
import com.example.filmapp.helper.FirebaseHelper;
import com.example.filmapp.model.Download;
import com.example.filmapp.model.Post;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DetalhePostActivity extends AppCompatActivity {

	private List<Post> postList = new ArrayList<>();
	private List<String> downloadList = new ArrayList<>();
	private AdapterPost adapterPost;

	private TextView textTitle;
	private ImageView imagePost;
	private ImageView imageFake;
	private TextView textYear;
	private TextView textTime;
	private TextView textCast;
	private ConstraintLayout btnPlay;
	private ConstraintLayout btnDownload;
	private TextView textSynopsis;
	private RecyclerView rvPosts;

	private Post post;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_post);

		iniciaComponentes();
		configRv();
		configDados();
		configCliques();
		recuperaPost();
		recuperaDownloads();
	}

	private void configDados() {
		post = (Post) getIntent().getSerializableExtra("postSelecionado");

		textTitle.setText(post.getTitulo());

		imageFake.setVisibility(View.GONE);

		Picasso.get().load(post.getImagem()).into(imagePost);

		textYear.setText(post.getAno());
		textTime.setText(getString(R.string.text_duracao, post.getDuracao()));
		textSynopsis.setText(post.getSinopse());
		textCast.setText(post.getElenco());
	}

	private void configCliques() {
		findViewById(R.id.ibVoltar).setOnClickListener(view -> {
			startActivity(new Intent(this, MainActivity.class));
			finish();
		});
		findViewById(R.id.btnBaixar).setOnClickListener(view -> efetuarDownload());
	}

	private void efetuarDownload() {

		if (!downloadList.contains(post.getId())){
			downloadList.add(post.getId());
			Download.salvar(downloadList);
			Toast.makeText(getBaseContext(), "Download efetuado com sucesso!", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(getBaseContext(), "Download já efetuado anteriormente!", Toast.LENGTH_SHORT).show();
		}
	}

	private void recuperaDownloads() {
		DatabaseReference downloadRef = FirebaseHelper.getDatabaseReference()
				.child("downloads");
		downloadRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				for (DataSnapshot ds : snapshot.getChildren()){
					downloadList.add(ds.getValue(String.class));
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {

			}
		});
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
				adapterPost.notifyDataSetChanged();
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {

			}
		});
	}

	private void configRv(){
		rvPosts.setLayoutManager(new GridLayoutManager(this, 3));
		rvPosts.setHasFixedSize(true);
		adapterPost = new AdapterPost(postList, this);
		rvPosts.setAdapter(adapterPost);
	}

	private void iniciaComponentes() {
		textTitle = findViewById(R.id.textTitulo);
		imagePost = findViewById(R.id.imagemPost);
		imageFake = findViewById(R.id.imageFake);
		textYear = findViewById(R.id.textAno);
		textTime = findViewById(R.id.textDuracao);
		textCast = findViewById(R.id.textElenco);
		btnPlay = findViewById(R.id.btnAssistir);
		btnDownload = findViewById(R.id.btnBaixar);
		textSynopsis = findViewById(R.id.textSinopse);
		rvPosts = findViewById(R.id.rvPosts);
	}
}
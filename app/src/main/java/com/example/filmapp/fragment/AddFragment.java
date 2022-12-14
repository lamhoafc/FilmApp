package com.example.filmapp.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.filmapp.R;
import com.example.filmapp.helper.FirebaseHelper;
import com.example.filmapp.model.Post;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.List;


public class AddFragment extends Fragment {

	private final int SELECAO_GALERIA = 100;
	private String caminhoImagem = null;

	private Button btnSalvar;
	private ImageView imageView;
	private ImageView imageFake;
	private ProgressBar progressBar;

	private EditText editTitulo;
	private EditText editGenero;
	private EditText editElenco;
	private EditText editAno;
	private EditText editDuracao;
	private EditText editSinopse;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_add, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		iniciaComponentes(view);
		configCliks();
	}



	private void validaDados() {
		String titulo = editTitulo.getText().toString().trim();
		String genero = editGenero.getText().toString().trim();
		String elenco = editElenco.getText().toString().trim();
		String ano = editAno.getText().toString().trim();
		String duracao = editDuracao.getText().toString().trim();
		String sinopse = editSinopse.getText().toString().trim();

		if (!titulo.isEmpty()) {
			if (!genero.isEmpty()) {
				if (!elenco.isEmpty()) {
					if (!ano.isEmpty()) {
						if (!duracao.isEmpty()) {
							if (!sinopse.isEmpty()) {
								if (caminhoImagem != null) {

									progressBar.setVisibility(View.VISIBLE);

									Post post = new Post();
									post.setTitulo(titulo);
									post.setGenero(genero);
									post.setElenco(elenco);
									post.setAno(ano);
									post.setDuracao(duracao);
									post.setSinopse(sinopse);

									salvarImagemFirebase(post);

								} else {
									Toast.makeText(getActivity(), "Seleciona uma imagem.", Toast.LENGTH_SHORT).show();
								}
							} else {
								editSinopse.setError("Informa????o obrigat??ria.");
							}
						} else {
							editDuracao.setError("Informa????o obrigat??ria.");
						}
					} else {
						editAno.setError("Informa????o obrigat??ria.");
					}
				} else {
					editElenco.setError("Informa????o obrigat??ria.");
				}
			} else {
				editGenero.setError("Informa????o obrigat??ria.");
			}
		} else {
			editTitulo.setError("Informa????o obrigat??ria.");
		}
	}

	private void salvarImagemFirebase(Post post) {
		StorageReference StorageReference = FirebaseHelper.getStorageReference()
				.child("imagens")
				.child("posts")
				.child(post.getId() + "jpeg");

		UploadTask uploadTask = StorageReference.putFile(Uri.parse(caminhoImagem));
		uploadTask.addOnSuccessListener(taskSnapshot -> StorageReference.getDownloadUrl().addOnCompleteListener(task -> {

			post.setImagem(task.getResult().toString());
			post.salvar();

			// Limpa os campos apos o cadastro
			limparCampos();

		})).addOnFailureListener(e -> {
			progressBar.setVisibility(View.GONE);
			Toast.makeText(getActivity(), "Erro ao salvar cadastro!", Toast.LENGTH_SHORT).show();
		});
	}

	private void limparCampos() {
		imageView.setImageBitmap(null);
		imageFake.setVisibility(View.VISIBLE);

		editTitulo.getText().clear();
		editGenero.getText().clear();
		editElenco.getText().clear();
		editAno.getText().clear();
		editDuracao.getText().clear();
		editSinopse.getText().clear();
		progressBar.setVisibility(View.GONE);
		ocultarTeclado();
	}

	private void ocultarTeclado() {
		InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(editTitulo.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}


	private void configCliks() {
		imageView.setOnClickListener(view -> verificaPermissaoGaleria());
		btnSalvar.setOnClickListener(view -> validaDados());
	}

	private void verificaPermissaoGaleria() {

		PermissionListener permissionlistener = new PermissionListener() {
			@Override
			public void onPermissionGranted() {
				abrirGaleria();
			}

			@Override
			public void onPermissionDenied(List<String> deniedPermissions) {
				Toast.makeText(getActivity(), "Permiss??o Negada", Toast.LENGTH_SHORT).show();
			}
		};

		TedPermission.create()
				.setPermissionListener(permissionlistener)
				.setDeniedTitle("Permiss??es")
				.setDeniedMessage("Se voc?? n??o aceitar a permiss??o n??o poder?? acessar a Galeria do dispositivo, deseja ativar a permiss??o agora ?")
				.setDeniedCloseButtonText("N??o")
				.setGotoSettingButtonText("Sim")
				.setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
				.check();
	}

	private void abrirGaleria() {
		Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, SELECAO_GALERIA);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == SELECAO_GALERIA) {
				Uri imagemSelecionada = data.getData();
				caminhoImagem = imagemSelecionada.toString();

				try {
					Bitmap bitmap;

					if (Build.VERSION.SDK_INT < 28) {
						bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imagemSelecionada);
					} else {
						ImageDecoder.Source source = ImageDecoder.createSource(getActivity().getContentResolver(), imagemSelecionada);
						bitmap = ImageDecoder.decodeBitmap(source);
					}

					imageFake.setVisibility(View.GONE);
					imageView.setImageBitmap(bitmap);

				} catch (Exception e) {
					e.printStackTrace();
					imageFake.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	private void iniciaComponentes(View view) {
		btnSalvar = view.findViewById(R.id.btnSalvar);
		imageView = view.findViewById(R.id.imagemPost);
		imageFake = view.findViewById(R.id.imageFake);
		progressBar = view.findViewById(R.id.progressBar);

		editTitulo = view.findViewById(R.id.edtTitulo);
		editGenero = view.findViewById(R.id.edtGenero);
		editElenco = view.findViewById(R.id.edtElenco);
		editAno = view.findViewById(R.id.edtAno);
		editDuracao = view.findViewById(R.id.edtDuracao);
		editSinopse = view.findViewById(R.id.EdtSinopse);
	}
}
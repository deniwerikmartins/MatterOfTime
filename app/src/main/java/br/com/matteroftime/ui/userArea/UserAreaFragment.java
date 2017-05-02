package br.com.matteroftime.ui.userArea;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import br.com.matteroftime.R;
import br.com.matteroftime.core.listeners.OnMusicSelectedListener;
import br.com.matteroftime.models.Musica;
import br.com.matteroftime.ui.selectMusic.SelectMusicDialogFragment;
import br.com.matteroftime.ui.uploadMusic.UploadMusicFragment;
import br.com.matteroftime.util.Constants;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.R.id.message;


public class UserAreaFragment extends Fragment implements UserAreaContract.View, OnMusicSelectedListener{


    private View view;
    private UserAreaAdapter adapter;
    private UserAreaContract.Actions presenter;
    private SelectMusicDialogFragment selectMusicDialogFragment;
    private UploadMusicFragment uploadMusicFragment;
    private Musica musicaUpload;
    private Musica musicaDownload;
    private Context context;


    @BindView(R.id.user_area_recycler_view) RecyclerView userAreaRecyclerView;
    @BindView(R.id.btnSelecionarMusica) Button selecionarMusica;
    @BindView(R.id.btnEnviarMusica) Button enviarMusica;
    @BindView(R.id.btnAtualizarMusica) Button atualizarMusica;
    @BindView(R.id.txtMusicaBaixar) TextView musicaBaixar;
    @BindView(R.id.edtMusicaPesquisar) EditText pesquisarMusica;
    @BindView(R.id.btnBaixarMusica) Button btnbaixarMusica;
    @BindView(R.id.btnPesquisar) Button btnPesquisarMusica;
    @BindView(R.id.txtSemMusicas) TextView semMusicas;



    public UserAreaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getBaseContext();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_area, container,false);
        musicaUpload = new Musica();
        musicaDownload = new Musica();
        ButterKnife.bind(this, view);
        presenter = new UserAreaPresenter(this);

        //setup Recyclerview
        List<Musica> tempMusicas = new ArrayList<>();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        adapter = new UserAreaAdapter(tempMusicas, getContext(), this);
        userAreaRecyclerView.setLayoutManager(layoutManager);
        userAreaRecyclerView.setAdapter(adapter);



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //presenter.loadMusics();
    }

    @Override
    public void onSelectMusic(Musica musicaSelecionada) {
        musicaDownload = musicaSelecionada;
        musicaBaixar.setText(musicaDownload.getNome());
        presenter.baixaMusica(musicaDownload, context);
    }

    @Override
    public void onLongClickMusic(Musica musicaClicada) {

    }

    @Override
    public void showEmptyText() {
        semMusicas.setVisibility(View.VISIBLE);
        userAreaRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void hideEmptyText() {
        semMusicas.setVisibility(View.GONE);
        userAreaRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showMessage(String message) {
        showToastMessage(message);
    }

    private void showToastMessage(String message) {
        Snackbar.make(view.getRootView(), message, Snackbar.LENGTH_SHORT).show();
    }

    @OnClick(R.id.btnSelecionarMusica)
    public void showSelectMusicDialog(View view){
        selectMusicDialogFragment = SelectMusicDialogFragment.newInstance(0);
        //selectMusicDialogFragment.dismiss();
        selectMusicDialogFragment.show(getActivity().getFragmentManager(), "Dialog");
        //selectMusicDialogFragment.getArguments();
    }

    @OnClick(R.id.btnEnviarMusica)
    public void enviarMusica(View view){
        //recuperar usuario e senha - if
        uploadMusicFragment = UploadMusicFragment.newInstance(0/*, email.getText().toString(), senha.getText().toString()*/);
        uploadMusicFragment.show(getActivity().getFragmentManager(), "Dialog");
    }

    @OnClick(R.id.btnAtualizarMusica)
    public void atualizarMusica(View view){
        //recuperar usuario e senha - if
        uploadMusicFragment = UploadMusicFragment.newInstance(musicaUpload.getId()/*, email.getText().toString(), senha.getText().toString()*/);
        uploadMusicFragment.show(getActivity().getFragmentManager(), "Dialog");
    }

    @OnClick(R.id.btnPesquisar)
    public void pesquisarMusica(View view){
        //pesquisar no banco
//        List<Musica> availableMusics;
        if (pesquisarMusica.getText().toString().isEmpty()){
            showMessage(getString(R.string.informe_musica));
        } else {
            presenter.pesquisaMusica(pesquisarMusica.getText().toString(), context);
        }

        /*availableMusics = new ArrayList<>();
        showMusicas(availableMusics);*/
    }

    @Override
    public void showMusicas(List<Musica> musicas) {
        adapter.replaceData(musicas);
    }

    @OnClick(R.id.btnBaixarMusica)
    public void baixarMusica(View view){

    }



}

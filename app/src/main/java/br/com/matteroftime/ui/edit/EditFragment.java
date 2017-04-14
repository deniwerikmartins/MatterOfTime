package br.com.matteroftime.ui.edit;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import br.com.matteroftime.R;
import br.com.matteroftime.core.listeners.OnMusicSelectedListener;
import br.com.matteroftime.models.Compasso;
import br.com.matteroftime.models.Musica;
import br.com.matteroftime.ui.addMusic.AddMusicDialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditFragment extends Fragment implements EditContract.View, OnMusicSelectedListener{


    private View view;
    private EditAdapter adapter;
    private EditContract.Actions presenter;
    private AddMusicDialogFragment addMusicDialogFragment;
    private String[] valorNotas = new String[]{"Semibreve","Mímina","Seminima","Colcheia","Semicolcheia","Fusa","Semifusa"};
    private int nota;
    private String select;
    private boolean contagem;
    private Musica musica;

    @BindView(R.id.editList_recycler_view) RecyclerView editListRecyclerView;
    @BindView(R.id.empty_text) TextView emptyText;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.btnConfirmaCompasso) Button btnConfirmaCompasso;
    @BindView(R.id.btn_removerCompasso) Button btnRemoverCompasso;
    @BindView(R.id.btn_InserirCompasso) Button btnInserirCompasso;
    @BindView(R.id.spn_nota) Spinner spinner;
    @BindView(R.id.chk_pre_contagem) CheckBox checkBox;

    @BindView(R.id.txt_nome_musica) TextView nomeMusica;
    @BindView(R.id.txtNumeroMusica) TextView numeroMusica;
    @BindView(R.id.edt_ordem) EditText ordemDaMusica;
    @BindView(R.id.imgBtnConfirmaMusica) ImageButton confirmaMusica;
    @BindView(R.id.edt_contar) EditText contar;
    @BindView(R.id.txtNumCompasso) TextView txtNumeroCompasso;
    @BindView(R.id.txtTempoCompasso) TextView txtTempoCompasso;
    @BindView(R.id.txtNotaCompasso) TextView txtNotaCompasso;
    @BindView(R.id.txtBpmCompasso) TextView txtBpmCompasso;

    @BindView(R.id.edt_numero_compasso) EditText edtNumeroCompasso;
    @BindView(R.id.edt_bpm) EditText edtBpm;
    @BindView(R.id.edt_tempos) EditText edtTempos;
    @BindView(R.id.edt_repeticoes) EditText edtRepeticoes;





    public EditFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_edit, container, false);
        musica = new Musica();
        ButterKnife.bind(this, view);
        presenter = new EditPresenter(this);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onAddMusicButtonClicked();
            }
        });

        //Setup Recyclyerview
        List<Musica> tempMusicas = new ArrayList<>();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        adapter = new EditAdapter(tempMusicas, getContext(), this);
        editListRecyclerView.setLayoutManager(layoutManager);
        editListRecyclerView.setAdapter(adapter);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, valorNotas);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                select = (String)spinner.getSelectedItem();
                switch (select){
                    case "Semibreve":
                        nota = 1;
                        break;
                    case "Mímina":
                        nota = 2;
                        break;
                    case "Seminima":
                        nota = 4;
                        break;
                    case "Colcheia":
                        nota = 8;
                        break;
                    case "Semicolcheia":
                        nota = 16;
                        break;
                    case "Fusa":
                        nota = 32;
                        break;
                    case "Semifusa":
                        nota = 64;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        edtNumeroCompasso.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    int ord = Integer.parseInt(edtNumeroCompasso.getText().toString());
                    int x = 0;
                    if (musica.getNome() != null && ord <= 0 || ord > musica.getCompassos().size()){
                        showMessage(getString(R.string.compasso_inexistente));
                    } else if(musica.getCompassos() != null && musica.getCompassos().size() > 0){
                        ord = ord - 1;
                        x = musica.getCompassos().get(ord).getOrdem();
                        x++;
                        txtNumeroCompasso.setText(String.valueOf(x));

                        atualizaViewsCompasso(musica, musica.getCompassos().get(ord));

                    }
                }
            }
        });




        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.loadMusics();
    }

    @Override
    public void recebeMusica(Musica musica) {
        this.musica = musica;
    }

    @Override
    public void showMusics(List<Musica> musicas) {
        adapter.replaceData(musicas);
    }

    @Override
    public void showAddMusicForm() {
        addMusicDialogFragment = AddMusicDialogFragment.newInstance(0);
        addMusicDialogFragment.show(getActivity().getFragmentManager(), "Dialog");
    }

    @Override
    public void showEditMusicForm(Musica musica) {
        AddMusicDialogFragment dialog = AddMusicDialogFragment.newInstance(musica.getId());
        dialog.show(getActivity().getFragmentManager(), "Dialog");
    }

    @Override
    public void showDeleteMusicPrompt(final Musica musica) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View titleView = (View)inflater.inflate(R.layout.dialog_title,null);
        TextView titleText = (TextView) titleView.findViewById(R.id.txt_view_dialog_title);
        titleText.setText("Delete Music?");

        alertDialog.setMessage("Delete" + musica.getNome());
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                presenter.deleteMusic(musica);
                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();

    }

    @Override
    public void showEmptyText() {
        emptyText.setVisibility(View.VISIBLE);
        editListRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void hideEmptyText() {
        emptyText.setVisibility(View.GONE);
        editListRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showMessage(String message) {
        showToastMessage(message);
    }



    private void showToastMessage(String message) {
        Snackbar.make(view.getRootView(),message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onSelectMusic(Musica musicaSelecionada) {

        presenter.ondAddToEditButtonClicked(musicaSelecionada);
        nomeMusica.setText(musicaSelecionada.getNome());
        numeroMusica.setText(String.valueOf(musicaSelecionada.getOrdem() + 1));

    }

    @Override
    public void onLongClickMusic(Musica musicaClicada) {
        showMusicContextMenu(musicaClicada);

    }

    private void showMusicContextMenu(final Musica musicaClicada) {
        final String[] sortOptions = {"Edit Music", "Delete"};

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View convertView = (View) inflater.inflate(R.layout.dialog_list, null);
        alertDialog.setView(convertView);

        View titleView = (View) inflater.inflate(R.layout.dialog_title, null);
        TextView titleText = (TextView) titleView.findViewById(R.id.txt_view_dialog_title);
        if (musicaClicada.getNome() != null){
            titleText.setText(musicaClicada.getNome());
        }
        alertDialog.setCustomTitle(titleView);

        ListView dialogList = (ListView) convertView.findViewById(R.id.dialog_listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (getActivity(), android.R.layout.simple_list_item_1,sortOptions);
        dialogList.setAdapter(adapter);

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        final Dialog dialog = alertDialog.create();
        dialog.show();
        dialogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        presenter.onEditMusicaButtonClicked(musicaClicada);
                        dialog.dismiss();
                        break;
                    case 1:
                        presenter.onDeleteMusicButtonClicked(musicaClicada);
                        presenter.loadMusics();
                        dialog.dismiss();
                        break;
                }
            }
        });
    }

    @OnClick(R.id.chk_pre_contagem)
    public void onCheckboxClicked(View view){
        contagem = ((CheckBox) view).isChecked();
        if (contagem == false){
            contar.setActivated(false);
        } else {
            contar.setActivated(true);
        }

    }

    @OnClick(R.id.imgBtnConfirmaMusica)
    public void setConfirmaMusica(View view){
        int ord = Integer.parseInt(ordemDaMusica.getText().toString());
        if (ord < adapter.getItemCount() || ord > adapter.getItemCount()){
            showMessage(getString(R.string.posicao_invalida));
        } else if (musica.getNome() == null){
            showMessage(getString(R.string.sem_musica));
        } else if (ordemDaMusica.getText().toString().isEmpty()){
            showMessage(getString(R.string.ordem_necessaria));
        } else if(presenter.getListaMusicas() == null){
            showMessage(getString(R.string.sem_musicas));
        } else if(contagem == true && contar.getText().toString().isEmpty() || Integer.parseInt(contar.getText().toString()) == 0) {
            showMessage(getString(R.string.sem_contagem));
        } else if (contagem == true) {

            if (ord - 1 < 0){
                showMessage(getString(R.string.posicao_invalida));

            } else if (ord > presenter.getListaMusicas().size()){
                showMessage(getString(R.string.posicao_invalida));
            }
            else{
                musica.setOrdem(Integer.parseInt(ordemDaMusica.getText().toString()) - 1);
                musica.setPreContagem(contagem);
                musica.setTemposContagem(Integer.parseInt(contar.getText().toString()));
                atualizaViewsMusica(musica);
            }

        } else if (contagem == false){

            if (ord - 1 < 0){
                showMessage(getString(R.string.musica_inexistente));
            } else if (ord > presenter.getListaMusicas().size()){
                showMessage(getString(R.string.musica_inexistente));
            } else {
                musica.setOrdem(Integer.parseInt(ordemDaMusica.getText().toString()) - 1);
                musica.setPreContagem(contagem);
                atualizaViewsMusica(musica);
            }
        }
    }

    @Override
    public void atualizaViewsMusica(Musica musica) {
        List<Musica> musicas = presenter.getListaMusicas();
        musicas.set(musica.getOrdem(), musica);
        presenter.updateMusica(musica);
        this.showMusics(musicas);
    }



    @OnClick(R.id.btnConfirmaCompasso)
    public void setConfirmaCompasso(){
        int ord = Integer.parseInt(edtNumeroCompasso.getText().toString());
        if (musica.getNome() == null){
            showMessage(getString(R.string.sem_musica));
        } else if (edtNumeroCompasso.getText().toString().isEmpty()){
            showMessage(getString(R.string.sem_compasso));
        } else if (ord - 1 < 0){
            showMessage(getString(R.string.compasso_inexistente));
        } else if(ord > musica.getCompassos().size()){
            showMessage(getString(R.string.compasso_inexistente));
        } else {
            Compasso compasso = new Compasso();
            compasso.setOrdem(Integer.parseInt(edtNumeroCompasso.getText().toString()) - 1);
            compasso.setBpm(Integer.parseInt(edtBpm.getText().toString()));
            compasso.setTempos(Integer.parseInt(edtTempos.getText().toString()));
            compasso.setNota(nota);
            compasso.setRepeticoes(Integer.parseInt(edtRepeticoes.getText().toString()));

            RealmList<Compasso> compassos = musica.getCompassos();
            compassos.set(compasso.getOrdem(), compasso);
            musica.setCompassos(compassos);

            //musica.getCompassos().set(compasso.getOrdem(),compasso);


            presenter.updateMusica(musica);
            List<Musica> musicas = presenter.getListaMusicas();
            this.showMusics(musicas);
            atualizaViewsCompasso(musica, compasso);



        }
    }

    @Override
    public void atualizaViewsCompasso(Musica musica, Compasso compasso) {
        txtTempoCompasso.setText(String.valueOf(musica.getCompassos().get(compasso.getOrdem()).getTempos()));
        txtNotaCompasso.setText(String.valueOf(musica.getCompassos().get(compasso.getOrdem()).getNota()));
        txtBpmCompasso.setText(String.valueOf(musica.getCompassos().get(compasso.getOrdem()).getBpm()));
    }

    @OnClick(R.id.btn_removerCompasso)
    public void setBtnRemoverCompasso(){
        int ord = Integer.parseInt(edtNumeroCompasso.getText().toString());
        if (musica.getNome() == null) {
            showMessage(getString(R.string.sem_musica));
        } else if (ord - 1 < 0){
            showMessage(getString(R.string.compasso_inexistente));
        } else if(ord > musica.getCompassos().size()){
            showMessage(getString(R.string.compasso_inexistente));
        } else {
            musica.getCompassos().remove(ord - 1);
            presenter.updateMusica(musica);
            List<Musica> musicas = presenter.getListaMusicas();
            this.showMusics(musicas);
            //showMessage(getString(R.string.tamanho_compassos) + String.valueOf(musica.getCompassos().size()) + getString(R.string.compassos));
            Toast.makeText(getContext(), getString(R.string.tamanho_compassos) + " " + String.valueOf(musica.getCompassos().size()) + " " +getString(R.string.compassos), Toast.LENGTH_SHORT ).show();
        }

    }

    @OnClick(R.id.btn_InserirCompasso)
    public void setBtnInserirCompasso(){
        if(musica.getNome() == null){
            showMessage(getString(R.string.sem_musica));
        } else {
            musica.getCompassos().add(new Compasso());
            presenter.updateMusica(musica);
            List<Musica> musicas = presenter.getListaMusicas();
            this.showMusics(musicas);
            showMessage(getString(R.string.tamanho_compassos) + String.valueOf(musica.getCompassos().size()) + getString(R.string.compassos));
        }

    }




}

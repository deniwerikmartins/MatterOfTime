package br.com.matteroftime.ui.play;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.inject.Inject;

import br.com.matteroftime.R;
import br.com.matteroftime.common.DrawerLocker;
import br.com.matteroftime.common.MainActivity;
import br.com.matteroftime.core.events.MusicListChangedEvent;
import br.com.matteroftime.core.listeners.OnMusicSelectedListener;
import br.com.matteroftime.models.Compasso;
import br.com.matteroftime.models.Musica;
import br.com.matteroftime.util.Constants;
import br.com.matteroftime.util.EventBus;
import br.com.matteroftime.util.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static br.com.matteroftime.R.string.compasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayFragment extends Fragment implements PlayContract.View, OnMusicSelectedListener {

    private View view;
    private PlayAdapter playAdapter;
    private PlayContract.Actions presenter;
    private String[] valorNotas;
    private int nota;
    private String select;
    private Musica musica;
    private boolean tocando = false;
    Handler handler;

    @BindView(R.id.playlist_recycler_view) RecyclerView playlistRecyclerView;
    @BindView(R.id.edtTempos) EditText tempos;
    @BindView(R.id.spnNota) Spinner spinner;
    @BindView(R.id.edtBPM) EditText bpm;
    @BindView(R.id.txtBPMAtual) TextView bpmAtual;
    @BindView(R.id.txtTemposAtual) TextView temposAtual;
    @BindView(R.id.txtNotaAtual) TextView notaAtual;
    @BindView(R.id.txtProxBPM) TextView proxBpm;
    @BindView(R.id.txtProxTempos) TextView proxTempos;
    @BindView(R.id.txtProxNota) TextView proxNota;
    @BindView(R.id.btnOk) ImageButton imgBtnOk;
    @BindView(R.id.btnPlay) ImageButton imgBtnPlay;
    @BindView(R.id.btnStop) ImageButton imgBtnStop;
    @BindView(R.id.imgClick1) ImageView imgClick1;
    @BindView(R.id.imgClick2) ImageView imgClick2;
    @BindView(R.id.empty_text) TextView emptyText;
    @BindView(R.id.txtNomeMusica) TextView nomeMusica;
    @BindView(R.id.txtRepeticoesAtual) TextView repeticoesAtual;
    @BindView(R.id.txtRepeticoesTotal) TextView repeticoesTotal;
    @BindView(R.id.txtRepeticoesProximo) TextView repeticoesProximo;
    @BindView(R.id.txtRepeticoesCompassoAtual) TextView repeticoesCompassoAtual;
    @BindView(R.id.txtRepeticoesCompassoTotal) TextView repeticoesCompassoTotal;
    @Inject Bus bus = new Bus();

    public PlayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_play, container, false);
        valorNotas = new String[]{getString(R.string.semibreve),getString(R.string.minima),getString(R.string.seminima),getString(R.string.colcheia),getString(R.string.semicolcheia),getString(R.string.fusa),getString(R.string.semifusa)};
        ButterKnife.bind(this, view);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                //super.handleMessage(msg);
                Bundle data = msg.getData();
                switch (msg.what){
                    case 0:
                        if (imgClick1.getVisibility() == View.VISIBLE){
                            imgClick1.setVisibility(View.INVISIBLE);
                        }else{
                            imgClick1.setVisibility(View.VISIBLE);
                        }
                        if (imgClick1.getVisibility() == View.VISIBLE){
                            imgClick2.setVisibility(View.INVISIBLE);
                        } else {
                            imgClick2.setVisibility(View.VISIBLE);
                        }
                        String compassoAtualBPM = String.valueOf(data.getInt(Constants.COMPASSO_ATUAL_BPM));
                        String compassoAtualTempos = String.valueOf(data.getInt(Constants.COMPASSO_ATUAL_TEMPOS));
                        String compassoAtualNota = String.valueOf(data.getInt(Constants.COMPASSO_ATUAL_NOTA));
                        String compassoAtualTempoAtual = String.valueOf(data.getInt(Constants.COMPASSO_ATUAL_TEMPO_ATUAL));
                        String compassoAtualTempoTotal = String.valueOf(data.getInt(Constants.COMPASSO_ATUAL_TEMPO_TOTAL));

                        bpmAtual.setText(compassoAtualBPM);
                        temposAtual.setText(compassoAtualTempos);
                        notaAtual.setText(compassoAtualNota);
                        repeticoesAtual.setText(compassoAtualTempoAtual);
                        repeticoesTotal.setText(compassoAtualTempoTotal);
                        break;
                    case 1:
                        if (musica.getCompassos().size() > 1){
                            String compassoProximoBPM = String.valueOf(data.getInt(Constants.COMPASSO_PROXIMO_BPM));
                            String compassoProximoTempos = String.valueOf(data.getInt(Constants.COMPASSO_PROXIMO_TEMPOS));
                            String compassoProximoNota = String.valueOf(data.getInt(Constants.COMPASSO_PROXIMO_NOTA));
                            String compassoProximoRepeticoes = String.valueOf(data.getInt(Constants.COMPASSO_PROXIMO_REPETICOES));

                            proxBpm.setText(compassoProximoBPM);
                            proxTempos.setText(compassoProximoTempos);
                            proxNota.setText(compassoProximoNota);
                            repeticoesProximo.setText(compassoProximoRepeticoes);
                        }
                        break;
                    case 2:
                        String compassoRepeticoesAtual = String.valueOf(data.getInt(Constants.COMPASSO_REPETICOES_ATUAL));
                        String compassoRepeticoesTotal = String.valueOf(data.getInt(Constants.COMPASSO_REPETICOES_TOTAL));

                        repeticoesCompassoAtual.setText(compassoRepeticoesAtual);
                        repeticoesCompassoTotal.setText(compassoRepeticoesTotal);
                        break;
                    case 3:
                        bpmAtual.setText(String.valueOf(msg.arg2));
                        temposAtual.setText(String.valueOf(msg.arg1));
                        notaAtual.setText(String.valueOf(msg.arg1));
                        repeticoesAtual.setText(String.valueOf(msg.arg2));
                        repeticoesTotal.setText(String.valueOf(msg.arg2));
                        repeticoesCompassoAtual.setText(String.valueOf(msg.arg2));
                        repeticoesCompassoTotal.setText(String.valueOf(msg.arg2));
                        proxBpm.setText(String.valueOf(msg.arg2));
                        proxTempos.setText(String.valueOf(msg.arg1));
                        proxNota.setText(String.valueOf(msg.arg1));
                        repeticoesProximo.setText(String.valueOf(msg.arg2));
                        imgClick1.setVisibility(View.INVISIBLE);
                        imgClick2.setVisibility(View.INVISIBLE);
                        ((DrawerLocker) getActivity()).setDrawerEnabled(true);
                        ((MainActivity) getActivity()).habilitaViewPager();
                        ((MainActivity) getActivity()).habilitaTabLayout();
                        tocando = false;
                        break;
                    case 4:
                        repeticoesAtual.setText(String.valueOf(msg.arg1));
                        repeticoesTotal.setText(String.valueOf(msg.arg2));
                        break;
                    case 5:
                        if (imgClick1.getVisibility() == View.VISIBLE){
                            imgClick1.setVisibility(View.INVISIBLE);
                        }else{
                            imgClick1.setVisibility(View.VISIBLE);
                        }
                        if (imgClick1.getVisibility() == View.VISIBLE){
                            imgClick2.setVisibility(View.INVISIBLE);
                        } else {
                            imgClick2.setVisibility(View.VISIBLE);
                        }
                        String compassoAtualBPMCompasso = String.valueOf(data.getInt(Constants.COMPASSO_ATUAL_BPM));
                        String compassoAtualTempoTotalCompasso = String.valueOf(data.getInt(Constants.COMPASSO_ATUAL_TEMPO_TOTAL));
                        String compassoAtualNotaCompasso = String.valueOf(data.getInt(Constants.COMPASSO_ATUAL_NOTA));
                        String compassoAtualTempoAtualCompasso = String.valueOf(data.getInt(Constants.COMPASSO_ATUAL_TEMPO_ATUAL));

                        bpmAtual.setText(compassoAtualBPMCompasso);
                        temposAtual.setText(compassoAtualTempoTotalCompasso);
                        notaAtual.setText(compassoAtualNotaCompasso);
                        repeticoesAtual.setText(compassoAtualTempoAtualCompasso);
                        repeticoesTotal.setText(compassoAtualTempoTotalCompasso);
                        break;
                }
                //repeticoesAtual.setText(String.valueOf(msg.arg1));
            }
        };


        presenter = new PlayPresenter(this);
        bus.register(this);

        //setup RecyclerView
        List<Musica> tempMusicas = new ArrayList<>(); // ou RealmList
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        playAdapter = new PlayAdapter(tempMusicas, getContext(), this);
        playlistRecyclerView.setLayoutManager(layoutManager);
        playlistRecyclerView.setAdapter(playAdapter);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, valorNotas);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner = (Spinner) spinner.findViewById(R.id.spn_notas);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //nota = (int)spinner.getSelectedItem();
                select = (String)spinner.getSelectedItem();
                int selectedItemPosition = spinner.getSelectedItemPosition();
                switch (selectedItemPosition){
                    case 0:
                        nota = 1;
                        break;
                    case 1:
                        nota = 2;
                        break;
                    case 2:
                        nota = 4;
                        break;
                    case 3:
                        nota = 8;
                        break;
                    case 4:
                        nota = 16;
                        break;
                    case 5:
                        nota = 32;
                        break;
                    case 6:
                        nota = 64;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tempos.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    Utils.hideKeyboard(getActivity().getBaseContext(), tempos);
                }
            }
        });

        bpm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    Utils.hideKeyboard(getActivity().getBaseContext(), bpm);
                }
            }
        });

        return view;
    }

    @Override
    public void mostrarMusicas(List<Musica> musicas) {
        playAdapter.replaceData(musicas);
    }

    @Override
    public void showEmptyText() {
        emptyText.setVisibility(View.VISIBLE);
        playlistRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void hideEmptyText() {
        emptyText.setVisibility(View.GONE);
        playlistRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showMessage(String message) {
        showToastMessage(message);
    }

    private void showToastMessage(String message) {
        //Snackbar.make(view.getRootView(),message, Snackbar.LENGTH_SHORT).show();
        Toast toast = Toast.makeText(getActivity().getBaseContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    @Override
    public void onSelectMusic(Musica musicaSelecionada) {


        boolean ok = false;

        for (int i = 0; i < musicaSelecionada.getCompassos().size(); i++){
            if (musicaSelecionada.getCompassos().get(i).getTempos() == 0){
                showMessage(getString(R.string.o_compasso) + " " +String.valueOf(i+1)+ " " + getString(R.string.compassos_nao_definidos));
                ok = false;
                return;
            } else if(musicaSelecionada.getCompassos().get(i).getRepeticoes() == 0){
                showMessage(getString(R.string.o_compasso) + " " + String.valueOf(i+1)+ " "  + getString(R.string.repeticoes_nao_definidos));
                ok = false;
                return;
            }  else if(musicaSelecionada.getCompassos().get(i).getBpm() == 0){
                showMessage(getString(R.string.o_compasso) + " " +String.valueOf(i+1)+ " "  + getString(R.string.bpm_nao_definido));
                ok = false;
                return;
            } else if(musicaSelecionada.getCompassos().get(i).getOrdem() < 0) {
                showMessage(getString(R.string.o_compasso) + " " +String.valueOf(i+1)+ " "  + getString(R.string.ordem_nao_definida));
                ok = false;
                return;
            } else {
                ok = true;
            }
        }

        if (ok == true){
            musica = musicaSelecionada;
            presenter.defineMusica(musica);
            nomeMusica.setText(musica.getNome());
            repeticoesTotal.setText(String.valueOf(musica.getCompassos().get(0).getRepeticoes()));

            ListIterator listIterator = musica.getCompassos().listIterator();

            if (listIterator.hasNext()){
                listIterator.next();
                if (listIterator.hasNext()){
                    Compasso compasso = (Compasso) listIterator.next();
                    repeticoesProximo.setText(String.valueOf(compasso.getRepeticoes()));
                }
            }
            atualizaViewsMusica(0);
        }
    }

    @Override
    public void onLongClickMusic(Musica musicaClicada) {
        onSelectMusic(musicaClicada);
    }

    @OnClick(R.id.btnOk)
    public void onClickOk(View view){
        Utils.hideKeyboard(getActivity().getBaseContext(), view);
        if (tempos.getText().toString().isEmpty()){
            tempos.setError(getString(R.string.obrigatorio));
            tempos.requestFocus();
            this.showMessage(getString(R.string.tempo_necessario));
        } else if (bpm.getText().toString().isEmpty()){
            bpm.setError(getString(R.string.obrigatorio));
            bpm.requestFocus();
            this.showMessage(getString(R.string.bpm_necessario));
        } else if (!bpm.getText().toString().isEmpty()){
            int val = Integer.parseInt(bpm.getText().toString());
            if (val < 30 || val > 250){
                showMessage(getString(R.string.valor_incompativel));
            } else {
                int t = Integer.parseInt(tempos.getText().toString());
                int b = Integer.parseInt(bpm.getText().toString());
                presenter.criaCompasso(b, t, nota);
                bpmAtual.setText(String.valueOf(b));
                temposAtual.setText(String.valueOf(t));
                notaAtual.setText(String.valueOf(nota));
                nomeMusica.setText(getString(compasso));
                bus.post(new MusicListChangedEvent());
                showMessage(getString(R.string.compasso_confirmado));
            }
        }


    }

    @OnClick(R.id.btnPlay)
    public void tocar(View view){

        if (tocando == false ){
            ((DrawerLocker) getActivity()).setDrawerEnabled(false);
            ((MainActivity) getActivity()).desabilitaViewPager();
            ((MainActivity) getActivity()).desabilitaTabLayout();
            tocando = true;
            presenter.play(getContext(), handler);
        } else {
            return;
        }
    }


    @OnClick(R.id.btnStop)
    public void parar(View view){
        if (tocando == true){
            ((DrawerLocker) getActivity()).setDrawerEnabled(true);
            ((MainActivity) getActivity()).habilitaViewPager();
            ((MainActivity) getActivity()).habilitaTabLayout();
            presenter.stop();
            repeticoesCompassoAtual.setText(String.valueOf(000));
            repeticoesCompassoTotal.setText(String.valueOf(000));
            tocando = false;
        } else {
            return;
        }

    }

    @Override
    public void atualizaViewsMusica(int i) {
        i = 0;
        if (musica.getCompassos().size() > 0){

            bpmAtual.setText(String.valueOf(musica.getCompassos().get(i).getBpm()));
            temposAtual.setText(String.valueOf(musica.getCompassos().get(i).getTempos()));
            notaAtual.setText(String.valueOf(musica.getCompassos().get(i).getNota()));

            if (musica.getCompassos().size() > 1){
                proxBpm.setText(String.valueOf(musica.getCompassos().get(i+1).getBpm()));
                proxTempos.setText(String.valueOf(musica.getCompassos().get(i+1).getTempos()));
                proxNota.setText(String.valueOf(musica.getCompassos().get(i+1).getNota()));
            }
            bus.post(new MusicListChangedEvent());
        }

    }

    @Override
    public void onStart() {
        //showMessage("on start");
        super.onStart();
        EventBus.getInstance().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getInstance().unregister(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        //showMessage("resume");
        presenter.loadMusics();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        presenter.stop();

    }

    /*
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.setClassLoader(ClassLoader.getSystemClassLoader());
        outState.putAll(outState);
        showMessage("save instance state");
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showMessage("activity created");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showMessage("on create");
    }


    @Override
    public void onPause() {
        super.onPause();
        showMessage("pause");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        showMessage("on Attach context");

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        showMessage("on attach activity");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        showMessage("destroy");
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        showMessage("destroy view");
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        showMessage("destroy options menu");
    }



    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        showMessage("inflate");
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showMessage("view created");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        showMessage("view state restored");
    }

    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        showMessage("on inflate");
    }
    */


    @Subscribe
    public void onMusicListChanged(MusicListChangedEvent event){
        presenter.loadMusics();

    }
}

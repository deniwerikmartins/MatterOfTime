package br.com.matteroftime.ui.userArea;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import br.com.matteroftime.R;
import br.com.matteroftime.core.listeners.OnMusicSelectedListener;
import br.com.matteroftime.models.Musica;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by RedBlood on 20/04/2017.
 */

public class UserAreaAdapter extends RecyclerView.Adapter<UserAreaAdapter.ViewHolder>{

    private List<Musica> musicas;
    private Context context;
    private final OnMusicSelectedListener listener;

    public UserAreaAdapter(List<Musica> musicas, Context context, OnMusicSelectedListener listener) {
        this.musicas = musicas;
        this.context = context;
        this.listener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_musicas_list,parent,false);
        ViewHolder viewHolder = new ViewHolder(rowView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (musicas != null){
            try {
                final Musica musica = musicas.get(position);
                holder.numeroMusica.setText(String.valueOf(musica.getOrdem() + 1));
                holder.nomeMusica.setText(musica.getNome());
                holder.bpm.setText(String.valueOf(musica.getCompassos().get(0).getBpm()));
                holder.tempos.setText(String.valueOf(musica.getCompassos().get(0).getTempos()));
                holder.nota.setText(String.valueOf(musica.getCompassos().get(0).getNota()));
                holder.totalCompassos.setText(String.valueOf(musica.getCompassos().size()));
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        if (musicas != null){
            return musicas.size();
        } else {
            return 0;
        }
    }

    public void replaceData(List<Musica> musicas) {
        this.musicas = musicas;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        @BindView(R.id.txt_numero_musica) TextView numeroMusica;
        @BindView(R.id.txt_nome_musica) TextView nomeMusica;
        @BindView(R.id.txt_BPM) TextView bpm;
        @BindView(R.id.txt_tempos) TextView tempos;
        @BindView(R.id.txt_nota) TextView nota;
        @BindView(R.id.txtTotalCompassos) TextView totalCompassos;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Musica musicaSelecionada = musicas.get(getLayoutPosition());
            listener.onSelectMusic(musicaSelecionada);
        }
    }
}
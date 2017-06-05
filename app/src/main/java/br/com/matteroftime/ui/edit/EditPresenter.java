package br.com.matteroftime.ui.edit;

import android.content.Context;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import javax.inject.Inject;

import br.com.matteroftime.core.MatterOfTimeApplication;
import br.com.matteroftime.core.events.MusicListChangedEvent;
import br.com.matteroftime.core.listeners.OnDatabaseOperationCompleteListener;
import br.com.matteroftime.models.Compasso;
import br.com.matteroftime.models.Musica;

/**
 * Created by RedBlood on 30/03/2017.
 */

public class EditPresenter implements EditContract.Actions, OnDatabaseOperationCompleteListener{

    private final EditContract.View view;
    @Inject EditContract.Repository repository;
    @Inject
    Bus bus;

    public EditPresenter(EditContract.View view) {
        this.view = view;
        MatterOfTimeApplication.getInstance().getAppComponent().inject(this);
        bus.register(this);
    }

    @Override
    public void loadMusics() {
        List<Musica> availableMusics = repository.getAllMusics();
        if (availableMusics != null && availableMusics.size() > 0){
            view.hideEmptyText();
            view.showMusics(availableMusics);
        } else {
            view.resetaNome();
            view.showEmptyText();
        }
    }

    @Override
    public List<Musica> getListaMusicas() {
        List<Musica> musicas = repository.getAllMusics();
        return musicas;
    }

    @Override
    public void onAddMusicButtonClicked() {
        view.showAddMusicForm();
    }

    @Override
    public void ondAddToEditButtonClicked(Musica musica) {
        view.recebeMusica(musica);
    }

    @Override
    public Musica getMusica(long id) {
        return repository.getMusicById(id);
    }

    @Override
    public void addMusic(Musica musica, Context context) {
        repository.addMusic(musica, this, context);
        loadMusics();
    }

    @Override
    public void onDeleteMusicButtonClicked(Musica musica) {
        view.showDeleteMusicPrompt(musica);
    }

    @Override
    public void deleteMusic(Musica musica, Context context) {
        repository.deleteMusic(musica,this, context);
        loadMusics();

    }

    @Override
    public void onEditMusicaButtonClicked(Musica musica) {
        view.showEditMusicForm(musica);
    }

    @Override
    public void updateMusica(Musica musica, Context context) {
        repository.updateMusic(musica,this, context);
        //repository.atualizaMusica(musica);
        loadMusics();
    }

    @Override
    public void atualizaMusica(Musica musica) {
        repository.atualizaMusica(musica);
        loadMusics();
    }

    @Override
    public void atualizarCompassodaMusica(Musica musica, Compasso compasso, Context context) {
        repository.atualizaCompasso(musica, this ,compasso, context);
        loadMusics();
    }

    @Subscribe
    public void onMusicListChanged(MusicListChangedEvent event){
        if(repository.getAllMusics().size() > 0){
            loadMusics();
        }

    }


    @Override
    public void onSQLOperationFailed(String error) {
        view.showMessage(error);
    }

    @Override
    public void onSQLOperationSucceded(String message) {
        view.showMessage(message);
        loadMusics();
        bus.post(new MusicListChangedEvent());
    }
}

package br.com.matteroftime.data.realm;

import android.content.Context;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import br.com.matteroftime.R;
import br.com.matteroftime.core.listeners.OnDatabaseOperationCompleteListener;
import br.com.matteroftime.models.Musica;
import br.com.matteroftime.ui.uploadMusic.UploadMusicContract;
import br.com.matteroftime.util.Constants;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by RedBlood on 23/04/2017.
 */

public class UploadMusicRepository implements UploadMusicContract.Repository{
    @Override
    public Musica getMusicById(long id) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Musica> musicas = realm.where(Musica.class).equalTo("id", id).findAll();
        Musica result = musicas.first();
        Musica inMemoryMusic = realm.copyFromRealm(result);
        realm.close();
        return inMemoryMusic;
    }

    @Override
    public void salvaMusica(Musica musica, final Context context, final OnDatabaseOperationCompleteListener listener, final String email, final String senha) {
        final File file = new File("data/data/br.com.matteroftime/"+musica.getNome()+"_music.met");
        final String nome = musica.getNome();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(Constants.EMAIL, email);
        jsonObject.addProperty(Constants.SENHA, senha);
        try{
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(musica);
            objectOutputStream.flush();
            objectOutputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();

            Ion.with(context)
                    .load("http://matteroftime.com.br/login.php")
                    .setJsonObjectBody(jsonObject)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (result.get("result").getAsString().equals("NO")){
                                listener.onSQLOperationFailed(context.getString(R.string.erro_login));
                            } else {
                                File echoedFile = context.getFileStreamPath("echo");
                                Future<File> uploading;
                                uploading = Ion.with(context)
                                        .load("http://matteroftime.com.br/inserir.php")
                                        .setMultipartParameter("nome", nome)
                                        .setMultipartFile("archive", file)
                                        .write(echoedFile)
                                        .setCallback(new FutureCallback<File>() {
                                            @Override
                                            public void onCompleted(Exception e, File result) {
                                                if (e != null){
                                                    listener.onSQLOperationFailed(context.getString(R.string.erro_envio));
                                                } else {
                                                    listener.onSQLOperationSucceded(context.getString(R.string.sucesso_envio));
                                                }
                                            }
                                        });
                                uploading = null;
                            }
                        }
                    });
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        file.delete();
    }

    @Override
    public void atualizaMusica(Musica musica, final Context context, final OnDatabaseOperationCompleteListener listener, final String email, final String senha) {
        final File file = new File("data/data/br.com.matteroftime/"+musica.getNome()+"_music.met");
        final String nome = musica.getNome();
        final long id = musica.getId();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(Constants.ID_MUSICA, musica.getId());
        jsonObject.addProperty(Constants.EMAIL, email);
        jsonObject.addProperty(Constants.SENHA, senha);

        try{
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(musica);
            objectOutputStream.flush();
            objectOutputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();

            Ion.with(context)
                    .load("http://matteroftime.com.br/login.php")
                    .setJsonObjectBody(jsonObject)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (result.get("result").getAsString().equals("NO")){
                                listener.onSQLOperationFailed(context.getString(R.string.erro_envio));
                            } else {
                                File echoedFile = context.getFileStreamPath("echo");
                                Future<File> uploading;
                                uploading = Ion.with(context)
                                        .load("http://matteroftime.com.br/inserir.php")
                                        .setMultipartParameter("nome", nome)
                                        .setMultipartParameter(Constants.ID_MUSICA, String.valueOf(id))
                                        .setMultipartFile("archive", file)
                                        .write(echoedFile)
                                        .setCallback(new FutureCallback<File>() {
                                            @Override
                                            public void onCompleted(Exception e, File result) {
                                                if (e != null){
                                                    listener.onSQLOperationFailed(context.getString(R.string.erro_envio));
                                                } else {
                                                    listener.onSQLOperationSucceded(context.getString(R.string.sucesso_envio));
                                                }
                                            }
                                        });
                                uploading = null;
                            }
                        }
                    });
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        file.delete();
    }
}
package es.schooleando.xkcdcomic;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by angel on 11/01/2017.
 */
public class DownloadIntentService extends IntentService {

    private static final String TAG = DownloadIntentService.class.getSimpleName();
    private ResultReceiver mReceiver;
    private HttpURLConnection conexion = null;
    private Bundle b;
    private Bitmap bmp = null;
    public static int ultimoNum = -1;


    // definimos tipos de mensajes que utilizaremos en ResultReceiver
    public static final int PROGRESS = 0;
    public static final int FINSISHED = 1;
    public static final int ERROR = 2;

    public DownloadIntentService() {
        super("DownloadIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        b = intent.getExtras();
        mReceiver = intent.getParcelableExtra("receiver");
        String urlJson = intent.getStringExtra("url");
        mReceiver.send(PROGRESS,b);

        // TODO Aquí hacemos la conexión y accedemos a la imagen.
        try{
            URL urlObject = new URL(urlJson);           // TODO url del json
            conexion = (HttpURLConnection) urlObject.openConnection();
            conexion.setRequestMethod("HEAD");          //TODO Peticion de cabecera
            conexion.connect();         ////TODO 1ª conexion

            if(conexion.getResponseCode() == 200) {//TODO Conexion correcta, existe el recurso

                String urlImagen = descargarJSON(urlObject);//TODO Extraemos la url de la imagen del JSON

                if(urlImagen != null){                      //TODO Cadena que contiene la url de la imagen
                    Bitmap imgBits = descargarImagenBitmap(urlImagen);  //TODO obtenemos el bitmap de la imagen
                    if(imgBits != null) {
                        b.putParcelable("img", imgBits);
                        b.putInt("num", ultimoNum);
                        mReceiver.send(FINSISHED, b);
                    }
                }else{                  //TODO Error al acceder "img" del JSON, devolvemos codigo de respuesta
                    b.putString("error","No se pudo acceder a la imagen.");
                    mReceiver.send(ERROR,b);//Error
                }

            }
            else {      //TODO Error al solicitar el recurso devolvemos codigo de respuesta
                b.putString("error",String.valueOf(conexion.getResponseCode()));
                mReceiver.send(ERROR,b);//Error
            }
        }catch(MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //TODO Para descargar el recurso JSON.
    public String descargarJSON(URL urlObject){
        String cadena = "";
        InputStream in = null;
        BufferedInputStream leer = null;

        try {
            in = urlObject.openStream();        //TODO Accedemos al flujo de datos
            leer = new BufferedInputStream(in);
            int n;
            while((n=leer.read())>0){
                cadena += (char)n;      //TODO Cadena con el JSON
            }
            String url = extraerUrlImagen(cadena);    //TODO Procesamos el JSON para extraer la url de la imagen
            return url;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String extraerUrlImagen(String cadena){        //TODO Procesamos el JSON para extraer la url de la imagen
        String urlJson = "";
        try {
            JSONObject json = new JSONObject(cadena);
            urlJson = json.getString("img");
            if(ultimoNum == -1){    //TODO si el numero es -1 es que no tenemos el num del último comic
                extraerNum(cadena); //TODO capturamos el num del último comic
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return urlJson;
    }

    public void extraerNum(String cadena){  //TODO Extraer el número del último comic

        try {
            JSONObject json = new JSONObject(cadena);
            ultimoNum = json.getInt("num"); //TODO asignamos a la var estática el num de último comic
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Bitmap descargarImagenBitmap(String urlImagen){
        URL urlObject = null;
        try{
            urlObject = new URL(urlImagen);     //TODO url del json
            InputStream input = urlObject.openStream();     //TODO 2ª conexión, descargar la imagen
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int n;
            byte[] buf = new byte[1024];
            while ((n = input.read(buf)) > 0){
                bos.write(buf,0,n);
            }
            input.close();//TODO Cerramos flujo de datos
            bos.close();

            byte[] byteImg = bos.toByteArray();
            bmp = BitmapFactory.decodeByteArray(byteImg, 0, byteImg.length);

            return bmp; //TODO bmp contiene la imagen

        }catch(MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp;    //TODO bmp = null no se descargo nada
    }

}

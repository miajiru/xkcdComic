package es.schooleando.xkcdcomic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import static es.schooleando.xkcdcomic.DownloadIntentService.ultimoNum;

public class ComicActivity extends AppCompatActivity implements BgResultReceiver.Receiver {

    private BgResultReceiver mResultReceiver;
    private ProgressBar progreso;
    private ImageView imagen;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic);

        progreso = (ProgressBar) findViewById(R.id.progressBar);
        imagen = (ImageView) findViewById(R.id.imageView);

        progreso.setVisibility(View.VISIBLE);       //TODO Se inicia cuando empieza a descargar el comic mas reciente

        //TODO creamos el BgResultReceiver
        mResultReceiver = new BgResultReceiver(new Handler());
        mResultReceiver.setReceiver(this);

        //TODO Esto es gratis: al arrancar debemos cargar el cómic actual
        intent = new Intent(this, DownloadIntentService.class);
        intent.putExtra("url", "http://xkcd.com/info.0.json");
        intent.putExtra("receiver", mResultReceiver);
        startService(intent);

        //TODO Callback de ImageView para hacer click en la imagen y que se descargue otro comic aleatorio.
        imagen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int numAleatorio = (int) (Math.random()*ultimoNum)+1;       //TODO Número aleatorio para el nuevo comic
                intent = new Intent(getApplicationContext(), DownloadIntentService.class);
                intent.putExtra("url", "http://xkcd.com/"+numAleatorio+"/info.0.json");     //TODO Introducimos en la url el num del comic solicitado
                intent.putExtra("receiver", mResultReceiver);
                startService(intent);
            }
        });
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        // TODO: podemos recibir diferentes resultCodes del IntentService
        switch (resultCode) {
            case 0:             //TODO PROGRESS -> descargando la imagen (ProgressBar)
                progreso.setVisibility(View.VISIBLE);
                break;
            case 1:             //TODO OK -> nos hemos descargado la imagen correctamente. (ImageView)
                Bitmap imgBits = (Bitmap) resultData.get("img");
                ultimoNum = resultData.getInt("num");
                imagen.setImageBitmap(imgBits);             //TODO Colocamos la imagen descargada en el imageView
                progreso.setVisibility(View.INVISIBLE);     //TODO Finaliza la descarga y se oculta
                break;

            case 2:             //TODO ERROR -> ha habido un problema
                Toast.makeText(this, resultData.getString("error"), Toast.LENGTH_SHORT).show();
                break;
        }

    }
}

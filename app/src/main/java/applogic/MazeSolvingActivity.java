package applogic;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.trist.ambages10.R;

import applogic.Maze;
import applogic.MazePoint;

/**
 * Created on 8/24/2016.
 */

//TODO: Document and test! This app is just some untested and undocumented fun so far.

public class MazeSolvingActivity extends Activity {

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Fields //////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    //Maze Related
    private List<MazePoint> startAndEnd = new ArrayList<>();

    //Information For The User Related
    private ProgressBar progressBar;
    private TextView message;
    private SeekBar seekBar;
    private Button pictureButton;
    private Button solveButton;

    //Image Related
    private static final String BITMAP_STORAGE_KEY = "viewbitmap";
    private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";

    private Integer resolution = 65/2;
    private ImageView imageView;

    private Bitmap imageBitmap;
    private Bitmap lastSolved;

    private String currentPhotoPath;
    private String lastPhotoPath;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Listener Call Related ///////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void setPicture(String path){

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = resolution/2;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);

        imageBitmap = bitmap;
        imageView.setImageBitmap(bitmap);
        imageView.setVisibility(View.VISIBLE);
        lastPhotoPath = path;

    }

    private void solve(){

        message.setVisibility(View.GONE);
        try {
            double stepSize = .2;
            double impassability = 0;
            Maze maze = new Maze(imageBitmap, impassability, startAndEnd.get(0), startAndEnd.get(1));

            List<MazePoint> path;
            List<MazePoint> previousPath = new ArrayList<>();
            while((path = maze.getSolution()) != null) {
                previousPath = path;
                impassability+= stepSize;
                maze = new Maze(imageBitmap, impassability, startAndEnd.get(0), startAndEnd.get(1));
            }

            lastSolved = Maze.drawSolution(imageBitmap, previousPath);

        }catch(InterruptedException e){
            e.printStackTrace();
        }

    }

    private MazePoint convertEventLocationToMazePoint(MotionEvent e){

        double imageWidth = imageView.getWidth();
        double imageHeight = imageView.getHeight();

        double bitmapWidth = imageBitmap.getWidth();
        double bitmapHeight = imageBitmap.getHeight();

        double xTransform = bitmapWidth/imageWidth;
        double yTransform = bitmapHeight/imageHeight;

        return new MazePoint((int)(e.getX()*xTransform), (int)(e.getY()*yTransform));


    }

    private boolean setCoordinates(MotionEvent e){

        if(imageBitmap != null) {
            MazePoint coordinate = convertEventLocationToMazePoint(e);

            Log.d("list: ", startAndEnd.toString());
            if (startAndEnd.size() >= 2) {
                startAndEnd.remove(0);
                startAndEnd.add(coordinate);
            } else {
                startAndEnd.add(coordinate);
            }

            Log.d("list: ", startAndEnd.toString());

            Bitmap copy = imageBitmap.copy(imageBitmap.getConfig(), true);

            Canvas canvas = new Canvas(copy);
            Paint paint = new Paint();
            paint.setColor(Color.rgb(234, 78, 78));
            canvas.drawBitmap(copy, new Matrix(), null);

            MazePoint mazeStart = null;
            MazePoint mazeEnd = null;

            if (startAndEnd.size() > 0){
                mazeStart = startAndEnd.get(0);
            }
            if (startAndEnd.size() > 1){
                mazeEnd = startAndEnd.get(1);
            }

            if(mazeStart != null){
                Log.d("Set: ", "start: " + mazeStart.toString());
                canvas.drawCircle(mazeStart.getX(), mazeStart.getY(), 7, paint);

            }
            if(mazeEnd != null){
                Log.d("Set: ", "end: " + mazeEnd.toString());
                canvas.drawCircle(mazeEnd.getX(), mazeEnd.getY(), 7, paint);
            }

            imageView.setImageBitmap(copy);
            imageView.setVisibility(View.VISIBLE);
        }

        return true;
    }

    private void galleryAddPicture() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void dispatchTakePictureIntent() {

        startAndEnd.clear();

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            File f = setUpPhotoFile();
            currentPhotoPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            currentPhotoPath = null;
        }


        startActivityForResult(takePictureIntent, 1);
    }

    private void handleCamera(){

        if (currentPhotoPath != null) {
            setPicture(currentPhotoPath);
            galleryAddPicture();
            currentPhotoPath = null;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            handleCamera();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Life Cycle Callbacks ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BITMAP_STORAGE_KEY, imageBitmap);
        outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (imageBitmap != null) );
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        imageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
        imageView.setImageBitmap(imageBitmap);
        imageView.setVisibility(
                savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ?
                        ImageView.VISIBLE : ImageView.INVISIBLE
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Initialization //////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        imageView = (ImageView) findViewById(R.id.imageView1);
        imageView.setOnTouchListener(setCoordinateOnTouchistener);
        imageBitmap = null;

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(resolutionOnChangeListener);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        message = (TextView) findViewById(R.id.textView2);
        message.setVisibility(View.GONE);

        solveButton = (Button) findViewById(R.id.solve);
        solveButton.setOnClickListener(solveOnClickListener);

        pictureButton = (Button) findViewById(R.id.btnIntend);
        setButtonListenerOrDisable(
                pictureButton,
                takePictureOnClickListener,
                MediaStore.ACTION_IMAGE_CAPTURE
        );

    }

    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void setButtonListenerOrDisable(
            Button btn,
            Button.OnClickListener onClickListener,
            String intentName
    ) {
        if (isIntentAvailable(this, intentName)) {
            btn.setOnClickListener(onClickListener);
        } else {
            btn.setClickable(false);
        }
    }

    private void setAllElementsEnabled(boolean enabled){
        imageView.setEnabled(enabled);
        seekBar.setEnabled(enabled);
        progressBar.setEnabled(enabled);
        message.setEnabled(enabled);
        solveButton.setEnabled(enabled);
        pictureButton.setEnabled(enabled);
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Listeners ///////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ImageView.OnTouchListener setCoordinateOnTouchistener =
            new ImageView.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent e) {
                    return e.getAction() != MotionEvent.ACTION_DOWN || setCoordinates(e);
                }
            };

    Button.OnClickListener takePictureOnClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent();
                }
            };

    Button.OnClickListener solveOnClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (imageBitmap != null && startAndEnd.size() == 2) {

                        progressBar.setVisibility(View.VISIBLE);

                        setAllElementsEnabled(false);

                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(final Void... params) {
                                solve();
                                return null;
                            }


                            @Override
                            protected void onPostExecute(final Void result) {
                                imageView.setImageBitmap(lastSolved);
                                imageView.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                setAllElementsEnabled(true);
                            }
                        }.execute();

                    } else {
                        message.setVisibility(View.VISIBLE);
                    }
                }
            };



    SeekBar.OnSeekBarChangeListener resolutionOnChangeListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    resolution = 100 - progress;
                    setPicture(lastPhotoPath);
                    startAndEnd.clear();

                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar){}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar){}
            };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // File Utils //////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = SimpleDateFormat.getDateTimeInstance().format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDirectory();
        return File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);

    }

    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        currentPhotoPath = f.getAbsolutePath();

        return f;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Album Utils /////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private String getAlbumName() {
        return getString(R.string.album_name);
    }


    private File getAlbumDirectory() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = getAlbumStorageDirectory(getAlbumName());

            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private File getAlbumStorageDirectory(String albumName){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return getFroyoAlbumStorageDirectory(albumName);
        } else {
            return getBaseAlbumStorageDirectory(albumName);
        }
    }

    private File getFroyoAlbumStorageDirectory(String albumName){
        return new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                albumName
        );
    }

    private File getBaseAlbumStorageDirectory(String albumName){
        String directory = "/dcim/";
        return new File (
                Environment.getExternalStorageDirectory()
                        + directory
                        + albumName
        );
    }
}
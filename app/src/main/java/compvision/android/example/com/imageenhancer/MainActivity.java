package compvision.android.example.com.imageenhancer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    ImageView sourceImageImageView;
    Bitmap sourceImageBitmap; Bitmap enhancedImageBitmap;

    static final int SELECT_PHOTO_REQUEST_CODE = 100;
    static final int IMAGE_CAPTURE_REQUEST_CODE = 1;

    String cutOffFractionFromEditText = "1";
    String cutOffPercentageFromEditText = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sourceImageImageView = (ImageView) findViewById(R.id.source_image_image_view);
    }

    public void takeImage(View view)
    {
        Intent imageTaker = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (imageTaker.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(imageTaker, IMAGE_CAPTURE_REQUEST_CODE);
        }

        /*
        String mCurrentPhotoPath = null;
        Intent imageTaker = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (imageTaker.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try{
                //Create an image file name
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                File image = File.createTempFile(
                        imageFileName,
                        ".jpg",
                        storageDir
                );

                // Save a file: path for use with ACTION_VIEW intents
                mCurrentPhotoPath = "file:" + image.getAbsolutePath();
                photoFile = image;
            }catch (IOException e)
            {
                e.printStackTrace();
            }

            if(photoFile != null)
            {
                if(mCurrentPhotoPath != null)
                {
                    galleryAddPic(mCurrentPhotoPath);
                }
                imageTaker.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(imageTaker, IMAGE_CAPTURE_REQUEST_CODE);
                startActivityForResult(imageTaker, IMAGE_CAPTURE_REQUEST_CODE);
            }
        }
        */
    }
    /*
    private void galleryAddPic(String mCurrentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    */

    public void selectImage(View view)
    {
        Intent imagePicker = new Intent(Intent.ACTION_PICK);
        imagePicker.setType("image/*");
        startActivityForResult(imagePicker, SELECT_PHOTO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Uri selectedImageUri = imageReturnedIntent.getData();
                    //InputStream imageStream = null;
                    //imageStream = getContentResolver().openInputStream(selectedImageUri);
                    //Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    try {
                        Bitmap selectedImage = decodeUri(selectedImageUri);
                        sourceImageImageView.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case IMAGE_CAPTURE_REQUEST_CODE:
                if (resultCode == RESULT_OK)
                {
                    Bundle extras = imageReturnedIntent.getExtras();
                    Bitmap selectedImage = (Bitmap) extras.get("data");
                    sourceImageImageView.setImageBitmap(selectedImage);
                }
                break;
        }
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, options);

        int width_tmp = options.outWidth;
        int height_tmp = options.outHeight;

        // The new size we want to scale to
        final int reqHeight = 2048;
        final int reqWidth = 2048;

        // Find the correct scale value. It should be the power of 2.
        int scale = 1;

        while (true) {
            if (width_tmp / 2 < reqWidth
                    || height_tmp / 2 < reqHeight) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage),null, options);

    }

    public void enhanceRGBCutOff(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the cutoff fraction:");
        //Set up the input
        final EditText cutOffFractionEditText = new EditText(this);
        //Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        cutOffFractionEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        builder.setView(cutOffFractionEditText);

        //Set up the buttons
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cutOffFractionFromEditText = cutOffFractionEditText.getText().toString();
                sourceImageBitmap = ((BitmapDrawable)sourceImageImageView.getDrawable()).getBitmap();
                BitmapEnhancerRGBCutOff enhancerRGBCutOff = new BitmapEnhancerRGBCutOff();
                enhancerRGBCutOff.execute(sourceImageBitmap);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void enhanceRGBPercentage(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the percentage to cutoff:");
        //Set up the input
        final EditText cutOffPercentageEditText = new EditText(this);
        //Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        cutOffPercentageEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        builder.setView(cutOffPercentageEditText);

        //Set up the buttons
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cutOffPercentageFromEditText = cutOffPercentageEditText.getText().toString();
                sourceImageBitmap = ((BitmapDrawable)sourceImageImageView.getDrawable()).getBitmap();
                BitmapEnhancerRGBPercentage enhancerRGBPercentage = new BitmapEnhancerRGBPercentage();
                enhancerRGBPercentage.execute(sourceImageBitmap);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void enhanceHSVCutOff(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the cutoff fraction:");
        //Set up the input
        final EditText cutOffFractionEditText = new EditText(this);
        //Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        cutOffFractionEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        builder.setView(cutOffFractionEditText);

        //Set up the buttons
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cutOffFractionFromEditText = cutOffFractionEditText.getText().toString();
                sourceImageBitmap = ((BitmapDrawable) sourceImageImageView.getDrawable()).getBitmap();
                BitmapEnhancerHSVCutOff enhancerHSVCutOff = new BitmapEnhancerHSVCutOff();
                enhancerHSVCutOff.execute(sourceImageBitmap);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void enhanceHSVPercentage(View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter the percentage to cutoff:");
        //Set up the input
        final EditText cutOffPercentageEditText = new EditText(this);
        //Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        cutOffPercentageEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        builder.setView(cutOffPercentageEditText);

        //Set up the buttons
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cutOffPercentageFromEditText = cutOffPercentageEditText.getText().toString();
                sourceImageBitmap = ((BitmapDrawable)sourceImageImageView.getDrawable()).getBitmap();
                BitmapEnhancerHSVPercentage enhancerHSVPercentage = new BitmapEnhancerHSVPercentage();
                enhancerHSVPercentage.execute(sourceImageBitmap);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void getNegativeButton(View view)
    {
        sourceImageBitmap = ((BitmapDrawable)sourceImageImageView.getDrawable()).getBitmap();

        //thread here
        BitmapNegative negative = new BitmapNegative();
        negative.execute(sourceImageBitmap);
    }

    public void rotateImageButton(View view)
    {
        sourceImageBitmap = ((BitmapDrawable) sourceImageImageView.getDrawable()).getBitmap();
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(sourceImageBitmap, 0, 0, sourceImageBitmap.getWidth(), sourceImageBitmap.getHeight(), matrix, true);
        sourceImageImageView.setImageBitmap(rotatedBitmap);
    }

    private class BitmapNegative extends AsyncTask<android.graphics.Bitmap, Integer, Bitmap>
    {
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        protected  void onPreExecute()
        {
            dialog.setMessage("Getting negative...");
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        protected Bitmap doInBackground(Bitmap... bitmap)
        {
            int width = sourceImageBitmap.getWidth();
            int height = sourceImageBitmap.getHeight();
            enhancedImageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(enhancedImageBitmap);
            c.drawBitmap(sourceImageBitmap, 0, 0, new Paint());

            int pixel;
            int red; int green; int blue;

            int horPixel; int verPixel;
            int percentageFactor = width/100;
            if(percentageFactor == 0){percentageFactor = 1;}
            for(horPixel = 0; horPixel <= width-1; horPixel++) {
                for (verPixel = 0; verPixel <= height-1; verPixel++) {
                    pixel = sourceImageBitmap.getPixel(horPixel, verPixel);
                    red = Color.red(pixel);
                    green = Color.green(pixel);
                    blue = Color.blue(pixel);
                    enhancedImageBitmap.setPixel(horPixel, verPixel, Color.rgb(255-red,255-green,255-blue));
                }
                publishProgress(horPixel/percentageFactor);
            }
            return enhancedImageBitmap;
        }

        protected void onProgressUpdate(Integer... progress) {
            dialog.setProgress(progress[0]);
        }

        protected void onPostExecute(Bitmap result) {
            sourceImageImageView.setImageBitmap(result);
            dialog.dismiss();
        }
    }

    private class BitmapEnhancerRGBCutOff extends AsyncTask<android.graphics.Bitmap, BitmapEnhancerRGBCutOff.TaskProgress, Bitmap>
    {
        public class TaskProgress{ int progress; String message;}
        TaskProgress currentProgress = new TaskProgress();
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        int[] parameters;
        int pixel;
        int red; int green; int blue;
        int width; int height;
        int horPixel; int verPixel;
        int percentageScaleFactor;

        double cutOffFraction;

        protected  void onPreExecute()
        {
            cutOffFraction = Double.valueOf(cutOffFractionFromEditText);
            Log.d("CutOff Fraction: ",""+cutOffFraction);
            parameters = new int[6];
            currentProgress.message = "Enhancing...";
            dialog.setMessage("Enhancing...");
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        protected Bitmap doInBackground(Bitmap... bitmap) {
            width = sourceImageBitmap.getWidth();
            height = sourceImageBitmap.getHeight();
            enhancedImageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(enhancedImageBitmap);
            c.drawBitmap(sourceImageBitmap, 0, 0, new Paint());


            parameters = getSourceImageParameters(sourceImageBitmap);
            int rMinRed = parameters[0]; int rMaxRed = parameters[1];
            int rMinGreen = parameters[2]; int rMaxGreen = parameters[3];
            int rMinBlue = parameters[4]; int rMaxBlue = parameters[5];

            if(rMinRed == rMaxRed) { rMaxRed = rMinRed++;}
            if(rMinGreen == rMaxGreen){ rMaxGreen = rMinGreen++;}
            if(rMinBlue == rMaxBlue) { rMaxBlue = rMinBlue++;}

            int sMin = 0; int sMax = 255;
            int sRed; int sGreen; int sBlue;

            currentProgress.message = "Enhancing...";
            percentageScaleFactor = width/100;
            if(percentageScaleFactor == 0){percentageScaleFactor = 1;}
            for(horPixel = 0; horPixel <= width -1; horPixel++) {
                for (verPixel = 0; verPixel <= height-1; verPixel++) {
                    pixel = sourceImageBitmap.getPixel(horPixel,verPixel);
                    red = Color.red(pixel);
                    green = Color.green(pixel);
                    blue = Color.blue(pixel);
                    sRed = (((sMax-sMin)/(rMaxRed-rMinRed))*(red-rMinRed)) + sMin;
                    sGreen = (((sMax-sMin)/(rMaxGreen-rMinGreen))*(green-rMinGreen)) + sMin;
                    sBlue = (((sMax-sMin)/(rMaxBlue-rMinBlue))*(blue-rMinBlue)) + sMin;
                    if(sRed > 255) sRed = 255;
                    if(sGreen > 255) sGreen = 255;
                    if(sBlue > 255) sBlue = 255;
                    if(sRed < 0) sRed = 0;
                    if(sGreen < 0) sGreen = 0;
                    if(sBlue < 0) sBlue = 0;
                    enhancedImageBitmap.setPixel(horPixel, verPixel, Color.rgb(sRed,sGreen,sBlue));
                }
                currentProgress.progress = horPixel/percentageScaleFactor;
                publishProgress(currentProgress);
            }
            return enhancedImageBitmap;
        }

        protected void onProgressUpdate(TaskProgress... progress) {
            dialog.setProgress(progress[0].progress);
            dialog.setMessage(progress[0].message);
        }

        protected void onPostExecute(Bitmap result) {
            sourceImageImageView.setImageBitmap(result);
            dialog.dismiss();
        }

        private int[] getSourceImageParameters(Bitmap image)
        {
            currentProgress.message = "Getting image parameters...";
            publishProgress(currentProgress);
            //rMinRed, rMaxRed, rMinGreen, rMaxGreen, rMinBlue, rMaxBlue
            parameters[0] = 300;
            parameters[1] = -300;
            parameters[2] = 300;
            parameters[3] = -300;
            parameters[4] = 300;
            parameters[5] = -300;
            //Getting maximum values
            currentProgress.message = "Getting image parameters: Maximum values...";
            publishProgress(currentProgress);
            percentageScaleFactor = width/100;
            if(percentageScaleFactor == 0){percentageScaleFactor = 1;}
            for(horPixel = 0; horPixel <= width-1; horPixel++) {
                for (verPixel = 0; verPixel <= height-1; verPixel++) {
                    pixel = image.getPixel(horPixel, verPixel);
                    red = Color.red(pixel);
                    green = Color.green(pixel);
                    blue = Color.blue(pixel);
                    //Get rMaxRed
                    if(red > parameters[1]) {
                        parameters[1] = red;
                    }
                    //get rMaxGreen
                    if(green > parameters[3]) {
                        parameters[3] = green;
                    }
                    //get rMaxBlue
                    if(blue > parameters[5]) {
                        parameters[5] = blue;
                    }
                }
                currentProgress.progress = horPixel/percentageScaleFactor;
                publishProgress(currentProgress);
            }
            Log.d("rMaxRed= ", ""+parameters[1]);
            Log.d("rMaxGreen= ", ""+parameters[3]);
            Log.d("rMaxBlue= ", ""+parameters[5]);
            //Getting minimum values
            currentProgress.message = "Getting image parameters: Minimum values...";
            publishProgress(currentProgress);
            //rMinX cutOff values
            int redCutoff = (int) (cutOffFraction*parameters[1] + 0.5);
            int greenCutoff = (int) (cutOffFraction*parameters[3] + 0.5);
            int blueCutoff = (int) (cutOffFraction*parameters[5] + 0.5);
            percentageScaleFactor = width/100;
            if(percentageScaleFactor == 0){percentageScaleFactor = 1;}
            for(horPixel = 0; horPixel <= width-1; horPixel++) {
                for (verPixel = 0; verPixel <= height-1; verPixel++) {
                    pixel = image.getPixel(horPixel, verPixel);
                    red = Color.red(pixel);
                    green = Color.green(pixel);
                    blue = Color.blue(pixel);
                    //Get rMinRed
                    if(red < parameters[0] && red > redCutoff)
                    {
                        parameters[0] = red;
                    }
                    //get rMinGreen
                    if(green < parameters[2] && green > greenCutoff)
                    {
                        parameters[2] = green;
                    }
                    //get rMinBlue
                    if(blue < parameters[4] && blue > blueCutoff)
                    {
                        parameters[4] = blue;
                    }
                }
                currentProgress.progress = horPixel/percentageScaleFactor;
                publishProgress(currentProgress);
            }
            Log.d("rMinRed= ", ""+parameters[0]);
            Log.d("rMinGreen= ", ""+parameters[2]);
            Log.d("rMinBlue= ", ""+parameters[4]);
            return parameters;
        }
    }

    private class BitmapEnhancerRGBPercentage extends AsyncTask<android.graphics.Bitmap, BitmapEnhancerRGBPercentage.TaskProgress, Bitmap>
    {
        public class TaskProgress{ int progress; String message;}
        TaskProgress currentProgress = new TaskProgress();
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        int[] parameters;
        int pixel;
        int red; int green; int blue;
        int width; int height;
        int horPixel; int verPixel;
        int percentageScaleFactor;

        int percentageCutOff;

        int[] redHistogram; int[] greenHistogram; int[] blueHistogram;

        protected  void onPreExecute()
        {
            percentageCutOff = Integer.valueOf(cutOffPercentageFromEditText);
            Log.d("CutOff Percentage: ",""+percentageCutOff);
            redHistogram = new int[256]; greenHistogram = new int[256]; blueHistogram = new int[256];
            parameters = new int[6];
            currentProgress.message = "Enhancing...";
            dialog.setMessage("Enhancing...");
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        protected Bitmap doInBackground(Bitmap... bitmap) {
            width = sourceImageBitmap.getWidth();
            height = sourceImageBitmap.getHeight();
            enhancedImageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(enhancedImageBitmap);
            c.drawBitmap(sourceImageBitmap, 0, 0, new Paint());

            generateHistograms(sourceImageBitmap);

            parameters = getSourceImageParameters(sourceImageBitmap);
            int rMinRed = parameters[0]; int rMaxRed = parameters[1];
            int rMinGreen = parameters[2]; int rMaxGreen = parameters[3];
            int rMinBlue = parameters[4]; int rMaxBlue = parameters[5];

            if(rMinRed == rMaxRed) { rMaxRed = rMinRed++;}
            if(rMinGreen == rMaxGreen){ rMaxGreen = rMinGreen++;}
            if(rMinBlue == rMaxBlue) { rMaxBlue = rMinBlue++;}

            int sMin = 0; int sMax = 255;
            int sRed; int sGreen; int sBlue;

            currentProgress.message = "Enhancing...";
            percentageScaleFactor = width/100;
            if(percentageScaleFactor == 0){percentageScaleFactor = 1;}
            for(horPixel = 0; horPixel <= width -1; horPixel++) {
                for (verPixel = 0; verPixel <= height-1; verPixel++) {
                    pixel = sourceImageBitmap.getPixel(horPixel,verPixel);
                    red = Color.red(pixel);
                    green = Color.green(pixel);
                    blue = Color.blue(pixel);
                    sRed = (((sMax-sMin)/(rMaxRed-rMinRed))*(red-rMinRed)) + sMin;
                    sGreen = (((sMax-sMin)/(rMaxGreen-rMinGreen))*(green-rMinGreen)) + sMin;
                    sBlue = (((sMax-sMin)/(rMaxBlue-rMinBlue))*(blue-rMinBlue)) + sMin;
                    if(sRed > 255) sRed = 255;
                    if(sGreen > 255) sGreen = 255;
                    if(sBlue > 255) sBlue = 255;
                    if(sRed < 0) sRed = 0;
                    if(sGreen < 0) sGreen = 0;
                    if(sBlue < 0) sBlue = 0;
                    enhancedImageBitmap.setPixel(horPixel, verPixel, Color.rgb(sRed,sGreen,sBlue));
                }
                currentProgress.progress = horPixel/percentageScaleFactor;
                publishProgress(currentProgress);
            }
            return enhancedImageBitmap;
        }

        protected void onProgressUpdate(TaskProgress... progress) {
            dialog.setProgress(progress[0].progress);
            dialog.setMessage(progress[0].message);
        }

        protected void onPostExecute(Bitmap result) {
            sourceImageImageView.setImageBitmap(result);
            dialog.dismiss();
        }

        private int[] getSourceImageParameters(Bitmap image)
        {
            currentProgress.message = "Getting image parameters...";
            publishProgress(currentProgress);

            double cutOffValue = width * height  * (1.0 * percentageCutOff/100);
            //rMinRed, rMaxRed, rMinGreen, rMaxGreen, rMinBlue, rMaxBlue

            int sum = 0;
            for(int i = 0; i <= 255; i++) {
                sum = sum + redHistogram[i];
                if(sum > cutOffValue) {
                    parameters[0] = i-1;
                    break;
                }
            }
            Log.d("rMinRed= ", ""+parameters[0]);
            sum = 0;
            for(int i = 255; i >= 0; i--) {
                sum = sum + redHistogram[i];
                if(sum > cutOffValue) {
                    parameters[1] = i+1;
                    break;
                }
            }
            Log.d("rMaxRed= ", "" + parameters[1]);
            sum = 0;
            for(int i = 0; i <= 255; i++) {
                sum = sum + greenHistogram[i];
                if (sum > cutOffValue) {
                    parameters[2] = i-1;
                    break;
                }
            }
            Log.d("rMinGreen= ", ""+parameters[2]);
            sum = 0;
            for(int i = 255; i >= 0; i--) {
                sum = sum + greenHistogram[i];
                if(sum > cutOffValue) {
                    parameters[3] = i+1;
                    break;
                }
            }
            Log.d("rMaxGreen= ", "" + parameters[3]);
            sum = 0;
            for(int i = 0; i <= 255; i++) {
                sum = sum + blueHistogram[i];
                if(sum > cutOffValue) {
                    parameters[4] = i-1;
                    break;
                }
            }
            Log.d("rMinBlue= ", ""+parameters[4]);
            sum = 0;
            for(int i = 255; i >= 0; i--) {
                sum = sum + blueHistogram[i];
                if(sum > cutOffValue) {
                    parameters[5] = i+1;
                    break;
                }
            }
            Log.d("rMaxBlue= ", ""+parameters[5]);
            return parameters;
        }

        private void generateHistograms(Bitmap image)
        {
            currentProgress.message = "Generating histograms...";
            publishProgress(currentProgress);
            percentageScaleFactor = width/100;
            if(percentageScaleFactor == 0){percentageScaleFactor = 1;}
            for(horPixel=0;horPixel<width-1;horPixel++)
            {
                for(verPixel=0;verPixel<height-1;verPixel++)
                {
                    pixel = image.getPixel(horPixel,verPixel);
                    red = Color.red(pixel);
                    green = Color.green(pixel);
                    blue = Color.blue(pixel);
                    redHistogram[red]++;
                    greenHistogram[green]++;
                    blueHistogram[blue]++;
                }
                currentProgress.progress = horPixel/percentageScaleFactor;
                publishProgress(currentProgress);
            }
        }
    }

    private class BitmapEnhancerHSVCutOff extends AsyncTask<android.graphics.Bitmap, BitmapEnhancerHSVCutOff.TaskProgress, Bitmap>
    {
        public class TaskProgress{ int progress; String message;}
        TaskProgress currentProgress = new TaskProgress();
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        float[] parameters;
        int pixel;
        int red; int green; int blue;
        int width; int height;
        int horPixel; int verPixel;
        int percentageScaleFactor;

        float[] hsv = new float[3];

        float cutOffFraction;

        protected  void onPreExecute()
        {
            cutOffFraction = Float.valueOf(cutOffFractionFromEditText);
            Log.d("Cutoff Fraction: ",""+cutOffFraction);
            parameters = new float[2];
            currentProgress.message = "Enhancing...";
            dialog.setMessage("Enhancing...");
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        protected Bitmap doInBackground(Bitmap... bitmap) {
            width = sourceImageBitmap.getWidth();
            height = sourceImageBitmap.getHeight();
            enhancedImageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(enhancedImageBitmap);
            c.drawBitmap(sourceImageBitmap, 0, 0, new Paint());

            parameters = getSourceImageParameters(sourceImageBitmap);
            float rMax; float rMin;
            rMin = parameters[0]; rMax = parameters[1];

            if(rMin == rMax) { rMax = (float)(rMax + 0.1);}
            float sMin = 0; float sMax = 1;

            currentProgress.message = "Enhancing...";
            percentageScaleFactor = width/100;
            if(percentageScaleFactor == 0){percentageScaleFactor = 1;}
            for (horPixel = 0; horPixel < width - 1; horPixel++)
            {
                for(verPixel=0; verPixel<height-1; verPixel++)
                {
                    pixel = sourceImageBitmap.getPixel(horPixel, verPixel);
                    red = Color.red(pixel);
                    green = Color.green(pixel);
                    blue = Color.blue(pixel);
                    Color.RGBToHSV(red, green, blue, hsv);
                    hsv[2] = (((sMax-sMin)/(rMax-rMin))*(hsv[2]-rMin)) + sMin;
                    if(hsv[2] > 1) hsv[2] = 1;
                    if(hsv[2] < 0) hsv[2] = 0;
                    pixel = Color.HSVToColor(hsv);
                    enhancedImageBitmap.setPixel(horPixel, verPixel, pixel);
                }
                currentProgress.progress = horPixel/percentageScaleFactor;
                publishProgress(currentProgress);
            }
            return enhancedImageBitmap;
        }

        protected void onProgressUpdate(TaskProgress... progress) {
            dialog.setProgress(progress[0].progress);
            dialog.setMessage(progress[0].message);
        }

        protected void onPostExecute(Bitmap result) {
            sourceImageImageView.setImageBitmap(result);
            dialog.dismiss();
        }

        private float[] getSourceImageParameters(Bitmap image)
        {
            currentProgress.message = "Getting image parameters...";
            publishProgress(currentProgress);
            //rMin, rMax
            parameters[0] = 2;
            parameters[1] = -1;

            //Getting maximum value
            currentProgress.message = "Getting image parameters: Maximum value...";
            publishProgress(currentProgress);
            percentageScaleFactor = width/100;
            if(percentageScaleFactor == 0){percentageScaleFactor = 1;}
            for (horPixel = 0; horPixel < width - 1; horPixel++) {
                for (verPixel = 0; verPixel < height - 1; verPixel++) {
                    pixel = image.getPixel(horPixel, verPixel);
                    red = Color.red(pixel);
                    green = Color.green(pixel);
                    blue = Color.blue(pixel);
                    Color.RGBToHSV(red, green, blue, hsv);
                    if (hsv[2] > parameters[1]) {
                        parameters[1] = hsv[2];
                    }
                }
                currentProgress.progress = horPixel/percentageScaleFactor;
                publishProgress(currentProgress);
            }
            Log.d("rMax= ", ""+parameters[1]);
            //Getting minimum value
            currentProgress.message = "Getting image parameters: Minimum value...";
            publishProgress(currentProgress);
            //rMin cutOff value
            float cutOff = cutOffFraction*parameters[1];
            percentageScaleFactor = width/100;
            if(percentageScaleFactor == 0){percentageScaleFactor = 1;}
            for (horPixel = 0; horPixel < width - 1; horPixel++) {
                for (verPixel = 0; verPixel < height - 1; verPixel++) {
                    pixel = image.getPixel(horPixel, verPixel);
                    red = Color.red(pixel);
                    green = Color.green(pixel);
                    blue = Color.blue(pixel);
                    Color.RGBToHSV(red, green, blue, hsv);
                    if (hsv[2] < parameters[0] && hsv[2] > cutOff) {
                        parameters[0] = hsv[2];
                    }
                }
                currentProgress.progress = horPixel/percentageScaleFactor;
                publishProgress(currentProgress);
            }
            Log.d("rMin= ", ""+parameters[0]);
            return parameters;
        }
    }

    private class BitmapEnhancerHSVPercentage extends AsyncTask<android.graphics.Bitmap, BitmapEnhancerHSVPercentage.TaskProgress, Bitmap>
    {
        public class TaskProgress{ int progress; String message;}
        TaskProgress currentProgress = new TaskProgress();
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        float[] parameters;
        int pixel;
        int red; int green; int blue;
        int width; int height;
        int horPixel; int verPixel;
        int percentageScaleFactor;

        float[] hsv;

        int percentageCutOff;

        int[] histogram;

        protected  void onPreExecute()
        {
            percentageCutOff = Integer.valueOf(cutOffPercentageFromEditText);
            Log.d("CutOff Percentage: ",""+percentageCutOff);
            hsv = new float[3];
            histogram = new int[1000];
            parameters = new float[2];
            currentProgress.message = "Enhancing...";
            dialog.setMessage("Enhancing...");
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }

        protected Bitmap doInBackground(Bitmap... bitmap) {
            width = sourceImageBitmap.getWidth();
            height = sourceImageBitmap.getHeight();
            enhancedImageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(enhancedImageBitmap);
            c.drawBitmap(sourceImageBitmap, 0, 0, new Paint());

            generateHistograms(sourceImageBitmap);

            parameters = getSourceImageParameters(sourceImageBitmap);
            float rMin = parameters[0]; float rMax = parameters[1];

            if(rMin == rMax) { rMax = (float) (rMax + 0.1);}
            float sMin = 0; float sMax = 1;

            currentProgress.message = "Enhancing...";
            percentageScaleFactor = width/100;
            if(percentageScaleFactor == 0){percentageScaleFactor = 1;}
            for (horPixel = 0; horPixel < width - 1; horPixel++)
            {
                for(verPixel=0; verPixel<height-1; verPixel++)
                {
                    pixel = sourceImageBitmap.getPixel(horPixel, verPixel);
                    red = Color.red(pixel);
                    green = Color.green(pixel);
                    blue = Color.blue(pixel);
                    Color.RGBToHSV(red, green, blue, hsv);
                    hsv[2] = (((sMax-sMin)/(rMax-rMin))*(hsv[2]-rMin)) + sMin;
                    if(hsv[2] > 1) hsv[2] = 1;
                    if(hsv[2] < 0) hsv[2] = 0;
                    pixel = Color.HSVToColor(hsv);
                    enhancedImageBitmap.setPixel(horPixel, verPixel, pixel);
                }
                currentProgress.progress = horPixel/percentageScaleFactor;
                publishProgress(currentProgress);
            }
            return enhancedImageBitmap;
        }

        protected void onProgressUpdate(TaskProgress... progress) {
            dialog.setProgress(progress[0].progress);
            dialog.setMessage(progress[0].message);
        }

        protected void onPostExecute(Bitmap result) {
            sourceImageImageView.setImageBitmap(result);
            dialog.dismiss();
        }

        private float[] getSourceImageParameters(Bitmap image)
        {
            currentProgress.message = "Getting image parameters...";
            publishProgress(currentProgress);
            //rMin, rMax
            double cutOffValue = width * height  * (1.0 * percentageCutOff/100);
            //Getting maximum value
            currentProgress.message = "Getting image parameters: Maximum value...";
            publishProgress(currentProgress);
            int sum = 0;
            for(int i = 0; i <= 999; i++) {
                sum = sum + histogram[i];
                if(sum > cutOffValue) {
                    parameters[0] = (float) (i-1)/999;
                    break;
                }
            }
            Log.d("rMin= ", "" + parameters[0]);
            //Getting minimum value
            currentProgress.message = "Getting image parameters: Minimum value...";
            publishProgress(currentProgress);
            sum = 0;
            for(int i = 999; i >= 0; i--) {
                sum = sum + histogram[i];
                if(sum > cutOffValue) {
                    parameters[1] = (float) (i+1)/999;
                    break;
                }
            }
            Log.d("rMax= ", "" + parameters[1]);
            return parameters;
        }

        private void generateHistograms(Bitmap image)
        {
            currentProgress.message = "Generating histograms...";
            publishProgress(currentProgress);
            percentageScaleFactor = width/100;
            if(percentageScaleFactor == 0){percentageScaleFactor = 1;}
            for(horPixel=0;horPixel<width-1;horPixel++)
            {
                for(verPixel=0;verPixel<height-1;verPixel++) {
                    pixel = image.getPixel(horPixel, verPixel);
                    red = Color.red(pixel);
                    green = Color.green(pixel);
                    blue = Color.blue(pixel);
                    Color.RGBToHSV(red, blue, green, hsv);
                    histogram[Math.round(hsv[2] * 999)]++;
                }
                currentProgress.progress = horPixel/percentageScaleFactor;
                publishProgress(currentProgress);
            }
        }
    }
}

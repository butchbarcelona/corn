package com.perpetual.corn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VideoFeederFrag.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VideoFeederFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoFeederFrag extends Fragment {

    Button btnDetect;
    public VideoFeederFrag() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VideoFeederFrag.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoFeederFrag newInstance(String param1, String param2) {
        VideoFeederFrag fragment = new VideoFeederFrag();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_video_feeder, container, false);

        final VideoFeederView videoFeederView = (VideoFeederView) view.findViewById(R.id.video_feed);

        final ImageView img = (ImageView) view.findViewById(R.id.img_temp);

        btnDetect = (Button) view.findViewById(R.id.btn_detect);
        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //take picture
                //Bitmap bmapFeed = videoFeederView.getBitmapView();
                //img.setImageBitmap(bmapFeed);
                //img.setImageBitmap(
                        processImage(((BitmapDrawable)img.getDrawable()).getBitmap(), img);
                //);
                Log.d("DJICORN","end image process");


            }
        });


        return view;
    }

    public void changeYellowToRed(Bitmap myBitmap, ImageView imageView){

        currImgView  =imageView;
        currBitmap = myBitmap;
        int[] pixels = new int[myBitmap.getHeight()*myBitmap.getWidth()];

        myBitmap.getPixels(pixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(), myBitmap.getHeight());

        // Create new immutable bitmap
        Bitmap newBmp = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(newBmp);
        c.drawBitmap(myBitmap, 0, 0, new Paint());

        for (int i=0; i<myBitmap.getHeight()*myBitmap.getWidth(); i++) {

            int color = pixels[i];


            //r and g are high and blue low becomes yellow
            //https://www.researchgate.net/post/How_to_find_the_intensity_of_yellow_color_from_an_image
            //Yellow = White - Blue

            int yellow = Color.WHITE - Color.blue(color);

            //Log.d("DJICORN","yellow: "+yellow);
            if(Math.abs(yellow) > 153){ //60% (255) of pixel is yellow
                //it is yellow
                pixels[i] = Color.RED;
                Log.d("DJICORN","yellow: "+yellow);
            }
        }
        newBmp.setPixels(pixels, 0, myBitmap.getWidth(), 0, 0, myBitmap.getWidth(), myBitmap.getHeight());

        currImgView.setImageBitmap(newBmp);
    }

    public void processImage(Bitmap myBitmap, ImageView imageView){

        //change yellow pixels to red
        changeYellowToRed(myBitmap, imageView);


        //canny edge
       // detectEdges(myBitmap, imageView);


    }

    Bitmap currBitmap;
    ImageView currImgView;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this.getContext()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {

                    Mat rgba = new Mat();
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    Utils.bitmapToMat(currBitmap, rgba);

                    Mat edges = new Mat(rgba.size(), CvType.CV_8UC1);
                    Imgproc.cvtColor(rgba, edges, Imgproc.COLOR_RGB2GRAY, 4);
                    Imgproc.Canny(edges, edges, 80, 100);
                    Bitmap resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(edges, resultBitmap);
                    currImgView.setImageBitmap(resultBitmap);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    private void detectEdges(final Bitmap bitmap, final ImageView imageView) {
        currImgView  =imageView;
        currBitmap = bitmap;

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this.getContext(), mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

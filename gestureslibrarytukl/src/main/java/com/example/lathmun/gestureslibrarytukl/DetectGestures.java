package com.example.lathmun.gestureslibrarytukl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
//finallllllll
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

/**
 * Created by Lathmun on 1/6/16.
 */
public class DetectGestures implements SensorEventListener{


    boolean draggingObject_;
    boolean zoomingObject_;
    boolean motionDetected_;
    boolean motionStopperTemp_;

    long dragUpdate_;
    long zoomingTime_ = 0;

    Vector<Vector> angle_;
    Vector<Vector> motion_;
    Vector<Vector> endingAngle_;
    Vector<Vector> endingPostion_;
    Vector<Vector> startingAngle_;
    Vector<Vector> motionPostion_;
    Vector<Vector> startingPostion_;

    double [] minAngle_;
    double [] maxAngle_;
    double [] maxMotion_;
    double [] _initialValues;
    double [] secondMaxMotion_;
    double [] differenceOfAngle_;




    private SensorManager mgr;
    private Sensor accelerometer;



    private float timestamp;
    private float last_x, last_y, last_z;
    private float[] motion = new float[3];
    private float[] gravity = new float[3];
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];


    private double [] ratio = new double[3];
    private double [] mAngle = new double[3];

    private  double counter;
    private double counterX = 0;
    private double angleXForDrage_ = 0;
    private double angleYForDrage_ = 0;
    private double angleZForDrage_ = 0;

    int tempValue;
    int tapCounter_ = 0;
    int dragDirection_ =0;
    int motionCounter_ = 0;
    int _resetVectorsCounter = 0;
    private int directionOfMovement_ = -1;
    private static final int SHAKE_THRESHOLD = 1200;

    private long lastUpdate = 0;
    private long lastUpdate1 = 0;
    private long timeToCheckLongGesture = 0;


    EditText _text;
    Button _searchButton;

    FileOutputStream fos;

    private GestureDetectionInterface _interfaceObject;
    Context _appContext;
    private boolean _somethingHappened;


    public DetectGestures(GestureDetectionInterface interfaceObj, Context c)
    {

        _appContext = c;
        _somethingHappened = true;
        _interfaceObject = interfaceObj;

        registerSensor();
        initializeVariable();
    }

    private void initializeVariable() {
        startingPostion_ = new Vector<Vector>();
        endingPostion_ = new Vector<Vector>();
        startingAngle_ = new Vector<Vector>();
        endingAngle_ = new Vector<Vector>();
        motion_ = new Vector<Vector>();
        motionPostion_ = new Vector<Vector>();
        angle_ = new Vector<Vector>();

        maxMotion_ = new double[3];
        secondMaxMotion_ = new double[3];
        maxAngle_ = new double[3];
        minAngle_ = new double[3];
        differenceOfAngle_ = new double[3];
        _initialValues = new double[3];
        motionStopperTemp_ = false;
        tempValue = 0 ;
    }

    public void registerSensor ()
    {

        SensorManager sensorManager = (SensorManager)_appContext.getSystemService(_appContext.SENSOR_SERVICE);
        if (sensorManager == null)
        {
            _interfaceObject.registrationError("error");
            return;
        }
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_FASTEST);
        _interfaceObject.GestureType("Connected");
    }


    @Override
    public void onSensorChanged(SensorEvent event) {



        for (int i = 0; i < 3; i++) {
            gravity[i] = (float) (0.1 * event.values[i] + 0.9 * gravity[i]);
            motion[i] = event.values[i] - gravity[i];
            ratio[i] = gravity[i] / SensorManager.GRAVITY_EARTH;
            if (ratio[i] > 1.0) ratio[i] = 1.0;
            if (ratio[i] < -1.0) ratio[i] = -1.0;
            mAngle[i] = Math.toDegrees(Math.acos(ratio[i]));
           /*
            if (gravity[i] < 0) {
                mAngle[i] = -mAngle[i];
            }
            */
        }
        if(motionDetected_)
        {
            addValuesToVector(motionPostion_, event.values[0], event.values[1], event.values[2]);
            addValuesToVector(motion_, motion[0], motion[1], motion[2]);
            addValuesToVector(angle_, mAngle[0],mAngle[1],mAngle[2]);
        }
        // if (counter++ % 1 == 0) {
        String msg = String.format(
                "Raw values\nX: %8.4f\nY: %8.4f\nZ: %8.4f\n" +
                        "Gravity\nX: %8.4f\nY: %8.4f\nZ: %8.4f\n" +
                        "Motion\nX: %8.4f\nY: %8.4f\nZ: %8.4f\nAngle0: %8.1f\n" +
                        "Angle1: %8.1f\n" +
                        "Angle2: %8.1f"+ "\nX : RED Y:Green Z:Yellow",
                event.values[0], event.values[1], event.values[2],
                gravity[0], gravity[1], gravity[2],
                motion[0], motion[1], motion[2],
                mAngle[0], mAngle[1], mAngle[2]);

        counter = 1;

        counterX = counterX + 0.02;
        /*
                double v0 = (double) Math.round(event.values[0] * 100) / 100;
                double v1 = (double) Math.round(event.values[1] * 100) / 100;
                double v2 = (double) Math.round(event.values[2] * 100) / 100;
        */
        double v0 = event.values[0];
        double v1 = event.values[1];
        double v2 = event.values[2];

        double m0 = (double) Math.round(motion[0] * 100) / 100;
        double m1 = (double) Math.round(motion[1] * 100) / 100;
        double m2 = (double) Math.round(motion[2] * 100) / 100;
        //Log.d("munir", Double.toString(v));


        long curTime = System.currentTimeMillis();

        String position = null;
        //if ((curTime - lastUpdate1) > 400) {
        int seconds = (int) (curTime / 1000) % 60;
        int minutes = (int) ((curTime / (1000 * 60)) % 60);
        int hours = (int) ((curTime / (1000 * 60 * 60)) % 24);
        String time = Integer.toString(hours) + ":" + Integer.toString(minutes) + ":" + Integer.toString(seconds) + "\n";
        //long diffTime = (curTime - lastUpdate1);
        //lastUpdate1 = curTime;

        //if (m0 <= -4 || m0 >= 4 || m1 <= -4 || m1 >= 4 || m2 <= -4 || m2 >= 4) {
        if (m0 <= -1 || m0 >= 1 || m1 <= -1 || m1 >= 1 || m2 <= -1 || m2 >= 1) {
            motionDetected_ = true;
            _resetVectorsCounter++;
            //addValuesToVector(motion_, m0, m1, m2);
            //   Log.d("Value_1", "X = " + Double.toString(v0) + " , Y =  " + Double.toString(v1) + " , Z = " + Double.toString(v2) + " m  " + Double.toString(m0) + "  " + Double.toString(m1) + "  " + Double.toString(m2));

        }

        else {
            if (motionDetected_ && motionCounter_ > 19) {
                removeAllElements(endingPostion_);
                removeAllElements(endingAngle_);
                addValuesToVector(endingPostion_, v0, v1, v2);
                addValuesToVector(endingAngle_ , mAngle[0], mAngle[1], mAngle[2]);


                // gestureFindingTime_ = true;
                _resetVectorsCounter = 0;
                mapGestures();
            }
            if (motionCounter_ > 20 ) {
                motionCounter_ = 0;
                motionDetected_ = false;
                removeAllElements(motion_);
                removeAllElements(motionPostion_);
                removeAllElements((angle_));
            }
            motionCounter_++;

        }
                /*
                 if ((m0 <= -1 || m0 >= 1 || m1 <= -1 || m1 >= 1 || m2 <= -1 || m2 >= 1)) {


                    Log.d("Value_1", "Positions  X = " + Double.toString(v0) + " , Y =  " + Double.toString(v1) + " , Z = " + Double.toString(v2) + " m  " + Double.toString(m0) + "  " + Double.toString(m1) + "  " + Double.toString(m2));
                    position = "1:"+Double.toString(v0) + "," + Double.toString(v1) + "," + Double.toString(v2) + ",m," + Double.toString(m0) + "," + Double.toString(m1) + "," + Double.toString(m2)+"t,"+time;

                }
                else if(motionDetected_){
                    Log.d("Value_motion", "Positions  X = " + Double.toString(v0) + " , Y =  " + Double.toString(v1) + " , Z = " + Double.toString(v2) + " m  " + Double.toString(m0) + "  " + Double.toString(m1) + "  " + Double.toString(m2));

                }

    */
        if (position != null) {
            // Log.d("file_f", position);
            // writeFile(position);

        }
        //  Log.d("Position","X ="+ Double.toString(v0)+ ",Y =" +Double.toString(v1)+ ",Z ="+ Double.toString(v2)+ "\n");
        //  String position = " "+ Double.toString(v0)+ " " +Double.toString(v1)+ " "+ Double.toString(v2)+ " " + Double.toString(m0)+"  " + Double.toString(m1)+"  " + Double.toString(m2)+"\n  ";
        //if (!motionDetected_ ) {
        if (_resetVectorsCounter == 0 ) {
            removeAllElements(startingPostion_);
            addValuesToVector(startingPostion_, v0, v1, v2);

            removeAllElements(startingAngle_);
            addValuesToVector(startingAngle_, mAngle[0], mAngle[1], mAngle[2]);

            timeToCheckLongGesture = System.currentTimeMillis();
        }

        //}
        long tempTime = System.currentTimeMillis();
        if(draggingObject_== true  & (tempTime-dragUpdate_) > 1500)
        {
            Log.d("drag", "angle x " + Double.toString(angleXForDrage_) + "  angle y" + Double.toString(angleZForDrage_));



            dragUpdate_ = System.currentTimeMillis();
            if(findDifference(mAngle[0],angleXForDrage_) > 10)
            {
                if ((mAngle[0]-angleXForDrage_) < 0)
                {
                    Log.d("drag", "x-value");
                    sendCounituousValuesForDrag(_initialValues, mAngle);
                    //button_.setX(button_.getX() - 1);
                   // mMap.animateCamera(CameraUpdateFactory.scrollBy(-100,0));

                }
                else
                {
                    Log.d("drag", "x+value");
                    sendCounituousValuesForDrag(_initialValues, mAngle);
                    //button_.setX(button_.getX() + 1);
                    //mMap.animateCamera(CameraUpdateFactory.scrollBy(100,0));
                }
                dragUpdate_ = System.currentTimeMillis();
            }
            else if(findDifference(mAngle[2],angleZForDrage_) > 10)
            {
                if ((mAngle[2]-angleZForDrage_) < 0)
                {
                    Log.d("drag", "y+value");
                    sendCounituousValuesForDrag(_initialValues, mAngle);
               //     mMap.animateCamera(CameraUpdateFactory.scrollBy(0,100));
                }
                else
                {
                    Log.d("drag", "y-value");
                    sendCounituousValuesForDrag(_initialValues, mAngle);
                //    mMap.animateCamera(CameraUpdateFactory.scrollBy(0,-100));
                }
                dragUpdate_ = System.currentTimeMillis();
            }

        }
        long zoomTempTime = System.currentTimeMillis();
        //if(zoomingObject_)
        if(zoomingObject_== true  & (zoomTempTime-zoomingTime_) > 1500)
        {
            Log.d("zoom", "angle x " + Double.toString(angleXForDrage_) + "  angle y" + Double.toString(angleYForDrage_) + "  angle z" + Double.toString(angleZForDrage_));


            if(mAngle[1] < angleYForDrage_ ){

                sendCounituousValuesForZOOM(_initialValues, mAngle);
                zoomingTime_ = System.currentTimeMillis();
            }
            //    mMap.animateCamera(CameraUpdateFactory.zoomIn());
            else if(mAngle[1] > angleYForDrage_ ){
                sendCounituousValuesForZOOM(_initialValues, mAngle);
                zoomingTime_ = System.currentTimeMillis();
            }
             //   mMap.animateCamera(CameraUpdateFactory.zoomOut());

        }
        shakeDetection(v0, v1, v2);


        // }


        // }
      /*  } //if ended
        else {
            //Log.d("MotionStopper", "MotionStopperElse");
            tempValue++;
            if (tempValue >= 250) {
                tempValue = 0;
                motionStopperTemp_ = false;
            }

        }
        */
        //else ended
    }

    private void sendCounituousValuesForZOOM(double[] initialValues, double[] mAngle) {
        _interfaceObject.continuosValues(Global.Gestures.EXTREME_ZOOM, initialValues, mAngle);
    }

    private void sendCounituousValuesForDrag(double[] initialValues, double[] mAngle) {
        _interfaceObject.continuosValues(Global.Gestures.LONG_PRESS, initialValues, mAngle);
    }

    private void addValuesToVector(Vector<Vector> vector, double v0, double v1, double v2) {
        Vector<Double> temp = new Vector<Double>();
        temp.add(v0);
        temp.add(v1);
        temp.add(v2);
        vector.add(temp);

        /*
        Vector<Double> angle = new Vector<Double>();
        angle.add(mAngle[0]);
        angle.add(mAngle[1]);
        angle.add(mAngle[2]);
        vector.add(angle);

        */
    }

    private void removeAllElements(Vector<Vector> vector) {
        vector.removeAllElements();
    }

    private void mapGestures() {

        long currentTime = System.currentTimeMillis();
        long difference = currentTime - lastUpdate1;
        if(difference <= 1000)
        {
            lastUpdate1 = System.currentTimeMillis();
            showGestureMessage(Global.WAIT_MESSAGE);
            return;
        }
        int counterForGesture = 0;
        if ((startingPostion_.size()< 1)){
            Log.d("Gesture", "its not gesture because of starting postion 0\n");
            //  gestureFindingTime_ = false;
        }
        else{


            Vector startPosition = startingPostion_.get(0);
            Vector endPosition = endingPostion_.get(0);
            for (int i = 0; i< startPosition.size(); i++){
                double ans;
                Object tempww = startPosition.get(i);
                double temp = (double) tempww;
                Object tempwrr = endPosition.get(i);
                double temp2 = (double)tempwrr;

                if(temp < 0){
                    temp = temp * -1;
                }
                if(temp2 < 0){
                    temp2 = temp2 * -1;
                }
                ans = temp - temp2;
                if(ans >= -2 && ans <= 2){
                    //Log.d("Gesture", "gesture detected");
                    counterForGesture++;
                    Log.d("testing", Integer.toString(counterForGesture));
                }

            }
            if(counterForGesture == 3)
            {
                maxMotion_[0] = secondMaxMotion_[0] = maxAngle_[0] = minAngle_[0] = 0;
                maxMotion_[1] = secondMaxMotion_[1] = maxAngle_[1] = minAngle_[1] = 0;
                maxMotion_[2] = secondMaxMotion_[2] = maxAngle_[2] = minAngle_[2] = 0;

                matchAGesture();
            }
            else
            {


                if(draggingObject_ == false)
                {
                    findMaxAngle();
                    detectPinch();
                }

            }




        }

    }

    private void detectPinch() {


        Vector startPosition = startingPostion_.get(0);
        double  startX = (double) startPosition.get(0);
        double  startY = (double) startPosition.get(1);
        double  startZ = (double) startPosition.get(2);

        Vector endPosition = endingPostion_.get(0);
        double  endX = (double) endPosition.get(0);
        double  endY = (double) endPosition.get(1);
        double  endZ = (double) endPosition.get(2);

        Log.d("Pinch", " start X " + Double.toString(startX) + " Y " + Double.toString(startY) + " Z " + Double.toString(startZ));
        Log.d("Pinch", " end X " + Double.toString(endX) + " Y " + Double.toString(endY) + " Z " + Double.toString(endZ));

        double startAngleX = (double) startingAngle_.get(0).get(0);
        double startAngleY = (double) startingAngle_.get(0).get(1);
        double startAngleZ = (double) startingAngle_.get(0).get(2);

        double endAngleX = (double) endingAngle_.get(0).get(0);
        double endAngleY = (double) endingAngle_.get(0).get(1);
        double endAngleZ = (double) endingAngle_.get(0).get(2);

        int rotationValue = 35;
        int rotationValueY = 10;
        if(startAngleY >  33)
        {
            rotationValue = 20;
            rotationValueY = 5;
        }

      /*
        if(( startAngleY- minAngle_[1] >=60 || (endAngleZ - startAngleZ > 60)) & differenceOfAngle_[0]<10)
        {

            showGestureMessage(Global.Gestures.EXTREME_ZOOM_IN);
            zoomingObject_ = !zoomingObject_;

            angleXForDrage_ = (startAngleX + endAngleX)/2;
            angleYForDrage_ = (startAngleY + endAngleY)/2;
            angleZForDrage_ = (startAngleZ + endAngleZ)/2;

            lastUpdate1 = System.currentTimeMillis() + 3000;
        }
        else if(( startAngleY- maxAngle_[1] <= -60 || (endAngleZ - startAngleZ < -60)) & differenceOfAngle_[0]<10)
        {

            showGestureMessage(Global.Gestures.EXTREME_ZOOM_OUT);
            zoomingObject_ = !zoomingObject_;

            angleXForDrage_ = (startAngleX + endAngleX)/2;
            angleYForDrage_ = (startAngleY + endAngleY)/2;
            angleZForDrage_ = (startAngleZ + endAngleZ)/2;

            lastUpdate1 = System.currentTimeMillis() + 3000;
        }



        else */

        if(( startAngleY- minAngle_[1] >=45 || (endAngleZ - startAngleZ > 35)) & differenceOfAngle_[0]<10 & !zoomingObject_)
        {

            showGestureMessage(Global.Gestures.ZOOM_IN);
           // mMap.animateCamera(CameraUpdateFactory.zoomBy(2));
            lastUpdate1 = System.currentTimeMillis() + 3000;
        }
        //else if( startAngleY- maxAngle_[1] <= -30 & differenceOfAngle_[0]<10)
        else if(( startAngleY- maxAngle_[1] <= -45 || (endAngleZ - startAngleZ < -35)) & differenceOfAngle_[0]<10 & !zoomingObject_)
        {
            showGestureMessage(Global.Gestures.ZOOM_OUT);
          //  mMap.animateCamera(CameraUpdateFactory.zoomBy(-2));
            lastUpdate1 = System.currentTimeMillis() + 3000;
        }
        else if( startAngleX - endAngleX >=rotationValue & differenceOfAngle_[1] > rotationValueY  & !zoomingObject_)
        {

            showGestureMessage(Global.Gestures.ROTATION_ANTI);
          //  CameraPosition oldPosition = mMap.getCameraPosition();
         //   CameraPosition newPosition  = CameraPosition.builder(oldPosition).bearing(30).build();

          //  mMap.animateCamera(CameraUpdateFactory.newCameraPosition(newPosition));

            lastUpdate1 = System.currentTimeMillis() + 1500;

        }
        else if( startAngleX - endAngleX <= -rotationValue & differenceOfAngle_[1] > rotationValueY & !zoomingObject_)
        {

            showGestureMessage(Global.Gestures.ROTATION_CLOCKWISE);
         //   CameraPosition oldPosition = mMap.getCameraPosition();
            //  CameraPosition newPosition  = CameraPosition.builder(oldPosition).bearing(-30).build();

          //  mMap.animateCamera(CameraUpdateFactory.newCameraPosition(newPosition));
            lastUpdate1 = System.currentTimeMillis() + 1500;
        }




/*

        if((findDifference(startX,endX)<=1 & startY- endZ >=0) || (findDifference(startX,endX)<=1 & findDifference(startZ,endY)<=2))
        {

            showGestureMessage("Pinch out (zoom in)");
            lastUpdate1 = System.currentTimeMillis() + 3000;
        }
        else if(findDifference(startX,endX)<=1 & startY-endY >= 3.5 & findDifference(startZ,endZ)<=5)
        {

            showGestureMessage("Pinch in (zoom out)");
            lastUpdate1 = System.currentTimeMillis() + 3000;
        }
    */

    }

    private void matchAGesture() {

        if(draggingObject_ == false &  !zoomingObject_)
        {
            findMaxMotion();
            findMaxAngle();
            findPress();
            findDrag();
            findTap();
        }
        else if(draggingObject_)
        {
            findMaxAngle();
            findPress();
        }


    }

    private void findTap()
    {
        Vector startPosition = startingPostion_.get(0);
        double [] startingPosition = {0,0,0};

        for (int i = 0; i< startPosition.size(); i++){
            Object tempObject = startPosition.get(i);
            startingPosition[i] = (double) tempObject;
        }
        double [] maxPosition = {0,0,0};

        for (int i = 0; i< motionPostion_.size(); i++)
        {
            Vector tempMotionVector = motionPostion_.get(i);

            for(int j =0; j< tempMotionVector.size(); j++ )
            {
                Object tempObject = tempMotionVector.get(j);
                double tempValue = (double) tempObject;
                double tempstartValue = startingPosition[j];
                double tempMax = maxPosition[j];

                /*
                if(tempstartValue < 0)
                {
                    tempstartValue = tempstartValue * -1;
                }
                if(tempValue < 0)
                {
                    tempValue = tempValue *-1;
                }
                */
                double difference = findDifference(tempstartValue,tempValue);

                if(difference > maxPosition[j])
                {
                    maxPosition[j] = difference;
                }

            }

        }

        int tapGestureTester = 0;
        if(maxPosition[2] >=3 )
        {
            if(maxPosition[1] >=2)
            {
                Log.d("testing", "passed till Y");
                if(maxPosition[0] >= -1 & maxPosition[0] <=3  & (maxPosition[2]>=5 || maxPosition[1]>=5))
                {
                    if(tapCounter_ <=2)
                    {
                        showGestureMessage(Global.Gestures.TAP);
                        lastUpdate1 = System.currentTimeMillis();
                        if(_text != null){
                            //InputMethodManager imm = (InputMethodManager)
                            //        getSystemService(Context.INPUT_METHOD_SERVICE);
                           // if(imm != null){
                             //   imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                            //}
                        }

                    }
                    else
                    {
                        showGestureMessage(Global.Gestures.DOUBLE_TAP);
                        lastUpdate1 = System.currentTimeMillis();
                    }

                    // gestureFindingTime_ = false;

                }
            }else if(maxPosition[0] >= -1 & maxPosition[0] <=3  & (maxPosition[2]>=5 || maxPosition[1]>=5)) {
                if (tapCounter_ <=2)
                {
                    showGestureMessage(Global.Gestures.TAP);
                    lastUpdate1 = System.currentTimeMillis();

                    if(_text != null){
                   //     InputMethodManager imm = (InputMethodManager)
                     //           getSystemService(Context.INPUT_METHOD_SERVICE);
                       // if(imm != null){
                         //   imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                        }
                    }
                }
                else
                {
                    showGestureMessage(Global.Gestures.DOUBLE_TAP);
                    lastUpdate1 = System.currentTimeMillis();
                }

            }
            //  Log.d("testing", Integer.toString(motionPostion_.size()) + " x  " + Double.toString(maxPosition[0]) + "Y " + Double.toString(maxPosition[1]) + "z " + Double.toString(maxPosition[2]));
        }

    private void findDrag()
    {
        double startAngleX = (double) startingAngle_.get(0).get(0);
        double endAngleX = (double) endingAngle_.get(0).get(0);

        Vector tempVector = motionPostion_.get(0);
        double tempFirstValue = (double) tempVector.get(0);
        if(tempFirstValue < 0 ){
            dragDirection_ = 1;
        }
        else{
            dragDirection_ = 2;
        }

        if(dragDirection_ == 1 & differenceOfAngle_[0] >=7 & differenceOfAngle_[1] >= 8  & differenceOfAngle_[2] >= 0 & differenceOfAngle_[2] <=25 )
        {
            showGestureMessage(Global.Gestures.LEFT_DRAG);
         //   mMap.animateCamera(CameraUpdateFactory.scrollBy(-700, 0));
            lastUpdate1 = System.currentTimeMillis();

        }
        else if(dragDirection_ == 2 & differenceOfAngle_[0] >=7 & differenceOfAngle_[1] >= 10  & differenceOfAngle_[2] >= 0 & differenceOfAngle_[2] <=25 )
        {
            showGestureMessage(Global.Gestures.RIGHT_DRAG);
           // mMap.animateCamera(CameraUpdateFactory.scrollBy(700, 0));
            lastUpdate1 = System.currentTimeMillis();
        }

    }
    private void findPress()
    {
        Log.d("pinch", "inside press");
        if(!draggingObject_)
        {

            //if(differenceOfAngle_[0] >10 & differenceOfAngle_[0] <= 50 & differenceOfAngle_[1] <= 5 & differenceOfAngle_[2] <= 50 )
            if(differenceOfAngle_[0] >10 & differenceOfAngle_[0] <= 30 & differenceOfAngle_[1] <= 6 & differenceOfAngle_[2] <= 35 )
            {
                showGestureMessage(Global.Gestures.PRESS);

                lastUpdate1 = System.currentTimeMillis();

            }
        }
        if(differenceOfAngle_[0] >15  & differenceOfAngle_[2] > 35 )
        {
            Log.d("pinch ", "inside dragging object" + Double.toString(differenceOfAngle_[0]) + " y "+Double.toString(differenceOfAngle_[1]) + " z "  + Double.toString(differenceOfAngle_[2]));
            if(System.currentTimeMillis() - lastUpdate1 <1000)
            {
                Log.d("Returninggggggg....." , "done");
                return;
            }

            showGestureMessage(Global.Gestures.LONG_PRESS);
            lastUpdate1 = System.currentTimeMillis();
            draggingObject_ = !draggingObject_;

            _initialValues[0]= angleXForDrage_ = (Double) endingAngle_.get(0).get(0);
            _initialValues[1]=angleYForDrage_ = (Double) endingAngle_.get(0).get(1);
            _initialValues[2]=angleZForDrage_ = (Double) endingAngle_.get(0).get(2);



        }
    }

    private void findMaxMotion() {

        for(int i = 0; i < motion_.size(); i++)
        {
            Vector tempObject = motion_.get(i);
            for (int j = 0 ; j< tempObject.size(); j++)
            {
                double tempValue = (double) tempObject.get(j);
                double tempMaxMotion = maxMotion_[j];
                double tempsecondMax = secondMaxMotion_[j];
                if(tempValue < 0)
                {
                    tempValue = tempValue * -1;
                }
                if(tempMaxMotion < 0)
                {
                    tempMaxMotion = tempMaxMotion * -1;
                }
                if(tempsecondMax < 0)
                {
                    tempsecondMax = tempsecondMax * -1;
                }
                if (tempValue > tempMaxMotion)
                {
                    secondMaxMotion_[j] = maxMotion_[j];
                    maxMotion_[j] = (double)tempObject.get(j);
                }
                if(tempValue > tempsecondMax && tempValue < tempMaxMotion)
                {
                    secondMaxMotion_[j] = (double)tempObject.get(j);
                }
            }
        }
        setDirectionOfMovement();
        findMinimumOccurence();
        Log.d("testing sposition", "x " + Double.toString((Double) startingPostion_.get(0).get(1)) + " y " + Double.toString((Double) startingPostion_.get(0).get(1)) + " z " + Double.toString((Double) startingPostion_.get(0).get(2)));
        Log.d("testing maxmotion", (" x  " + Double.toString(maxMotion_[0]) + "Y " + Double.toString(maxMotion_[1]) + "z " + Double.toString(maxMotion_[2])));
        Log.d("testing SecondMax", (" x  " + Double.toString(secondMaxMotion_[0]) + "Y " + Double.toString(secondMaxMotion_[1]) + "z " + Double.toString(secondMaxMotion_[2])));
        Log.d("testing", " hello");
    }
    private void findMaxAngle() {



        minAngle_[0] = minAngle_[1] = minAngle_[2] = 180;
        for(int i = 0; i < angle_.size(); i++)
        {
            Vector tempObject = angle_.get(i);
            for (int j = 0 ; j< tempObject.size(); j++)
            {
                double tempValue = (double) tempObject.get(j);
                double tempMaxAngle = maxAngle_[j];
                double tempsecondMaxAngle = minAngle_[j];
                if(tempValue < 0)
                {
                    tempValue = tempValue * -1;
                }
                if(tempMaxAngle < 0)
                {
                    tempMaxAngle = tempMaxAngle * -1;
                }
                if(tempsecondMaxAngle < 0)
                {
                    tempsecondMaxAngle = tempsecondMaxAngle * -1;
                }
                if (tempValue > tempMaxAngle)
                {
                    //minAngle_[j] = maxAngle_[j];
                    maxAngle_[j] = (double)tempObject.get(j);
                }
                if(tempValue < tempsecondMaxAngle)
                {
                    minAngle_[j] = (double)tempObject.get(j);
                }
            }
        }
        Log.d("pinch stanlge", "x " + Double.toString((Double) startingAngle_.get(0).get(0))+" y " + Double.toString((Double) startingAngle_.get(0).get(1))+" z " + Double.toString((Double) startingAngle_.get(0).get(2)));
        Log.d("pinch endanlge", "x " + Double.toString((Double) endingAngle_.get(0).get(0)) + " y " + Double.toString((Double) endingAngle_.get(0).get(1)) + " z " + Double.toString((Double) endingAngle_.get(0).get(2)));
        Log.d("pinch maxAngle", (" x  " + Double.toString(maxAngle_[0]) + " Y " + Double.toString(maxAngle_[1]) + " z " + Double.toString(maxAngle_[2])));
        Log.d("pinch minAngle", (" x  " + Double.toString(minAngle_[0]) + " Y " + Double.toString(minAngle_[1]) + " z " + Double.toString(minAngle_[2])));
        differenceOfAngle_[0] = differenceOfAngle_[1] = differenceOfAngle_ [2] = 0;
        for(int i = 0 ; i < 3 ; i ++)
        {
            double temp1 = (double)startingAngle_.get(0).get(i) - maxAngle_[i];
            double temp2 = (double)startingAngle_.get(0).get(i) - minAngle_[i];
            if(temp1 < 0)
                temp1 = temp1 * -1;
            if (temp2 < 0)
                temp2 = temp2 * -1;
            if(temp1 > temp2)
            {
                differenceOfAngle_[i] = temp1;
            }
            else
            {
                differenceOfAngle_[i] = temp2;
            }
        }
        Log.d("pinch answ", (" x  " + Double.toString(differenceOfAngle_[0]) + " Y " + Double.toString(differenceOfAngle_[1]) + " z " + Double.toString(differenceOfAngle_[2])));
        Log.d("pinch","pinch end" );


    }
    private void setDirectionOfMovement()
    {
        double max = 0;
        double maxX = maxMotion_[0];
        double maxZ = maxMotion_[2];
        for(int i = 0; i < 3; i++)
        {
            double value = maxMotion_[i];
            if(value < 0)
            {
                value = value * -1;
            }
            if(value > max)
            {
                directionOfMovement_ = i;
                max = value;
            }
        }
        if(maxX <= -7 & maxZ <= -7)
        {
            directionOfMovement_ = 0;
            Log.d("testing", "I'm setting");
        }
        findMinimumOccurence();
        Log.d("testing direction", Integer.toString(directionOfMovement_));
    }

    private void findMinimumOccurence() {

        boolean found = false;
        int foundAtIndex = 0;
        tapCounter_ = 0;
        double tempMaxMotion = secondMaxMotion_[directionOfMovement_];
        String test = "  max " + Double.toString((tempMaxMotion)) ;
        double fraction = 2 + directionOfMovement_ % 2;
        double doubleTappValue = 0;
        if(tempMaxMotion <0 )
        {
            doubleTappValue = tempMaxMotion + fraction;
        }
        else
        {
            doubleTappValue = tempMaxMotion - fraction;
        }
        Log.d("sampling", "DV " +Double.toString(doubleTappValue));

        if (motion_.size() <110)
        {
            tapCounter_ = 1;
        }
        else
        {
            for(int i = 0; i < motion_.size(); i++)
            {
                Vector tempObject = motion_.get(i);

                double tempValue = (double) tempObject.get(directionOfMovement_);


                test = test + ": " + Double.toString((double) Math.round(tempValue * 100) / 100);
                int indexDifference = i -foundAtIndex;
                if(indexDifference < 0)
                {
                    indexDifference = indexDifference * -1;
                }
                if (tempValue  == tempMaxMotion && found == false)
                {
                    Log.d("sampling", " sm " + Double.toString(tempValue) + " == " + Double.toString(tempMaxMotion) + "i = " + Integer.toString(i));
                    found = true;
                    tapCounter_++;
                    foundAtIndex = i;
                    i= 0;

                }
                else if(doubleTappValue >= tempValue & tempMaxMotion < 0 & indexDifference>5 & found ==true )
                {
                    Log.d("sampling", " sm " + Double.toString(doubleTappValue) + ">=" + Double.toString(tempValue) + "diff = " + Integer.toString(indexDifference));
                    tapCounter_++;
                    foundAtIndex= i;

                }
                else if(doubleTappValue <= tempValue & tempMaxMotion > 0 & indexDifference>5 & found ==true)
                {
                    Log.d("sampling", " sm " + Double.toString(doubleTappValue) + "<=" + Double.toString(tempValue) + "diff= " + Integer.toString(indexDifference));
                    tapCounter_++;
                    foundAtIndex= i;

                }

            }
            Log.d("sampling ", ("counter " + Integer.toString(tapCounter_) + test));
        }


    }

    private double findDifference(double value1, double value2)
    {
        double difference = value1 - value2;
        if(difference < 0)
        {
            difference = difference * -1;
        }
        return difference;

    }

    private void showGestureMessage(String gesture)
    {

        _interfaceObject.GestureType(gesture);
    }


    private void shakeDetection(double x, double y, double z) {
        long curTime = System.currentTimeMillis();


        if ((curTime - lastUpdate) > 100) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            float speed = (float) (Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000);

            if (speed > SHAKE_THRESHOLD) {
                Log.d("Value_shake", "shake happend");
                if(motionDetected_)
                {


                }
            }
            last_x = (float) x;
            last_y = (float) y;
            last_z = (float) z;
        }
    }
public void resettingForShake(){

    lastUpdate1 = System.currentTimeMillis();
    draggingObject_ = !draggingObject_;

    _initialValues[0]= angleXForDrage_ = (Double) endingAngle_.get(0).get(0);
    _initialValues[1]=angleYForDrage_ = (Double) endingAngle_.get(0).get(1);
    _initialValues[2]=angleZForDrage_ = (Double) endingAngle_.get(0).get(2);

}


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

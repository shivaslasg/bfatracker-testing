package sg.onemap.bfatracker.controllers

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import com.mapbox.mapboxsdk.geometry.LatLng
import com.navisens.motiondnaapi.MotionDna
import com.navisens.motiondnaapi.MotionDnaApplication
import com.navisens.motiondnaapi.MotionDnaInterface
import sg.onemap.bfatracker.MainActivity
import sg.onemap.bfatracker.interfaces.DrawTrackerListener

class MotionDNAController(var activity: MainActivity,
                          var pkg: PackageManager,
                          var motiondnaid: String,
                          var mListener: DrawTrackerListener) : MotionDnaInterface {
    var motionDnaApplication: MotionDnaApplication? = null

    var selectedinterval = 1000.0
    var TAG : String = "MotionDNAController"
    var CURRENT_MOTIONDNA : Int = 0
    var RECORDING_MOTIONDNA: Int = 1
    var PAUSE_MOTIONDNA: Int = 2
    var STOPPED_MOTIONDNA: Int = 3

    val intervalItems = arrayOf("0.5", "1", "2")

    init{
        try {
            initMotionDna()
        } catch (ex:Exception) {
            Log.e(TAG, "Error occurred in LocationUtility init : "+ex.message.toString())
        }
    }

    override fun getAppContext(): Context {
        return activity
    }

    fun initMotionDna() {
        motionDnaApplication = MotionDnaApplication(this)
        motionDnaApplication!!.runMotionDna(motiondnaid)
        //motionDnaApplication!!.setLocationNavisens()
        motionDnaApplication!!.setExternalPositioningState(MotionDna.ExternalPositioningState.HIGH_ACCURACY)
        motionDnaApplication!!.setPowerMode(MotionDna.PowerConsumptionMode.PERFORMANCE)
        //motionDnaApplication!!.startUDP()
        motionDnaApplication!!.setBinaryFileLoggingEnabled(true)
        motionDnaApplication!!.setCallbackUpdateRateInMs(selectedinterval)
        motionDnaApplication!!.setBackpropagationEnabled(true)
        motionDnaApplication!!.setBackpropagationBufferSize(2000)
    }

    fun fixMotionDnaLocation(location: LatLng){
        try {
            motionDnaApplication!!.setLocationLatitudeLongitude(
                location.latitude,
                location.longitude
            )
            Toast.makeText(activity, "Fixing location @ latitude : "+location.latitude+", longitude: "+location.longitude, Toast.LENGTH_SHORT).show()
            mListener.addTrackRecord(location, 0.0)
        } catch (ex : Exception) {
            Log.e(TAG, ex.toString())
        }
    }

    fun fixMotionDnaLocationWithBearing(location: LatLng, bearings: Float){
        try {
            motionDnaApplication!!.setLocationLatitudeLongitudeAndHeadingInDegrees(
                                    location.latitude,
                                    location.longitude,
                                    bearings.toDouble())
            Toast.makeText(activity, "Fixing location @ latitude : "+location.latitude+", longitude: "+location.longitude+", with bearings (in degrees) :"+bearings, Toast.LENGTH_SHORT).show()
            mListener.addTrackRecord(location, bearings.toDouble())
        } catch (ex : Exception) {
            Log.e(TAG, ex.toString())
        }
    }

    fun startMotionDna(){
        try {
            //motionDnaApplication!!.runMotionDna(motiondnaid)
            CURRENT_MOTIONDNA = RECORDING_MOTIONDNA
            Log.i(TAG, "startMotionDna")
        } catch (ex : Exception) {
            Log.e(TAG, ex.toString())
        }
    }

    fun stopMotionDna(){
        try {
            motionDnaApplication!!.stop()
            CURRENT_MOTIONDNA = STOPPED_MOTIONDNA
            //mListener.updateEndTrackRecord(location, 0.0)
            Log.i(TAG, "stopMotionDna")
        } catch (ex : Exception) {
            Log.e(TAG, ex.toString())
        }
    }

    fun pauseMotionDna(){
        try {
            motionDnaApplication!!.pause()
            CURRENT_MOTIONDNA = PAUSE_MOTIONDNA
            Log.i(TAG, "pauseMotionDna")
        } catch (ex : Exception) {
            Log.e(TAG, ex.toString())
        }
    }

    fun resumeMotionDna(){
        try {
            motionDnaApplication!!.resume()
            CURRENT_MOTIONDNA = RECORDING_MOTIONDNA
            Log.i(TAG, "resumeMotionDna")

        } catch (ex : Exception) {
            Log.e(TAG, ex.toString())
        }
    }

    override fun receiveNetworkData(p0: MotionDna?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun receiveNetworkData(p0: MotionDna.NetworkCode?, p1: MutableMap<String, out Any>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun receiveMotionDna(motionDna: MotionDna) {
        //val str = "**********Navisens MotionDna Location Data***********\n"
        /*val location:MotionDna.Location = motionDna.location
        var lat:Double = location.globalLocation.latitude
        var lng:Double = location.globalLocation.longitude

        str += "lat: " + lat + " \n"
        str += "lng: " + lng + " \n"*/

        //str += "Lat from getLocation().globallocation : " + motionDna.getLocation().globalLocation.latitude + " Lon from getLocation().longitude: " + motionDna.getLocation().globalLocation.longitude + "\n";
        //str += "Lat from getLocation().globallocation : " + motionDna.getLocation().globalLocation.latitude + " Lon from getLocation().longitude: " + motionDna.getLocation().globalLocation.longitude + "\n";
        //val location: XYZ = motionDna?.getLocation()!!.localLocation
        //str += "X: " + location.x + " \n"
        //str += "Y: " + location.y + " \n"
        //str += "Z: " + location.z + " \n"
        //str += String.format(" (%.2f, %.2f, %.2f)\n",location.x, location.y, location.z);
        //str += "Hdg: " + motionDna.getLocation().heading +  " \n";
        //str += String.format(" (%.2f, %.2f, %.2f)\n",location.x, location.y, location.z);
//str += "Hdg: " + motionDna.getLocation().heading +  " \n";

        /* str += "getMotionStatistics dwelling : " + motionDna.getMotionStatistics().dwelling +  " \n";
        str += "getMotionStatistics stationary : " + motionDna.getMotionStatistics().stationary +  " \n";
        str += "getMotionStatistics walking : " + motionDna.getMotionStatistics().walking +  " \n"; */

        /* str += "getMotionStatistics dwelling : " + motionDna.getMotionStatistics().dwelling +  " \n";
        str += "getMotionStatistics stationary : " + motionDna.getMotionStatistics().stationary +  " \n";
        str += "getMotionStatistics walking : " + motionDna.getMotionStatistics().walking +  " \n"; */

        //str += "getAttitude pitch: " + motionDna.getAttitude().pitch + " \n"
        //str += "getAttitude roll: " + motionDna.getAttitude().roll + " \n"
        //str += "getAttitude yaw: " + motionDna.getAttitude().yaw + " \n"
        //str += "getTimestamp : " + motionDna.getTimestamp() + " \n"

       // str += "getGpsLocation().globalLocation.latitude : " + motionDna.getGpsLocation().globalLocation.latitude + " \n"
       // str += "getGpsLocation().globalLocation.longitude : " + motionDna.getGpsLocation().globalLocation.longitude + " \n"



        //str += "getGpsLocation().locationStatus : " + motionDna.getGpsLocation().locationStatus +  " \n";
        //str += "getLocation().verticalMotionStatus : " + motionDna.getLocation().verticalMotionStatus +  " \n";
        //str += "getGpsLocation().verticalMotionStatus : " + motionDna.getGpsLocation().verticalMotionStatus +  " \n";

        //str += "getGpsLocation().locationStatus : " + motionDna.getGpsLocation().locationStatus +  " \n";
//str += "getLocation().verticalMotionStatus : " + motionDna.getLocation().verticalMotionStatus +  " \n";
//str += "getGpsLocation().verticalMotionStatus : " + motionDna.getGpsLocation().verticalMotionStatus +  " \n";

       // motionDnaApplication!!.sendUDPPacket(str)

        //str += "getTimestamp : " + motionDna.getTimestamp() + " \n"
        //str += "motionType: " + motionDna.getMotion().motionType + "\n"
        //str += "Lat from getGpsLocation().globallocation : " + motionDna.gpsLocation.globalLocation.latitude + "\n "
        //str += "Lon from getGpsLocation().longitude: " + motionDna.gpsLocation.globalLocation.longitude + "\n"

        if(CURRENT_MOTIONDNA == RECORDING_MOTIONDNA) {

            //Log.i(TAG, str)
            //motionDna.location.globalLocation.latitude
            val drawLatLng: LatLng = LatLng(
                motionDna.location.globalLocation.latitude,
                motionDna.location.globalLocation.longitude
            )


            if (motionDna.location.globalLocation.latitude == 0.0
                && motionDna.location.globalLocation.longitude == 0.0
            ) {
                Toast.makeText(
                    activity,
                    "Data received, latitude : " + drawLatLng.latitude + ", longitude: " + drawLatLng.longitude,
                    Toast.LENGTH_SHORT
                ).show()
            }

            //Toast.makeText(activity, "Data received, latitude : "+drawLatLng.latitude+", longitude: "+drawLatLng.longitude, Toast.LENGTH_SHORT).show()
            //mListener.drawLinesFromTracker(drawLatLng)
            mListener.drawSymbolFromTracker(drawLatLng)
        }
    }

    override fun reportError(errorCode: MotionDna.ErrorCode?, s: String?) {
        when (errorCode) {
            MotionDna.ErrorCode.ERROR_AUTHENTICATION_FAILED -> println("Error: authentication failed $s")
            MotionDna.ErrorCode.ERROR_SDK_EXPIRED -> println("Error: SDK expired $s")
            MotionDna.ErrorCode.ERROR_PERMISSIONS -> println("Error: permissions not granted $s")
            MotionDna.ErrorCode.ERROR_SENSOR_MISSING -> println("Error: sensor missing $s")
            MotionDna.ErrorCode.ERROR_SENSOR_TIMING -> println("Error: sensor timing $s")
        }
    }

    override fun getPkgManager(): PackageManager {
        return this.pkg
    }

    fun getCurrentStateForMotionDNA() : Int {
        return CURRENT_MOTIONDNA
    }

    fun setInterval(secondsInStr : String){
        try {
            selectedinterval = secondsInStr.toDouble() * 1000
            motionDnaApplication!!.setCallbackUpdateRateInMs(selectedinterval)
        } catch(ex: Exception) {

        }
    }

}
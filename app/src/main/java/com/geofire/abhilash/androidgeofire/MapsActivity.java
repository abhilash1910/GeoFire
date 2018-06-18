package com.geofire.abhilash.androidgeofire;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.GeolocationPermissions;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {
 private static final int MY_PERMISSION_REQUEST_CODE=7192;
 private static final int PLAY_SERVICE_RESOLUTION_REQUEST=300193;
 private GoogleApiClient googleApiClient;
 private Location lastlocation;
private LocationRequest locationRequest;

private static int UPDATE_INTERVAL=5000;
private static int FASTEST_INTERVAL=3000;
private static int DISPLACEMENT=10;
DatabaseReference ref;
GeoFire geoFire;
Marker mMarker;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
       ref = FirebaseDatabase.getInstance().getReference("Youir Location");
       geoFire=new GeoFire(ref);

        setuplocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

      switch(requestCode)
      {
          case MY_PERMISSION_REQUEST_CODE:
              if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
              {
                  if(checkgoogleplayservices())
                  {
                      buildapiclient();
                      createlocationrequest();
                      displaylocation();

                  }



              }
              break;


      }



    }

    private void setuplocation() {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
               ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {

            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);





        }
        else
        {
            if(checkgoogleplayservices())
            {
                    buildapiclient();
                    createlocationrequest();
                    displaylocation();

            }


        }

    }

    private void displaylocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
           return;
        }
        lastlocation =LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if(lastlocation !=null)
        {
            final double lati=lastlocation.getLatitude();
            final double longi= lastlocation.getLongitude();
            Toast.makeText(MapsActivity.this,lati + " " +longi,Toast.LENGTH_SHORT).show();
                geoFire.setLocation("Your Location", new GeoLocation(lati, longi)
                        ,new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                Toast.makeText(MapsActivity.this,"Your location",Toast.LENGTH_SHORT).show();
                                LatLng latLng=new LatLng(lati,longi);


                                   mMarker= mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title("Your Location"));

                                         mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10.5f));

                            }
                        });




            Log.d("ABHILASH",String.format("Your location was changes: %f/%f",lati,longi));
        }
        else
        {
            Log.d("ABHILASH",String.format("Cannot detect location changes"));
        }




    }

    private void createlocationrequest() {

        locationRequest =new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void buildapiclient() {

        googleApiClient=new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    private boolean checkgoogleplayservices() {

    int resultcode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    if(resultcode != ConnectionResult.SUCCESS)
    {
        if(GooglePlayServicesUtil.isUserRecoverableError(resultcode))
        {

            GooglePlayServicesUtil.getErrorDialog(resultcode,this,PLAY_SERVICE_RESOLUTION_REQUEST);

        }
        else
        {
            Toast.makeText(MapsActivity.this,"Device is not supported",Toast.LENGTH_SHORT).show();
              finish();
        }
               return false;

    }
    return true;


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
  /* LatLng latLng=new LatLng(33.8688,151.2093);
        mMap.addMarker(new MarkerOptions().position(latLng).title("Sydney"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10f));
*/



        LatLng dangerousarea=new LatLng(22.6745133, 88.3444496);
        mMap.addCircle(new CircleOptions()
        .center(dangerousarea)
                .radius(500)
                .strokeColor(Color.BLUE)
                .strokeWidth(5.0f)
                .fillColor(0x220000FF)
        );


        LatLng yashwanthpur= new LatLng(23.5476752,87.2920124);
        mMap.addCircle(new CircleOptions().fillColor(0x220000FF).center(yashwanthpur).radius(500).strokeWidth(Color.GREEN).strokeWidth(5.0f));

        GeoQuery geoQuery=geoFire.queryAtLocation(new GeoLocation(dangerousarea.latitude,dangerousarea.longitude),0.5f);
       GeoQuery geoQuery1 =geoFire.queryAtLocation(new GeoLocation(yashwanthpur.latitude,yashwanthpur.longitude),0.5f);

           geoQuery1.addGeoQueryEventListener(new GeoQueryEventListener() {
               @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
               @Override
               public void onKeyEntered(String key, GeoLocation location) {
                   Toast.makeText(MapsActivity.this,"Entered Durgapur",Toast.LENGTH_LONG).show();

                   sendnotification("Abhilash",String.format("%s Entered Durgapur",key));
               }

               @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
               @Override
               public void onKeyExited(String key) {
                   Toast.makeText(MapsActivity.this,"Exited Durgapur",Toast.LENGTH_LONG).show();

                   sendnotification("Abhilash",String.format("%s Exited Durgapur",key));
               }

               @Override
               public void onKeyMoved(String key, GeoLocation location) {
                   Toast.makeText(MapsActivity.this,"Moved in Durgapur area",Toast.LENGTH_LONG).show();
                   // Log.d("MOVE",String.format("%s Moved inside dangerous area %f/%f",key,location.latitude,location.longitude));
               }

               @Override
               public void onGeoQueryReady() {

               }

               @Override
               public void onGeoQueryError(DatabaseError error) {

                   Log.e("Error",""+error);
               }
           });

       geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Toast.makeText(MapsActivity.this,"Entered Home",Toast.LENGTH_LONG).show();

                sendnotification("Abhilash",String.format("%s Entered Home",key));
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onKeyExited(String key) {
                Toast.makeText(MapsActivity.this,"Exited Home",Toast.LENGTH_LONG).show();

                sendnotification("Abhilash",String.format("%s Exited Home",key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Toast.makeText(MapsActivity.this,"Moved in Home area",Toast.LENGTH_LONG).show();
                // Log.d("MOVE",String.format("%s Moved inside dangerous area %f/%f",key,location.latitude,location.longitude));
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

                 Log.e("Error",""+error);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void sendnotification(String abhilash, String content) {

        Notification.Builder builder=new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(abhilash)
                .setContentText(content);
        NotificationManager manager=(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent=new Intent(this,MapsActivity.class);
        PendingIntent contentintent=PendingIntent.getActivities(this,0, new Intent[]{intent},PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentintent);
        Notification notification= builder.build();
      notification.flags |=  Notification.FLAG_AUTO_CANCEL;
      notification.defaults |= Notification.DEFAULT_SOUND;
      manager.notify(new Random().nextInt(),notification);


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
 displaylocation();
 startlocationupdates();
    }

    private void startlocationupdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED )
        {
            return;

        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);


    }

    @Override
    public void onConnectionSuspended(int i) {
googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
       lastlocation =location;
       displaylocation();
    }
}

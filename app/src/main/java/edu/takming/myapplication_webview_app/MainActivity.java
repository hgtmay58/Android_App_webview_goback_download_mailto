package edu.takming.myapplication_webview_app;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
WebView w;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        w=(WebView) findViewById(R.id.web);
        w.getSettings().setJavaScriptEnabled(true);
        w.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        w.setWebViewClient(new WebViewClient(){
            //
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                super.shouldOverrideUrlLoading(view, url);
                if ( url.startsWith("tel:")) {
                    Intent tel = new Intent(Intent.ACTION_DIAL, Uri.parse( url));
                    startActivity(tel);
                    return true;
                }
                else if (url.contains("mailto:")) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;

                }else {
                    view.loadUrl(url);
                    return true;
                }
            }
            //
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if(view.getContentHeight()==0)
                    view.reload();
                else
                    super.onPageFinished(view, url);

            }
        });
        //
        w.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, final String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Log.v("TAG","Permission is require");
                //
                //Requesting Permissions at Run Time
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    Log.v("TAG","Permission is not granted");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                }
                else
                    {
                        downloadDialog(url,userAgent,contentDisposition,mimetype);
                    }

                //

            }
        });
        //
        w.loadUrl("https://www.takming.edu.tw/schtm/sch_report.asp?PageNo=1&category=3#about");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && w.canGoBack()) {
            w.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    //
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "請再次點選下載(Click again to download)", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "需要下載儲存權限(Requesting Permissions)", Toast.LENGTH_LONG).show();
        }
        //
    }
    //
    public void downloadDialog(final String url,final String userAgent,String contentDisposition,String mimetype)
    {
        //getting filename from url.
        final String filename = URLUtil.guessFileName(url,contentDisposition,mimetype);
        //alertdialog
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        //title of alertdialog
        builder.setTitle("Download");
        //message of alertdialog
        builder.setMessage("Do you want to save " +filename);
        //if Yes button clicks.
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //DownloadManager.Request created with url.
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                //cookie
                String cookie= CookieManager.getInstance().getCookie(url);
                //Add cookie and User-Agent to request
                request.addRequestHeader("Cookie",cookie);
                request.addRequestHeader("User-Agent",userAgent);
                //file scanned by MediaScannar
                request.allowScanningByMediaScanner();
                //Download is visible and its progress, after completion too.
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                //DownloadManager created
                DownloadManager downloadManager=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                //Saving files in Download folder
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                //download enqued
                downloadManager.enqueue(request);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //cancel the dialog if Cancel clicks
                dialog.cancel();
            }
        });
        //alertdialog shows.
        builder.create().show();
    }
    //
}
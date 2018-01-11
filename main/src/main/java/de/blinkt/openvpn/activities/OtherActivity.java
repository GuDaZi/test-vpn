/*
 * Copyright (c) 2012-2018 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.blinkt.openvpn.R;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.IServiceStatus;
import de.blinkt.openvpn.core.OpenVPNStatusService;
import de.blinkt.openvpn.core.PasswordCache;
import de.blinkt.openvpn.core.Preferences;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;
import de.blinkt.openvpn.fragments.Utils;
import de.blinkt.openvpn.views.FileSelectLayout;

/**
 * Created by TJTJL on 2018/1/10
 */

public class OtherActivity extends Activity {

    private static final String TAG = "OtherActivity";
    private VpnProfile mResult;
    private String mEmbeddedPwFile;
    private String mAliasName = null;
    private Map<Utils.FileType, FileSelectLayout> fileSelectMap = new HashMap<>();
    private transient List<String> mPathsegments;
    private VpnProfile mSelectedProfile;
    private static final int START_VPN_PROFILE = 70;
    private boolean mhideLog = false;
    private boolean mCmfixed = false;
    private String mTransientAuthPW;
    private String mTransientCertOrPCKS12PW;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_other);

        Button openVpn = findViewById(R.id.straight);
        openVpn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AssetManager assetManager = getAssets();
                try {
                    String[] list = assetManager.list("");
                    InputStream open = assetManager.open("bigdata.ovpn");
                    doImport(open);
                    if (mResult != null){
                        Log.e(TAG, "UUID: " + mResult.getUUID().toString());
                        saveProfile();
//                        launchVPN();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button toMain = findViewById(R.id.door);
        toMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    private void doImport(InputStream is) {
        ConfigParser cp = new ConfigParser();
        try {
            InputStreamReader isr = new InputStreamReader(is);

            cp.parseConfig(isr);
            mResult = cp.convertProfile();
            embedFiles(cp);
            return;

        } catch (IOException | ConfigParser.ConfigParseError e) {
//            log(R.string.error_reading_config_file);
//            log(e.getLocalizedMessage());
        }
        mResult = null;
    }

    /**
     * 保存VPNProfile
     */
    private void saveProfile() {
        Intent result = new Intent();
        ProfileManager vpl = ProfileManager.getInstance(this);

        if (!TextUtils.isEmpty(mEmbeddedPwFile))
            ConfigParser.useEmbbedUserAuth(mResult, mEmbeddedPwFile);

        vpl.addProfile(mResult);
        vpl.saveProfile(this, mResult);
        vpl.saveProfileList(this);

        result.putExtra(VpnProfile.EXTRA_PROFILEUUID, mResult.getUUID().toString());
        Log.e(TAG, "mResult.getUUID().toString(): " + mResult.getUUID().toString());
//        setResult(Activity.RESULT_OK, result);
//        finish();
        launchVPN();
    }

    /**
     * 开启VPN功能
     */
    void launchVPN() {
        mSelectedProfile = mResult;
        //VPNProfile
        int vpnok = mSelectedProfile.checkProfile(this);
        if (vpnok != R.string.no_error_found) {
            showConfigErrorDialog(vpnok);
            return;
        }

        Intent intent = VpnService.prepare(this);
        // Check if we want to fix /dev/tun
        SharedPreferences prefs = Preferences.getDefaultSharedPreferences(this);
        boolean usecm9fix = prefs.getBoolean("useCM9Fix", false);
        boolean loadTunModule = prefs.getBoolean("loadTunModule", false);

        if (loadTunModule)
            execeuteSUcmd("insmod /system/lib/modules/tun.ko");

        if (usecm9fix && !mCmfixed) {
            execeuteSUcmd("chown system /dev/tun");
        }

        if (intent != null) {
            VpnStatus.updateStateString("USER_VPN_PERMISSION", "", R.string.state_user_vpn_permission,
                    ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
            // Start the query
            try {
                Log.e(TAG, "START_VPN_PROFILE: " + START_VPN_PROFILE );

//                startActivityForResult(intent, START_VPN_PROFILE);
            } catch (ActivityNotFoundException ane) {
                // Shame on you Sony! At least one user reported that
                // an official Sony Xperia Arc S image triggers this exception
                VpnStatus.logError(R.string.no_vpn_support_image);
//                showLogWindow();
            }
        } else {
            Log.e(TAG, "launchVPN: error ??" );
//            onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);
            doOpenVpn();
        }

    }

    /**
     * 开启OpenVpn
     */
    private void doOpenVpn() {
        int needpw = mSelectedProfile.needUserPWInput(mTransientCertOrPCKS12PW, mTransientAuthPW);
        if (needpw != 0) {
            VpnStatus.updateStateString("USER_VPN_PASSWORD", "", R.string.state_user_vpn_password,
                    ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
            askForPW(needpw);
        } else {
            Log.e(TAG, "True entrance: " );
            SharedPreferences prefs = Preferences.getDefaultSharedPreferences(this);
            boolean showLogWindow = prefs.getBoolean("showlogwindow", true);

            if (!mhideLog && showLogWindow)
//                showLogWindow();

            //HEDA
            ProfileManager.updateLRU(this, mSelectedProfile);

            VPNLaunchHelper.startOpenVpn(mSelectedProfile, getBaseContext());
//            finish();
        }
    }

    private void askForPW(final int type) {

        final EditText entry = new EditText(this);

        entry.setSingleLine();
        entry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        entry.setTransformationMethod(new PasswordTransformationMethod());

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.pw_request_dialog_title, getString(type)));
        dialog.setMessage(getString(R.string.pw_request_dialog_prompt, mSelectedProfile.mName));

        @SuppressLint("InflateParams") final View userpwlayout = getLayoutInflater().inflate(R.layout.userpass, null, false);

        if (type == R.string.password) {
            ((EditText) userpwlayout.findViewById(R.id.username)).setText(mSelectedProfile.mUsername);
            ((EditText) userpwlayout.findViewById(R.id.password)).setText(mSelectedProfile.mPassword);
            ((CheckBox) userpwlayout.findViewById(R.id.save_password)).setChecked(!TextUtils.isEmpty(mSelectedProfile.mPassword));
            ((CheckBox) userpwlayout.findViewById(R.id.show_password)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        ((EditText) userpwlayout.findViewById(R.id.password)).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    else
                        ((EditText) userpwlayout.findViewById(R.id.password)).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            });

            dialog.setView(userpwlayout);
        } else {
            dialog.setView(entry);
        }

        AlertDialog.Builder builder = dialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (type == R.string.password) {
                            mSelectedProfile.mUsername = ((EditText) userpwlayout.findViewById(R.id.username)).getText().toString();

                            String pw = ((EditText) userpwlayout.findViewById(R.id.password)).getText().toString();
                            if (((CheckBox) userpwlayout.findViewById(R.id.save_password)).isChecked()) {
                                mSelectedProfile.mPassword = pw;
                            } else {
                                mSelectedProfile.mPassword = null;
                                mTransientAuthPW = pw;
                            }
                        } else {
                            mTransientCertOrPCKS12PW = entry.getText().toString();
                        }
                        Intent intent = new Intent(OtherActivity.this, OpenVPNStatusService.class);
                        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                    }

                });
        dialog.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        VpnStatus.updateStateString("USER_VPN_PASSWORD_CANCELLED", "", R.string.state_user_vpn_password_cancelled,
                                ConnectionStatus.LEVEL_NOTCONNECTED);
                        finish();
                    }
                });

        dialog.create().show();

    }

    private void execeuteSUcmd(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder("su", "-c", command);
            Process p = pb.start();
            int ret = p.waitFor();
            if (ret == 0)
                mCmfixed = true;
        } catch (InterruptedException | IOException e) {
            VpnStatus.logException("SU command", e);
        }
    }

    void embedFiles(ConfigParser cp) {
        // This where I would like to have a c++ style
        // void embedFile(std::string & option)

        if (mResult.mPKCS12Filename != null) {
            File pkcs12file = findFileRaw(mResult.mPKCS12Filename);
            if (pkcs12file != null) {
                mAliasName = pkcs12file.getName().replace(".p12", "");
            } else {
                mAliasName = "Imported PKCS12";
            }
        }

        mResult.mCaFilename = embedFile(mResult.mCaFilename, Utils.FileType.CA_CERTIFICATE, false);
        mResult.mClientCertFilename = embedFile(mResult.mClientCertFilename, Utils.FileType.CLIENT_CERTIFICATE, false);
        mResult.mClientKeyFilename = embedFile(mResult.mClientKeyFilename, Utils.FileType.KEYFILE, false);
        mResult.mTLSAuthFilename = embedFile(mResult.mTLSAuthFilename, Utils.FileType.TLS_AUTH_FILE, false);
        mResult.mPKCS12Filename = embedFile(mResult.mPKCS12Filename, Utils.FileType.PKCS12, false);
        mResult.mCrlFilename = embedFile(mResult.mCrlFilename, Utils.FileType.CRL_FILE, true);
        if (cp != null) {
            mEmbeddedPwFile = cp.getAuthUserPassFile();
            mEmbeddedPwFile = embedFile(cp.getAuthUserPassFile(), Utils.FileType.USERPW_FILE, false);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            IServiceStatus service = IServiceStatus.Stub.asInterface(binder);
            try {
                if (mTransientAuthPW != null)

                    service.setCachedPassword(mSelectedProfile.getUUIDString(), PasswordCache.AUTHPASSWORD, mTransientAuthPW);
                if (mTransientCertOrPCKS12PW != null)
                    service.setCachedPassword(mSelectedProfile.getUUIDString(), PasswordCache.PCKS12ORCERTPASSWORD, mTransientCertOrPCKS12PW);

                onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);

            } catch (RemoteException e) {
                e.printStackTrace();
            }

            unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private String embedFile(String filename, Utils.FileType type, boolean onlyFindFileAndNullonNotFound) {
        if (filename == null)
            return null;

        // Already embedded, nothing to do
        if (VpnProfile.isEmbedded(filename))
            return filename;

        File possibleFile = findFile(filename, type);
        if (possibleFile == null)
            if (onlyFindFileAndNullonNotFound)
                return null;
            else
                return filename;
        else if (onlyFindFileAndNullonNotFound)
            return possibleFile.getAbsolutePath();
        else
            return readFileContent(possibleFile, type == Utils.FileType.PKCS12);

    }

    private File findFileRaw(String filename) {
        if (filename == null || filename.equals(""))
            return null;

        // Try diffent path relative to /mnt/sdcard
        File sdcard = Environment.getExternalStorageDirectory();
        File root = new File("/");

        HashSet<File> dirlist = new HashSet<>();

        for (int i = mPathsegments.size() - 1; i >= 0; i--) {
            String path = "";
            for (int j = 0; j <= i; j++) {
                path += "/" + mPathsegments.get(j);
            }
            // Do a little hackish dance for the Android File Importer
            // /document/primary:ovpn/openvpn-imt.conf


            if (path.indexOf(':') != -1 && path.lastIndexOf('/') > path.indexOf(':')) {
                String possibleDir = path.substring(path.indexOf(':') + 1, path.length());
                // Unquote chars in the  path
                try {
                    possibleDir = URLDecoder.decode(possibleDir, "UTF-8");
                } catch (UnsupportedEncodingException ignored) {}

                possibleDir = possibleDir.substring(0, possibleDir.lastIndexOf('/'));

                dirlist.add(new File(sdcard, possibleDir));

            }
            dirlist.add(new File(path));


        }
        dirlist.add(sdcard);
        dirlist.add(root);

        String[] fileparts = filename.split("/");
        for (File rootdir : dirlist) {
            String suffix = "";
            for (int i = fileparts.length - 1; i >= 0; i--) {
                if (i == fileparts.length - 1)
                    suffix = fileparts[i];
                else
                    suffix = fileparts[i] + "/" + suffix;

                File possibleFile = new File(rootdir, suffix);
                if (possibleFile.canRead())
                    return possibleFile;
            }
        }
        return null;
    }

    void showConfigErrorDialog(int vpnok) {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle(R.string.config_error_found);
        d.setMessage(vpnok);
        d.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();

            }
        });
        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            setOnDismissListener(d);
        d.show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setOnDismissListener(AlertDialog.Builder d) {
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
    }

    private File findFile(String filename, Utils.FileType fileType) {
        File foundfile = findFileRaw(filename);

        if (foundfile == null && filename != null && !filename.equals("")) {
//            log(R.string.import_could_not_open, filename);
        }
        fileSelectMap.put(fileType, null);

        return foundfile;
    }

    String readFileContent(File possibleFile, boolean base64encode) {
        byte[] filedata;
        try {
            filedata = readBytesFromFile(possibleFile);
        } catch (IOException e) {
//            log(e.getLocalizedMessage());
            return null;
        }

        String data;
        if (base64encode) {
            data = Base64.encodeToString(filedata, Base64.DEFAULT);
        } else {
            data = new String(filedata);

        }

        return VpnProfile.DISPLAYNAME_TAG + possibleFile.getName() + VpnProfile.INLINE_TAG + data;
    }
    private byte[] readBytesFromFile(File file) throws IOException {
        InputStream input = new FileInputStream(file);

        long len = file.length();
        if (len > VpnProfile.MAX_EMBED_FILE_SIZE)
            throw new IOException("File size of file to import too large.");

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) len];

        // Read in the bytes
        int offset = 0;
        int bytesRead;
        while (offset < bytes.length
                && (bytesRead = input.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += bytesRead;
        }

        input.close();
        return bytes;
    }

}

package net.kultprosvet.androidcourse.socialapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import net.kultprosvet.androidcourse.socialapp.models.Post;
import net.kultprosvet.androidcourse.socialapp.models.User;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NewPostActivity extends BaseActivity {

    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";
    private DatabaseReference mDatabase;
    private EditText mTitleField;
    private EditText mBodyField;

    private static final int RC_TAKE_VIDEO = 101;
    private static final int RC_CHOOSE_VIDEO = 102;
    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";

    private FirebaseAuth mAuth;
    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        // Initialize Firebase Auth
        mAuth = getFirebaseAuth();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        mTitleField = (EditText) findViewById(R.id.field_title);
        mBodyField = (EditText) findViewById(R.id.field_body);

        findViewById(R.id.fab_submit_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });

        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }

        uploadVideo();
    }

    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());
        grantUriPermission("net.kultprosvet.androidcourse.socialapp", fileUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child("photos")
                .child(fileUri.getLastPathSegment());
        // [END get_child_ref]

        // Upload file to Firebase Storage
        // [START_EXCLUDE]
        showProgressDialog();
        // [END_EXCLUDE]
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        photoRef.putFile(fileUri)
                .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mDownloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                        mBodyField.setText(mDownloadUrl.toString());
                        hideProgressDialog();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        mDownloadUrl = null;
                        hideProgressDialog();
                        Toast.makeText(getApplicationContext(), "Error: upload failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUI(mAuth.getCurrentUser());
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable(KEY_FILE_URI, mFileUri);
        out.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        if (data != null && requestCode == RC_CHOOSE_VIDEO && resultCode == RESULT_OK) {
            mFileUri = data.getData();
            uploadFromUri(mFileUri);
        } else if (requestCode == RC_TAKE_VIDEO && resultCode == RESULT_OK && mFileUri != null) {
            uploadFromUri(mFileUri);
        } else {
            Toast.makeText(this, "Taking picture failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadVideo() {
        final CharSequence[] items = {getString(R.string.take_video),
                getString(R.string.choose_video_from_gallary),
                getString(R.string.dialog_choose_cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(NewPostActivity.this);
        builder.setTitle(R.string.add_new_video);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(getString(R.string.take_video))) {
                    cameraIntent();
                } else if (items[item].equals(getString(R.string.choose_video_from_gallary))) {
                    galleryIntent();
                } else if (items[item].equals(getString(R.string.dialog_choose_cancel))) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/Video/mult");
        File file = new File(dir, UUID.randomUUID().toString() + ".mp4");
        try {
            // Create directory if it does not exist.
            if (!dir.exists()) {
                dir.mkdir();
            }
            boolean created = file.createNewFile();
            Log.d(TAG, "file.createNewFile:" + file.getAbsolutePath() + ":" + created);
        } catch (IOException e) {
            Log.e(TAG, "file.createNewFile" + file.getAbsolutePath() + ":FAILED", e);
        }

        // Create content:// URI for file, required since Android N
        // See: https://developer.android.com/reference/android/support/v4/content/FileProvider.html
        mFileUri = FileProvider.getUriForFile(this,
                "net.kultprosvet.androidcourse.socialapp.NewPostActivity", file);

        Intent takeVideoIntent = new Intent(android.provider.MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
        startActivityForResult(takeVideoIntent, RC_TAKE_VIDEO);
    }

    private void galleryIntent() {
        Intent takeVideoIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
        startActivityForResult(takeVideoIntent, RC_CHOOSE_VIDEO);
    }

    private void updateUI(FirebaseUser user) {
        // Download URL and Download button
        if (mDownloadUrl != null) {
            ((TextView) findViewById(R.id.field_body)).setText(mDownloadUrl.toString());
        } else {
            ((TextView) findViewById(R.id.field_body)).setText(null);
        }
    }

    private void submitPost() {
        final String title = mTitleField.getText().toString();
        final String body = mBodyField.getText().toString();

        // Title is required
        if (TextUtils.isEmpty(title)) {
            mTitleField.setError(REQUIRED);
            return;
        }

        // Body is required
        if (TextUtils.isEmpty(body)) {
            mBodyField.setError(REQUIRED);
            return;
        }

        // single_value_read]
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(NewPostActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            writeNewPost(userId, user.username, title, body);
                        }

                        // Finish this Activity, back to the stream
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
    }

    // write_fan_out]
    private void writeNewPost(String userId, String username, String title, String body) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("posts").push().getKey();
        Post post = new Post(userId, username, title, body);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            updateUI(null);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}

package net.kultprosvet.androidcourse.socialapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

import net.kultprosvet.androidcourse.socialapp.R;
import net.kultprosvet.androidcourse.socialapp.models.Post;
import net.kultprosvet.androidcourse.socialapp.viewholder.PostViewHolder;

import static net.kultprosvet.androidcourse.socialapp.Const.POSTS;

public class MainActivity extends BaseActivity {
    public static final int POSTS_QUERY_LIMIT = 100;
    private static final int ONE_LIKE = 1;
    private static final String USER_POSTS = "user-posts";
    private static final String SHARE_INTENT_TYPE = "text/plain";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpToolbar();
        showProgressDialog();
        initializeFirebase();

        findViewById(R.id.fab_add_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Button launches NewPostActivity
                startActivity(new Intent(MainActivity.this, NewPostActivity.class));
            }
        });
        showPosts();
    }

    private void initializeFirebase() {
        //get firebase auth instance
        mAuth = getFirebaseAuth();
        //get current user
        final FirebaseUser user = mAuth.getCurrentUser();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);
    }

    private void showPosts() {
        setUpRecyclerView();
        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);
        mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(Post.class, R.layout.item_post,
                PostViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final PostViewHolder viewHolder, final Post model, final int position) {
                final DatabaseReference postRef = getRef(position);
                // Set click listener for the whole post view
                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getApplicationContext(), PostDetailActivity.class);
                        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                // Determine if the current user has liked this post and set UI accordingly
                if (model.likes.containsKey(getUid())) {
                    viewHolder.likesView.setImageResource(R.drawable.ic_toggle_star_24);
                } else {
                    viewHolder.likesView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                }

                // Bind Post to ViewHolder, setting OnClickListener for the like button
                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int id = view.getId();
                        if(id == R.id.post_like) {
                            // Need to write to both places the post is stored
                            DatabaseReference globalPostRef = mDatabase.child(POSTS).child(postRef.getKey());
                            DatabaseReference userPostRef = mDatabase.child(USER_POSTS).child(model.uid).child(postRef.getKey());

                            // Run two transactions
                            onLikeClicked(globalPostRef);
                            onLikeClicked(userPostRef);
                        } else if(id == R.id.post_share) {
                            onShareClicked(model.body);
                        }
                    }
                });
                hideProgressDialog();
                findViewById(R.id.empty_list_layout).setVisibility(View.GONE);
                findViewById(R.id.list_layout).setVisibility(View.VISIBLE);
            }
        };
        if(mAdapter.getItemCount() == 0) {
            hideProgressDialog();
            findViewById(R.id.empty_list_layout).setVisibility(View.VISIBLE);
        }
        mRecycler.setAdapter(mAdapter);
    }

    private void setUpRecyclerView() {
        mRecycler = (RecyclerView) findViewById(R.id.main_posts_list);
        mRecycler.setHasFixedSize(true);
        // Set up Layout Manager, reverse layout
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        mRecycler.setLayoutManager(manager);
    }

    private void onLikeClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }
                if (p.likes.containsKey(getUid())) {
                    // unlike the post and remove self from stars
                    p.likesCount = p.likesCount - ONE_LIKE;
                    p.likes.remove(getUid());
                } else {
                    // like the post and add self to stars
                    p.likesCount = p.likesCount + ONE_LIKE;
                    p.likes.put(getUid(), true);
                }
                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
            }
        });
    }

    private void onShareClicked(String videoLink) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(SHARE_INTENT_TYPE);
        shareIntent.putExtra(Intent.EXTRA_TEXT, videoLink);
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_intent_subject));
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_intent_title)));
    }

    public String getUid() {
        if(mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        }
        return null;
    }

    public Query getQuery(DatabaseReference databaseReference) {
        // Last 100 posts, these are automatically the 100 most recent
        return databaseReference.child(POSTS)
                .limitToFirst(POSTS_QUERY_LIMIT);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
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
            mAuth.signOut();
            return true;
        } else if (i == R.id.action_user_info) {
            showInfoDialog(mAuth.getCurrentUser());
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showInfoDialog(FirebaseUser user) {
        String userEmailText = getString(R.string.user_email_text)+ user.getEmail();
        String userIdText = getString(R.string.user_id_text)+ user.getUid();
        String userInfo = userEmailText + "\n" + userIdText;
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_info_title)
                .setMessage(userInfo)
                .create()
                .show();
    }
}
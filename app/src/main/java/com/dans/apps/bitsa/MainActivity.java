package com.dans.apps.bitsa;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dans.apps.bitsa.model.User;
import com.dans.apps.bitsa.ui.BaseFragment;
import com.dans.apps.bitsa.ui.ContributionFragment;
import com.dans.apps.bitsa.ui.HomeFragment;
import com.dans.apps.bitsa.ui.ManageFragment;
import com.dans.apps.bitsa.ui.ProjectsFragment;
import com.dans.apps.bitsa.ui.SuggestionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity
        implements ValueEventListener ,BaseFragment.Callback{
    public static final String KEY_SHOW_TRANSACTIONS = "show_transactions";
    FragmentManager fragmentManager;

    DatabaseReference reference;
    BottomNavigationView navigation;
    FirebaseUser user;
    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigation = findViewById(R.id.navigation);
        navigation.getMenu().findItem(R.id.navigation_manage).setVisible(false);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        user = FirebaseAuth.getInstance().getCurrentUser();
        fragmentManager = getSupportFragmentManager();

        if(savedInstanceState==null){
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            setToolbarTitle(R.string.title_home);
            transaction.replace(R.id.fragment_container,new HomeFragment());
            transaction.commit();
        }
    }

    public Fragment getCurrentFragment(){
        return fragmentManager.findFragmentById(R.id.fragment_container);
    }


    @Override
    protected void onStart() {
        super.onStart();
        fetchCurrentUser();
    }

    @Override
    protected void onStop() {
        super.onStop();
        reference.removeEventListener(this);
    }

    private void fetchCurrentUser(){
        reference = FirebaseDatabase.getInstance().
                getReference(Constants.PATHS.users);
        reference.orderByChild("email").equalTo(user.getEmail());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                        currentUser = snapshot.getValue(User.class);
                    }

                }

                if (!isFinishing()) {
                    if (currentUser.getType() == Constants.USER_TYPE.CLUB_OFFICIAL ||
                            currentUser.getType() == Constants.USER_TYPE.SUPER_ADMIN) {
                        navigation.getMenu().findItem(R.id.navigation_manage).setVisible(true);
                    }else{
                        navigation.getMenu().findItem(R.id.navigation_manage).setVisible(false);
                    }

                    if(getCurrentFragment() instanceof ManageFragment){
                        ((ManageFragment)getCurrentFragment()).onUserFetched(currentUser);
                    }
                    if(getCurrentFragment() instanceof BaseFragment){
                        ((BaseFragment)getCurrentFragment()).onUserFetched(currentUser);
                    }

                }


            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.account){
            //todo start accounts activity
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    setToolbarTitle(R.string.title_home);
                    transaction.replace(R.id.fragment_container,new HomeFragment()).commitNow();
                    return true;
                case R.id.navigation_contribution:
                    setToolbarTitle(R.string.contribution);
                    transaction.replace(R.id.fragment_container,new ContributionFragment()).commitNow();
                    return true;
                case R.id.navigation_suggest:
                    setToolbarTitle(R.string.suggest);
                    transaction.replace(R.id.fragment_container,new SuggestionFragment()).commitNow();
                    return true;
                case R.id.navigation_projects:
                    setToolbarTitle(R.string.project_catalogue);
                    transaction.replace(R.id.fragment_container,new ProjectsFragment()).commitNow();
                    return true;
                case R.id.navigation_manage:
                    setToolbarTitle(R.string.manage);
                    transaction.replace(R.id.fragment_container,new ManageFragment()).commitNow();
                    return true;
            }
            return false;
        }
    };

    private void setToolbarTitle(@NonNull int stringRes){
        getSupportActionBar().setTitle(stringRes);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }
}

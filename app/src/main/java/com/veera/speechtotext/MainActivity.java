package com.veera.speechtotext;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

 Toolbar toolbar,toolbartab;
 ViewPager viewPager;
 TabLayout tabLayout;
 View parent;
 PageAdapter pageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);





        parent=findViewById(android.R.id.content);
        toolbar=(Toolbar) findViewById(R.id.toolbar);
        toolbartab=(Toolbar)findViewById(R.id.toolbartab);
        viewPager=(ViewPager)findViewById(R.id.viewpager);
        tabLayout=(TabLayout) findViewById(R.id.tablayout);


        setSupportActionBar(toolbar);


        pageAdapter =new PageAdapter(getSupportFragmentManager());
        pageAdapter.addFragment(new TranslateFragment(),"Trans");
        pageAdapter.addFragment(new CallLogFragment(),"Calls");
        pageAdapter.addFragment(new ContactsFragment(),"Contacts");
        pageAdapter.addFragment(new MessageFragment(),"Messages");

        viewPager.setPageTransformer(true,new DepthPageTransformer());

        viewPager.setAdapter(pageAdapter);

        tabLayout.setupWithViewPager(viewPager);


    }



}
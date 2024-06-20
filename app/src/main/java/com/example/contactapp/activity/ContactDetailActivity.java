/*
package com.example.contactapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.contactapp.model.Contact;
import com.example.contactapp.viewmodel.ContactViewModel;
import com.example.contactapp.R;

import java.util.Objects;

public class ContactDetailActivity extends AppCompatActivity {
    private ImageView contactImageView;
    private TextView nameTextView, phoneTextView, emailTextView, groupTextView;
    private ContactViewModel contactViewModel;
    private Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // 设置主题
        boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("dark_theme", false);
        setTheme(isDarkTheme ? R.style.Base_Theme_ContactApp_Dark : R.style.Base_Theme_ContactApp);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        // 初始化视图
        contactImageView = findViewById(R.id.contact_image);
        nameTextView = findViewById(R.id.name_text);
        phoneTextView = findViewById(R.id.phone_text);
        emailTextView = findViewById(R.id.email_text);
        groupTextView = findViewById(R.id.group_text);

        // 获取联系人ID并加载详细信息
        int contactId = getIntent().getIntExtra("CONTACT_ID", -1);
        if (contactId != -1) {
            contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
            contactViewModel.getContactById(contactId).observe(this, contact -> {
                this.contact = contact;
                if (contact != null) {
                    // 更新UI
                    nameTextView.setText(contact.getName());
                    phoneTextView.setText(contact.getPhone());
                    emailTextView.setText(contact.getEmail());
                    groupTextView.setText(contact.getGroup());
                    // 加载头像（如果有）
                    if (contact.getPhotoUri() != null) {
                        Glide.with(this)
                                .load(Uri.parse(contact.getPhotoUri()))
                                .apply(RequestOptions.circleCropTransform())
                                .into(contactImageView);
                    }
                }
            });
        }
        setupToolbar();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("详情");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(view -> onBackPressed());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            Intent intent = new Intent(ContactDetailActivity.this, ContactEditActivity.class);
            intent.putExtra("CONTACT_ID", contact.getId());
            startActivity(intent);
        }
        return true;
    }
}
*/

package com.example.contactapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.contactapp.R;
import com.example.contactapp.model.Contact;
import com.example.contactapp.viewmodel.ContactViewModel;

import java.util.Objects;

public class ContactDetailActivity extends AppCompatActivity {
    private ImageView contactImageView;
    private TextView nameTextView, phoneTextView, emailTextView, groupTextView;
    private ContactViewModel contactViewModel;
    private Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 根据用户偏好设置主题
        setThemeBasedOnPreference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);

        // 初始化视图
        initializeViews();
        setupToolbar();

        // 获取传递的联系人ID并加载详细信息
        int contactId = getIntent().getIntExtra("CONTACT_ID", -1);
        if (contactId != -1) {
            loadContactDetails(contactId);
        }
    }

    private void setThemeBasedOnPreference() {
        // 检查用户偏好设置，并应用相应的主题
        boolean isDarkTheme = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("dark_theme", false);
        setTheme(isDarkTheme ? R.style.Base_Theme_ContactApp_Dark : R.style.Base_Theme_ContactApp);
    }

    private void initializeViews() {
        // 通过ID查找视图
        contactImageView = findViewById(R.id.contact_image);
        nameTextView = findViewById(R.id.name_text);
        phoneTextView = findViewById(R.id.phone_text);
        emailTextView = findViewById(R.id.email_text);
        groupTextView = findViewById(R.id.group_text);
    }

    private void loadContactDetails(int contactId) {
        // 初始化 ViewModel 并观察联系人数据变化
        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        contactViewModel.getContactById(contactId).observe(this, contact -> {
            this.contact = contact;
            if (contact != null) {
                // 更新UI
                updateUI(contact);
            }
        });
    }

    private void updateUI(Contact contact) {
        // 将联系人数据填充到视图中
        nameTextView.setText(contact.getName());
        phoneTextView.setText(contact.getPhone());
        emailTextView.setText(contact.getEmail());
        groupTextView.setText(contact.getGroup());
        if (contact.getPhotoUri() != null) {
            Glide.with(this)
                    .load(Uri.parse(contact.getPhotoUri()))
                    .apply(RequestOptions.circleCropTransform())
                    .into(contactImageView);
        }
    }

    private void setupToolbar() {
        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("详情");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 加载菜单布局
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 处理菜单项点击事件
        if (item.getItemId() == R.id.action_edit) {
            Intent intent = new Intent(ContactDetailActivity.this, ContactEditActivity.class);
            intent.putExtra("CONTACT_ID", contact.getId());
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


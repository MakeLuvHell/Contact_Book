package com.example.contactapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

    /**
     * 通过ID查找视图
     */
    private void initializeViews() {

        contactImageView = findViewById(R.id.contact_image);
        nameTextView = findViewById(R.id.name_text);
        phoneTextView = findViewById(R.id.phone_text);
        emailTextView = findViewById(R.id.email_text);
        groupTextView = findViewById(R.id.group_text);
    }

    /**
     * 初始化 ViewModel 并观察联系人数据变化
     * @param contactId 传入的联系人id
     */
    private void loadContactDetails(int contactId) {
        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        contactViewModel.getContactById(contactId).observe(this, contact -> {
            this.contact = contact;
            if (contact != null) {
                // 更新UI
                updateUI(contact);
            }
        });
    }

    /**
     * 将联系人数据填充到视图中
     * @param contact 通过id获取的联系人实体
     */
    private void updateUI(Contact contact) {
        nameTextView.setText(contact.getName());
        phoneTextView.setText(contact.getPhone());
        emailTextView.setText(contact.getEmail());
        groupTextView.setText(contact.getGroup());

        Log.d("ContactAdapter", "Contact Name: " + contact.getName() + ", PhotoUri: " + contact.getPhotoUri());

        if (contact.getPhotoUri() != null) {
            Glide.with(this)
                    .load(Uri.parse(contact.getPhotoUri()))
                    .apply(RequestOptions.circleCropTransform())
                    .into(contactImageView);
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("详情");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            Intent intent = new Intent(ContactDetailActivity.this, ContactEditActivity.class);
            intent.putExtra("CONTACT_ID", contact.getId());
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


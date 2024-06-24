package com.example.contactapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.contactapp.R;
import com.example.contactapp.model.Contact;
import com.example.contactapp.model.Group;
import com.example.contactapp.viewmodel.ContactViewModel;
import com.example.contactapp.viewmodel.GroupViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContactEditActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private TextInputEditText nameEditText, phoneEditText, emailEditText;
    private Spinner groupSpinner;
    private ImageView contactImageView;
    private Button saveContactButton;
    private ContactViewModel contactViewModel;
    private Contact contact;
    private Uri photoUri;
    private final List<Group> groupList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 根据用户偏好设置主题
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);

        // 初始化视图
        initializeViews();
        setupToolbar();

        // 初始化 ViewModel
        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        GroupViewModel groupViewModel = new ViewModelProvider(this).get(GroupViewModel.class);

        loadContactDetails();
        loadGroupData(groupViewModel);
        setEventListeners();
    }


    private void initializeViews() {
        // 通过ID查找视图
        nameEditText = findViewById(R.id.edit_name);
        phoneEditText = findViewById(R.id.edit_phone);
        emailEditText = findViewById(R.id.edit_email);
        groupSpinner = findViewById(R.id.edit_group_spinner);
        contactImageView = findViewById(R.id.contact_image);
        saveContactButton = findViewById(R.id.save_contact_button);
    }

    private void loadContactDetails() {
        // 获取传递的联系人ID并加载详细信息
        int contactId = getIntent().getIntExtra("CONTACT_ID", -1);
        if (contactId != -1) {
            contactViewModel.getContactById(contactId).observe(this, contact -> {
                this.contact = contact;
                if (contact != null) {
                    // 将联系人数据填充到视图中
                    populateContactData(contact);
                }
            });
        }
    }

    private void populateContactData(Contact contact) {
        // 将联系人数据填充到视图中
        nameEditText.setText(contact.getName());
        phoneEditText.setText(contact.getPhone());
        emailEditText.setText(contact.getEmail());

        /*for (int i = 0; i < groupList.size(); i++) {
            if (groupList.get(i).getId() == contact.getGroupId()) {
                groupSpinner.setSelection(i);
                break;
            }
        }*/

        // 确保分组数据加载后匹配分组选项
        if (!groupList.isEmpty()) {
            for (int i = 0; i < groupList.size(); i++) {
                if (groupList.get(i).getId() == contact.getGroupId()) {
                    groupSpinner.setSelection(i + 1); // 因为 "未分组" 是第一个选项，所以加1
                    break;
                }
            }
        }

        if (contact.getPhotoUri() != null) {
            photoUri = Uri.parse(contact.getPhotoUri());
            Glide.with(this)
                    .load(photoUri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(contactImageView);
        }
    }

    private void loadGroupData(GroupViewModel groupViewModel) {
        // 从 ViewModel 加载分组数据并填充到 Spinner 中
        groupViewModel.getAllGroups().observe(this, groups -> {
            groupList.clear();
            groupList.addAll(groups);
            List<String> groupNames = new ArrayList<>();
            groupNames.add("未分组");
            for (Group group : groups) {
                groupNames.add(group.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groupNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            groupSpinner.setAdapter(adapter);

            // 确保分组数据加载后，匹配选项
            if (contact != null) {
                for (int i = 0; i < groupList.size(); i++) {
                    if (groupList.get(i).getId() == contact.getGroupId()) {
                        groupSpinner.setSelection(i + 1); // 因为 "未分组" 是第一个选项，所以加1
                        break;
                    }
                }
            }
        });
    }

    private void setEventListeners() {
        // 设置点击事件选择图片
        contactImageView.setOnClickListener(view -> openImagePicker());
        // 设置保存按钮点击事件
        saveContactButton.setOnClickListener(view -> saveContact());
    }

    private void openImagePicker() {
        // 打开图片选择器
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void saveContact() {
        // 保存联系人数据
        int selectedGroupId = -1;
        String selectedGroupName = "未分组";

        if (groupSpinner.getSelectedItemPosition() > 0) {
            selectedGroupId = groupList.get(groupSpinner.getSelectedItemPosition() - 1).getId();
            selectedGroupName = groupList.get(groupSpinner.getSelectedItemPosition() - 1).getName();
        }

        if (contact == null) {
            // 创建新联系人并插入数据库
            contact = new Contact(
                    nameEditText.getText().toString(),
                    phoneEditText.getText().toString(),
                    emailEditText.getText().toString(),
                    selectedGroupId,
                    selectedGroupName,
                    photoUri != null ? photoUri.toString() : null
            );
            contactViewModel.insert(contact);
        } else {
            // 更新现有联系人
            updateContactData(selectedGroupId, selectedGroupName);
        }
        finish();
    }

    private void updateContactData(int selectedGroupId, String selectedGroupName) {
        // 更新联系人数据并保存到数据库
        contact.setName(nameEditText.getText().toString());
        contact.setPhone(phoneEditText.getText().toString());
        contact.setEmail(emailEditText.getText().toString());
        contact.setGroupId(selectedGroupId);
        contact.setGroup(selectedGroupName);
        contact.setPhotoUri(photoUri != null ? photoUri.toString() : null);
        contactViewModel.update(contact);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 检查请求代码和结果代码，以确保图片被成功选择
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // 获取选中图片的URI
            Uri imageUri = data.getData();
            // 保存图片到本地存储，并获取本地存储的URI
            photoUri = saveImageToInternalStorage(imageUri);
            // 使用Glide加载本地存储的图片到ImageView
            Glide.with(this)
                    .load(photoUri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(contactImageView);
        }
    }

    // 将图片保存到应用的内部存储，并返回本地存储的URI
    private Uri saveImageToInternalStorage(Uri imageUri) {
        try {
            // 打开输入流读取选中的图片
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            // 创建一个文件用于存储图片
            File imageFile = new File(getFilesDir(), "contact_image_" + System.currentTimeMillis() + ".jpg");
            // 打开输出流将图片写入文件
            FileOutputStream outputStream = new FileOutputStream(imageFile);

            // 将图片从输入流写入输出流
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // 关闭流
            outputStream.close();
            inputStream.close();

            // 返回本地存储的图片URI
            return Uri.fromFile(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupToolbar() {
        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar_edit);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("编辑");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
    }
}

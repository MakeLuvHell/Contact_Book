package com.example.contactapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.contactapp.model.Contact;
import com.example.contactapp.viewmodel.ContactViewModel;
import com.example.contactapp.R;
import com.example.contactapp.activity.ContactDetailActivity;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_CARD = 2;
    private final Context context;
    private final ContactViewModel contactViewModel;
    private final List<Object> itemsWithHeaders;
    private final boolean useCardView;

    public SectionAdapter(Context context, ContactViewModel contactViewModel, boolean useCardView) {
        this.context = context;
        this.contactViewModel = contactViewModel;
        this.itemsWithHeaders = new ArrayList<>();
        this.useCardView = useCardView;
    }

    public void submitListWithHeaders(List<Contact> list) {
        itemsWithHeaders.clear();

        char lastHeader = '\0';

        for (Contact contact : list) {
            char headerChar = getHeaderChar(contact.getName());
            String header;
            if (Character.isDigit(headerChar) || !Character.isLetter(headerChar)) {
                header = "#";
            } else {
                header = String.valueOf(headerChar).toUpperCase(Locale.getDefault());
            }

            if (headerChar != lastHeader) {
                itemsWithHeaders.add(header);
                lastHeader = headerChar;
            }

            itemsWithHeaders.add(contact);
        }

        notifyDataSetChanged();
    }

    private char getHeaderChar(String name) {
        if (name == null || name.isEmpty()) {
            return '#';
        }
        char firstChar = name.charAt(0);
        if (Character.isLetter(firstChar)) {
            if (Character.toString(firstChar).matches("[\\u4e00-\\u9fa5]+")) { // Check if it is a Chinese character
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(firstChar);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    return Character.toUpperCase(pinyinArray[0].charAt(0));
                }
            } else {
                return Character.toUpperCase(firstChar);
            }
        }
        return '#';
    }

    @Override
    public int getItemViewType(int position) {
        if (itemsWithHeaders.get(position) instanceof String) {
            return TYPE_HEADER;
        } else {
            return useCardView ? TYPE_CARD : TYPE_ITEM;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_header, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == TYPE_CARD) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_card, parent, false);
            return new ContactViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_list, parent, false);
            return new ContactViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) itemsWithHeaders.get(position));
        } else if (holder instanceof ContactViewHolder) {
            ((ContactViewHolder) holder).bind((Contact) itemsWithHeaders.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return itemsWithHeaders.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView headerTextView;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTextView = itemView.findViewById(R.id.header_text);
        }

        public void bind(String header) {
            headerTextView.setText(header);
        }
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView phoneTextView;
        private final ImageView contactImageView;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.contact_name);
            phoneTextView = itemView.findViewById(R.id.contact_phone);
            contactImageView = itemView.findViewById(R.id.contact_image);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Contact contact = (Contact) itemsWithHeaders.get(position);
                    Intent intent = new Intent(context, ContactDetailActivity.class);
                    intent.putExtra("CONTACT_ID", contact.getId());
                    context.startActivity(intent);
                }
            });

            itemView.setOnLongClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Contact contact = (Contact) itemsWithHeaders.get(position);
                    showDeleteConfirmationDialog(contact);
                }
                return true;
            });
        }

        public void bind(Contact contact) {
            nameTextView.setText(contact.getName());
            phoneTextView.setText(contact.getPhone());
            if (contact.getPhotoUri() != null) {
                Glide.with(context)
                        .load(contact.getPhotoUri())
                        .into(contactImageView);
            } else {
                contactImageView.setImageResource(R.drawable.ic_default);
            }
        }

        private void showDeleteConfirmationDialog(Contact contact) {
            new AlertDialog.Builder(context)
                    .setTitle("删除联系人")
                    .setMessage("确定要删除该联系人？")
                    .setPositiveButton("删除", (dialog, which) -> contactViewModel.delete(contact))
                    .setNegativeButton("取消", null)
                    .show();
        }
    }

    public int getPositionForSection(char section) {
        for (int i = 0; i < itemsWithHeaders.size(); i++) {
            Object item = itemsWithHeaders.get(i);
            if (item instanceof String && ((String) item).charAt(0) == section) {
                return i;
            }
        }
        return -1;
    }
}

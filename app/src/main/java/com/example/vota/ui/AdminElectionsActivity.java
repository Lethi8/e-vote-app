package com.example.vota.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import com.example.vota.R;
import com.example.vota.model.Election;
import com.example.vota.net.ApiClient;
import com.example.vota.net.RestApi;
import com.example.vota.util.SessionManager;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminElectionsActivity extends BaseActivity {
    private RestApi api;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        SessionManager session = new SessionManager(this);
        if (!"admin".equalsIgnoreCase(session.role())) { finish(); return; }
        api = ApiClient.get(session).create(RestApi.class);
        page("ADMIN • ELECTIONS", "Manage elections", "Manage elections in Supabase.");
        enablePageScrollbar();
        primary("Add election").setOnClickListener(v -> form(null));
        secondary("Refresh").setOnClickListener(v -> load());
        load();
    }
    private void clear() {
        LinearLayout cards = findViewById(R.id.contentContainer);
        if (cards != null) cards.removeAllViews();
    }
    private void load() {
        clear(); addSection("Loading", "Fetching elections..."); addBackSection();
        api.elections().enqueue(new Callback<List<Election>>() {
            @Override public void onResponse(Call<List<Election>> call, Response<List<Election>> r) {
                clear();
                if (!r.isSuccessful() || r.body() == null) {
                    addSection("Failed to load", error(r)); addBackSection(); return;
                }
                if (r.body().isEmpty()) addSection("No elections", "Use Add election to create one.");
                for (Election e : r.body()) {
                    String details = "Type: " + safe(e.type) + "\nRegion: " + safe(e.region)
                            + "\nStatus: " + safe(e.status) + "\nOpens: " + safe(e.opensAt)
                            + "\nCloses: " + safe(e.closesAt)
                            + "\nResults published: " + (e.resultsPublished ? "Yes" : "No")
                            + "\nDescription: " + safe(e.description);
                    addActionSection(safe(e.title), details, "Manage", v -> actions(e));
                }
                addBackSection();
            }
            @Override public void onFailure(Call<List<Election>> call, Throwable t) {
                clear(); addSection("Network error", safe(t.getMessage())); addBackSection();
            }
        });
    }
    private void actions(Election e) {
        new AlertDialog.Builder(this).setTitle(safe(e.title))
                .setItems(new String[]{"Edit", "Delete", "Cancel"}, (d, choice) -> {
                    if (choice == 0) form(e);
                    if (choice == 1) delete(e);
                }).show();
    }
    private EditText input(LinearLayout layout, String label, String initial) {
        EditText view = new EditText(this);
        view.setHint(label); view.setSingleLine(true);
        view.setText(initial == null ? "" : initial);
        layout.addView(view);
        return view;
    }
    private void form(Election old) {
        LinearLayout fields = new LinearLayout(this);
        fields.setOrientation(LinearLayout.VERTICAL);
        int p = (int)(20 * getResources().getDisplayMetrics().density);
        fields.setPadding(p, p, p, p);
        EditText title = input(fields, "Title", old == null ? "" : old.title);
        EditText type = input(fields, "Type", old == null ? "" : old.type);
        EditText desc = input(fields, "Description", old == null ? "" : old.description);
        EditText region = input(fields, "Region", old == null ? "" : old.region);
        EditText status = input(fields, "Status (database-approved value)", old == null ? "" : old.status);
        EditText opens = input(fields, "Opens: 2026-11-01T08:00:00+02:00", old == null ? "" : old.opensAt);
        EditText closes = input(fields, "Closes: 2026-11-01T17:00:00+02:00", old == null ? "" : old.closesAt);
        CheckBox published = new CheckBox(this);
        published.setText("Results published");
        published.setChecked(old != null && old.resultsPublished);
        fields.addView(published);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(fields);
        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle(old == null ? "Create election" : "Edit election")
                .setView(scroll).setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null).create();
        dlg.setOnShowListener(x -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String a = val(title), b = val(type), c = val(region), d = val(status);
            String start = val(opens), end = val(closes);
            Date startDate = date(start), endDate = date(end);
            if (a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()
                    || startDate == null || endDate == null || !endDate.after(startDate)) {
                Toast.makeText(this, "Enter title, type, region, status and valid opening/closing timestamps (closing later)", Toast.LENGTH_LONG).show();
                return;
            }
            Map<String,Object> body = new HashMap<>();
            body.put("title", a); body.put("type", b); body.put("description", val(desc));
            body.put("region", c); body.put("status", d);
            body.put("opens_at", start); body.put("closes_at", end);
            body.put("results_published", published.isChecked());
            Call<Void> call = old == null ? api.createElection("return=minimal", body)
                    : api.updateElection("eq." + old.id, "return=minimal", body);
            call.enqueue(new Callback<Void>() {
                @Override public void onResponse(Call<Void> call, Response<Void> result) {
                    if (result.isSuccessful()) {
                        dlg.dismiss(); Toast.makeText(AdminElectionsActivity.this, "Saved", Toast.LENGTH_SHORT).show(); load();
                    } else showError("Save failed", error(result));
                }
                @Override public void onFailure(Call<Void> call, Throwable t) { showError("Network error", safe(t.getMessage())); }
            });
        }));
        dlg.show();
    }
    private void delete(Election e) {
        new AlertDialog.Builder(this).setTitle("Delete election?")
                .setMessage("Never delete a live election or one with votes. Deletion may be irreversible, and the database may block it if related records exist.")
                .setNegativeButton("Cancel", null).setPositiveButton("Delete", (d, w) -> {
                    api.deleteElection("eq." + e.id).enqueue(new Callback<Void>() {
                        @Override public void onResponse(Call<Void> call, Response<Void> r) {
                            if (r.isSuccessful()) { Toast.makeText(AdminElectionsActivity.this, "Delete request completed", Toast.LENGTH_SHORT).show(); load(); }
                            else showError("Delete failed", error(r));
                        }
                        @Override public void onFailure(Call<Void> call, Throwable t) { showError("Network error", safe(t.getMessage())); }
                    });
                }).show();
    }
    private Date date(String value) {
        if (!value.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}")) return null;
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
        f.setLenient(false);
        try { return f.parse(value); } catch (java.text.ParseException ex) { return null; }
    }
    private String val(EditText e) { return e.getText().toString().trim(); }
    private String safe(String s) { return s == null ? "" : s; }
    private String error(Response<?> r) {
        String message = "HTTP " + r.code();
        if (r.errorBody() != null) try { message += "\n" + r.errorBody().string(); } catch (IOException ignored) {}
        return message;
    }
    private void showError(String title, String message) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK", null).show();
    }
}

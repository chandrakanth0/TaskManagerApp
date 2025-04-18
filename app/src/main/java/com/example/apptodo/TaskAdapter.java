package com.example.apptodo;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private ArrayList<Task> taskList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public TaskAdapter(ArrayList<Task> tasks) {
        this.taskList = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.titleText.setText(task.getTitle());
        holder.descriptionText.setText(task.getDescription());
        holder.startDateText.setText("Start: " + task.getStartDate());
        holder.dueDateText.setText("Due: " + task.getDueDate());
        holder.timeText.setText("Time: " + task.getTime());

        // Calculate and display remaining days
        try {
            Date dueDate = dateFormat.parse(task.getDueDate());
            Date currentDate = new Date();
            long timeDifference = dueDate.getTime() - currentDate.getTime();
            long remainingDays = TimeUnit.DAYS.convert(timeDifference, TimeUnit.MILLISECONDS);

            if (!task.isCompleted()) {
                if (remainingDays >= 0) {
                    holder.remainingDaysText.setText(remainingDays + " days left");
                    holder.remainingDaysText.setTextColor(Color.parseColor("#1976D2")); // Blue
                } else {
                    holder.remainingDaysText.setText("Overdue");
                    holder.remainingDaysText.setTextColor(Color.RED);
                }
            } else {
                holder.remainingDaysText.setText("Completed");
                holder.remainingDaysText.setTextColor(Color.GRAY);
            }
        } catch (ParseException e) {
            holder.remainingDaysText.setText("Invalid Date");
            holder.remainingDaysText.setTextColor(Color.RED);
            e.printStackTrace();
        }

        if (task.isCompleted()) {
            holder.titleText.setPaintFlags(holder.titleText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.titleText.setTextColor(Color.GRAY);
            holder.completeButton.setVisibility(View.GONE);
        } else {
            holder.titleText.setPaintFlags(holder.titleText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.titleText.setTextColor(Color.BLACK);
            holder.completeButton.setVisibility(View.VISIBLE);
        }

        // Mark as completed
        holder.completeButton.setOnClickListener(v -> {
            task.setCompleted(true);
            notifyItemChanged(position);

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance()
                    .collection("users").document(userId)
                    .collection("tasks").document(task.getDocumentId())
                    .update("completed", true);
        });

        // Delete task
        holder.deleteButton.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance()
                    .collection("users").document(userId)
                    .collection("tasks").document(task.getDocumentId())
                    .delete();

            taskList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, taskList.size());
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, descriptionText, startDateText, dueDateText, timeText, remainingDaysText;
        Button deleteButton, completeButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            startDateText = itemView.findViewById(R.id.startDateText);
            dueDateText = itemView.findViewById(R.id.dueDateText);
            timeText = itemView.findViewById(R.id.timeText);
            remainingDaysText = itemView.findViewById(R.id.remainingDaysText); // Initialize the new TextView
            deleteButton = itemView.findViewById(R.id.deleteButton);
            completeButton = itemView.findViewById(R.id.completeButton);
        }
    }
}
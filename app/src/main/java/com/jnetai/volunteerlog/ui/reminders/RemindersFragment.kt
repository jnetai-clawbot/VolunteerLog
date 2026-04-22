package com.jnetai.volunteerlog.ui.reminders

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jnetai.volunteerlog.R
import com.jnetai.volunteerlog.data.entity.Reminder
import com.jnetai.volunteerlog.databinding.FragmentRemindersBinding
import com.jnetai.volunteerlog.databinding.ItemReminderBinding
import com.jnetai.volunteerlog.reminders.ReminderScheduler
import com.jnetai.volunteerlog.ui.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class RemindersFragment : Fragment() {

    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RemindersAdapter
    private var selectedDateTime: Long = System.currentTimeMillis()
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.UK)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repo = (requireActivity() as MainActivity).repository

        adapter = RemindersAdapter({ reminder ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete)
                .setMessage("Delete this reminder?")
                .setPositiveButton(R.string.delete) { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        ReminderScheduler.cancel(requireContext(), reminder.id)
                        withContext(Dispatchers.IO) { repo.deleteReminder(reminder) }
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        })
        binding.rvReminders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReminders.adapter = adapter

        updateDateTimeDisplay()

        // Load organisations for autocomplete
        viewLifecycleOwner.lifecycleScope.launch {
            val orgs = withContext(Dispatchers.IO) { repo.getAllOrganisationsOnce() }
            val names = orgs.map { it.name }
            val actvAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
            binding.actvReminderOrg.setAdapter(actvAdapter)
        }

        binding.etReminderDateTime.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                TimePickerDialog(requireContext(), { _, h, min ->
                    selectedDateTime = Calendar.getInstance().apply {
                        set(y, m, d, h, min)
                    }.timeInMillis
                    updateDateTimeDisplay()
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnSaveReminder.setOnClickListener {
            val title = binding.etReminderTitle.text.toString().trim()
            val message = binding.etReminderMessage.text.toString().trim()
            val orgName = binding.actvReminderOrg.text.toString().trim()

            if (title.isEmpty()) {
                binding.tilReminderTitle.error = getString(R.string.required_field)
                return@setOnClickListener
            }
            binding.tilReminderTitle.error = null

            val reminder = Reminder(
                title = title,
                message = message,
                organisationName = orgName,
                dateTime = selectedDateTime
            )

            viewLifecycleOwner.lifecycleScope.launch {
                val id = withContext(Dispatchers.IO) { repo.insertReminder(reminder) }
                val saved = reminder.copy(id = id)
                ReminderScheduler.schedule(requireContext(), saved)
                Toast.makeText(requireContext(), R.string.reminder_set, Toast.LENGTH_SHORT).show()
                clearForm()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repo.getAllReminders().collect { reminders ->
                adapter.submitList(reminders)
                binding.tvNoReminders.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
                binding.rvReminders.visibility = if (reminders.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun updateDateTimeDisplay() {
        binding.etReminderDateTime.setText(dateTimeFormat.format(Date(selectedDateTime)))
    }

    private fun clearForm() {
        binding.etReminderTitle.text?.clear()
        binding.etReminderMessage.text?.clear()
        binding.actvReminderOrg.text?.clear()
        selectedDateTime = System.currentTimeMillis()
        updateDateTimeDisplay()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class RemindersAdapter(
        private val onDelete: (Reminder) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<RemindersAdapter.VH>() {

        private var items: List<Reminder> = emptyList()

        fun submitList(list: List<Reminder>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(binding)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val reminder = items[position]
            holder.binding.tvReminderTitle.text = reminder.title
            holder.binding.tvReminderDateTime.text = dateTimeFormat.format(Date(reminder.dateTime))
            holder.binding.tvReminderMessage.text = reminder.message
            holder.binding.btnDeleteReminder.setOnClickListener { onDelete(reminder) }
        }

        override fun getItemCount() = items.size

        inner class VH(val binding: ItemReminderBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)
    }
}
package com.jnetai.volunteerlog.ui.hours

import android.app.DatePickerDialog
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jnetai.volunteerlog.R
import com.jnetai.volunteerlog.data.entity.VolunteerEntry
import com.jnetai.volunteerlog.databinding.DialogEditEntryBinding
import com.jnetai.volunteerlog.databinding.FragmentHoursListBinding
import com.jnetai.volunteerlog.ui.MainActivity
import com.jnetai.volunteerlog.util.ExportUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class HoursListFragment : Fragment() {

    private var _binding: FragmentHoursListBinding? = null
    private val binding get() = _binding!!
    private lateinit var entriesAdapter: EntriesAdapter
    private var filterFrom: Long? = null
    private var filterTo: Long? = null
    private var filterOrg: String? = null
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.UK)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHoursListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repo = (requireActivity() as MainActivity).repository

        entriesAdapter = EntriesAdapter({ entry -> showEditDialog(entry, repo) }, { entry ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete)
                .setMessage("Delete this entry?")
                .setPositiveButton(R.string.delete) { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.IO) { repo.deleteEntry(entry) }
                        Toast.makeText(requireContext(), R.string.entry_deleted, Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        })
        binding.rvEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEntries.adapter = entriesAdapter

        // Load organisations for spinner
        viewLifecycleOwner.lifecycleScope.launch {
            val orgs = withContext(Dispatchers.IO) { repo.getAllOrganisationsOnce() }
            val names = listOf(getString(R.string.all_organisations)) + orgs.map { it.name }
            val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerOrg.adapter = spinnerAdapter
        }

        // Date pickers
        binding.etFromDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                Calendar.getInstance().apply { set(y, m, d) }; filterFrom = Calendar.getInstance().apply { set(y, m, d, 0, 0, 0) }.timeInMillis
                binding.etFromDate.setText(dateFormat.format(Date(filterFrom!!)))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.etToDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                filterTo = Calendar.getInstance().apply { set(y, m, d, 23, 59, 59) }.timeInMillis
                binding.etToDate.setText(dateFormat.format(Date(filterTo!!)))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Filter
        binding.btnApplyFilter.setOnClickListener {
            val orgPos = binding.spinnerOrg.selectedItemPosition
            filterOrg = if (orgPos == 0) null else binding.spinnerOrg.selectedItem.toString()
            applyFilter(repo)
        }

        binding.btnClearFilter.setOnClickListener {
            filterFrom = null
            filterTo = null
            filterOrg = null
            binding.etFromDate.text?.clear()
            binding.etToDate.text?.clear()
            binding.spinnerOrg.setSelection(0)
            observeEntries(repo)
        }

        // Export
        binding.btnExportJson.setOnClickListener { export(repo, "json") }
        binding.btnExportCsv.setOnClickListener { export(repo, "csv") }

        observeEntries(repo)
    }

    private fun applyFilter(repo: com.jnetai.volunteerlog.data.repository.VolunteerRepository) {
        var flow: Flow<List<VolunteerEntry>>? = null
        when {
            filterOrg != null && filterFrom != null && filterTo != null ->
                flow = repo.getByOrgAndDateRange(filterOrg!!, filterFrom!!, filterTo!!)
            filterFrom != null && filterTo != null ->
                flow = repo.getByDateRange(filterFrom!!, filterTo!!)
            filterOrg != null ->
                flow = repo.getByOrganisation(filterOrg!!)
        }
        if (flow != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                flow.collect { list ->
                    entriesAdapter.submitList(list)
                    binding.tvNoEntries.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        } else {
            observeEntries(repo)
        }
    }

    private fun observeEntries(repo: com.jnetai.volunteerlog.data.repository.VolunteerRepository) {
        viewLifecycleOwner.lifecycleScope.launch {
            repo.getAllEntries().collect { entries ->
                entriesAdapter.submitList(entries)
                binding.tvNoEntries.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun export(repo: com.jnetai.volunteerlog.data.repository.VolunteerRepository, format: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val entries = withContext(Dispatchers.IO) { repo.getAllEntriesOnce() }
            val dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: File(Environment.DIRECTORY_DOWNLOADS)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(Date())
            val file = when (format) {
                "json" -> {
                    val content = ExportUtil.entriesToJson(entries)
                    ExportUtil.saveToFile(dir, "volunteerlog_$timestamp.json", content)
                }
                else -> {
                    val content = ExportUtil.entriesToCsv(entries)
                    ExportUtil.saveToFile(dir, "volunteerlog_$timestamp.csv", content)
                }
            }
            Toast.makeText(requireContext(), "${getString(R.string.export_saved)}: ${file.name}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showEditDialog(entry: VolunteerEntry, repo: com.jnetai.volunteerlog.data.repository.VolunteerRepository) {
        val dialogBinding = DialogEditEntryBinding.inflate(layoutInflater)
        var editDate = entry.date

        dialogBinding.etEditOrgName.setText(entry.organisationName)
        dialogBinding.etEditDate.setText(dateFormat.format(Date(entry.date)))
        dialogBinding.etEditHours.setText(entry.hours.toString())
        dialogBinding.etEditRole.setText(entry.role)
        dialogBinding.etEditNotes.setText(entry.notes)

        dialogBinding.etEditDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { timeInMillis = editDate }
            DatePickerDialog(requireContext(), { _, y, m, d ->
                editDate = Calendar.getInstance().apply { set(y, m, d) }.timeInMillis
                dialogBinding.etEditDate.setText(dateFormat.format(Date(editDate)))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val updated = entry.copy(
                    organisationName = dialogBinding.etEditOrgName.text.toString().trim(),
                    date = editDate,
                    hours = dialogBinding.etEditHours.text.toString().trim().toDoubleOrNull() ?: entry.hours,
                    role = dialogBinding.etEditRole.text.toString().trim(),
                    notes = dialogBinding.etEditNotes.text.toString().trim()
                )
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) { repo.updateEntry(updated) }
                    Toast.makeText(requireContext(), R.string.entry_saved, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class EntriesAdapter(
        private val onEdit: (VolunteerEntry) -> Unit,
        private val onDelete: (VolunteerEntry) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<EntriesAdapter.VH>() {

        private var items: List<VolunteerEntry> = emptyList()

        fun submitList(list: List<VolunteerEntry>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_entry, parent, false)
        )

        override fun onBindViewHolder(holder: VH, position: Int) {
            val entry = items[position]
            holder.org.text = entry.organisationName
            holder.date.text = dateFormat.format(Date(entry.date))
            holder.role.text = entry.role
            holder.hours.text = String.format("%.1f hrs", entry.hours)

            holder.itemView.setOnLongClickListener {
                val options = arrayOf(getString(R.string.edit), getString(R.string.delete))
                MaterialAlertDialogBuilder(requireContext())
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> onEdit(entry)
                            1 -> onDelete(entry)
                        }
                    }
                    .show()
                true
            }
        }

        override fun getItemCount() = items.size

        inner class VH(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val org: TextView = view.findViewById(R.id.tvEntryOrg)
            val date: TextView = view.findViewById(R.id.tvEntryDate)
            val role: TextView = view.findViewById(R.id.tvEntryRole)
            val hours: TextView = view.findViewById(R.id.tvEntryHours)
        }
    }
}
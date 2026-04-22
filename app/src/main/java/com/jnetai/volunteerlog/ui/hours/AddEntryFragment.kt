package com.jnetai.volunteerlog.ui.hours

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.jnetai.volunteerlog.R
import com.jnetai.volunteerlog.data.entity.VolunteerEntry
import com.jnetai.volunteerlog.databinding.FragmentAddEntryBinding
import com.jnetai.volunteerlog.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AddEntryFragment : Fragment() {

    private var _binding: FragmentAddEntryBinding? = null
    private val binding get() = _binding!!
    private var selectedDate: Long = System.currentTimeMillis()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repo = (requireActivity() as MainActivity).repository
        updateDateDisplay()

        // Load organisations for autocomplete
        viewLifecycleOwner.lifecycleScope.launch {
            val orgs = withContext(Dispatchers.IO) { repo.getAllOrganisationsOnce() }
            val names = orgs.map { it.name }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
            binding.actvOrgName.setAdapter(adapter)
        }

        binding.etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                Calendar.getInstance().apply {
                    set(year, month, day)
                    selectedDate = timeInMillis
                }
                updateDateDisplay()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnSaveEntry.setOnClickListener {
            val orgName = binding.actvOrgName.text.toString().trim()
            val hoursStr = binding.etHours.text.toString().trim()
            val role = binding.etRole.text.toString().trim()
            val notes = binding.etNotes.text.toString().trim()

            if (orgName.isEmpty()) {
                binding.tilOrgName.error = getString(R.string.required_field)
                return@setOnClickListener
            }
            binding.tilOrgName.error = null

            val hours = hoursStr.toDoubleOrNull()
            if (hours == null || hours <= 0) {
                binding.tilHours.error = getString(R.string.invalid_hours)
                return@setOnClickListener
            }
            binding.tilHours.error = null

            val entry = VolunteerEntry(
                organisationName = orgName,
                date = selectedDate,
                hours = hours,
                role = role,
                notes = notes
            )

            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) { repo.insertEntry(entry) }
                Toast.makeText(requireContext(), R.string.entry_saved, Toast.LENGTH_SHORT).show()
                clearForm()
            }
        }
    }

    private fun updateDateDisplay() {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.UK)
        binding.etDate.setText(sdf.format(java.util.Date(selectedDate)))
    }

    private fun clearForm() {
        binding.actvOrgName.text.clear()
        binding.etHours.text?.clear()
        binding.etRole.text?.clear()
        binding.etNotes.text?.clear()
        selectedDate = System.currentTimeMillis()
        updateDateDisplay()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.jnetai.volunteerlog.ui.organisations

import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jnetai.volunteerlog.R
import com.jnetai.volunteerlog.data.entity.Organisation
import com.jnetai.volunteerlog.databinding.DialogAddOrganisationBinding
import com.jnetai.volunteerlog.databinding.FragmentOrganisationsBinding
import com.jnetai.volunteerlog.databinding.ItemOrganisationBinding
import com.jnetai.volunteerlog.ui.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrganisationsFragment : Fragment() {

    private var _binding: FragmentOrganisationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: OrgsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrganisationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repo = (requireActivity() as MainActivity).repository

        adapter = OrgsAdapter({ org ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete)
                .setMessage("Delete ${org.name}?")
                .setPositiveButton(R.string.delete) { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.IO) { repo.deleteOrganisation(org) }
                        Toast.makeText(requireContext(), "Organisation deleted", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        })
        binding.rvOrganisations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrganisations.adapter = adapter

        binding.btnAddOrganisation.setOnClickListener { showAddDialog(repo) }

        viewLifecycleOwner.lifecycleScope.launch {
            repo.getAllOrganisations().collect { orgs ->
                adapter.submitList(orgs)
                binding.tvNoOrgs.visibility = if (orgs.isEmpty()) View.VISIBLE else View.GONE
                binding.rvOrganisations.visibility = if (orgs.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun showAddDialog(repo: com.jnetai.volunteerlog.data.repository.VolunteerRepository) {
        val dialogBinding = DialogAddOrganisationBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_organisation)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = dialogBinding.etDialogOrgName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.required_field, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val org = Organisation(
                    name = name,
                    contactPhone = dialogBinding.etDialogOrgPhone.text.toString().trim(),
                    contactEmail = dialogBinding.etDialogOrgEmail.text.toString().trim(),
                    contactInfo = dialogBinding.etDialogOrgContact.text.toString().trim()
                )
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) { repo.insertOrganisation(org) }
                    Toast.makeText(requireContext(), "Organisation added", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class OrgsAdapter(
        private val onDelete: (Organisation) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<OrgsAdapter.VH>() {

        private var items: List<Organisation> = emptyList()

        fun submitList(list: List<Organisation>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val binding = ItemOrganisationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(binding)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val org = items[position]
            holder.binding.tvOrgName.text = org.name
            val contactParts = mutableListOf<String>()
            if (org.contactPhone.isNotEmpty()) contactParts.add("📞 ${org.contactPhone}")
            if (org.contactEmail.isNotEmpty()) contactParts.add("📧 ${org.contactEmail}")
            if (org.contactInfo.isNotEmpty()) contactParts.add(org.contactInfo)
            holder.binding.tvOrgContact.text = contactParts.joinToString("\n")
            holder.binding.btnDeleteOrg.setOnClickListener { onDelete(org) }
        }

        override fun getItemCount() = items.size

        inner class VH(val binding: ItemOrganisationBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)
    }
}
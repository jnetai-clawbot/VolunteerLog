package com.jnetai.volunteerlog.ui.goals

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jnetai.volunteerlog.R
import com.jnetai.volunteerlog.data.entity.Goal
import com.jnetai.volunteerlog.databinding.FragmentGoalsBinding
import com.jnetai.volunteerlog.databinding.ItemGoalBinding
import com.jnetai.volunteerlog.ui.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GoalsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repo = (requireActivity() as MainActivity).repository

        adapter = GoalsAdapter(repo, viewLifecycleOwner.lifecycleScope)
        binding.rvGoals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGoals.adapter = adapter

        // Load organisations for spinner
        viewLifecycleOwner.lifecycleScope.launch {
            val orgs = withContext(Dispatchers.IO) { repo.getAllOrganisationsOnce() }
            val names = listOf("Global") + orgs.map { it.name }
            val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerGoalOrg.adapter = spinnerAdapter
        }

        binding.btnSaveGoal.setOnClickListener {
            val targetStr = binding.etTargetHours.text.toString().trim()
            val target = targetStr.toIntOrNull()
            if (target == null || target <= 0) {
                Toast.makeText(requireContext(), "Enter a valid goal", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val orgPos = binding.spinnerGoalOrg.selectedItemPosition
            val orgName = if (orgPos == 0) "" else binding.spinnerGoalOrg.selectedItem.toString()

            val goal = Goal(targetHours = target, organisationName = orgName)

            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) { repo.insertGoal(goal) }
                Toast.makeText(requireContext(), "Goal saved", Toast.LENGTH_SHORT).show()
                binding.etTargetHours.text?.clear()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repo.getAllGoals().collect { goals ->
                adapter.submitList(goals)
                binding.tvNoGoals.visibility = if (goals.isEmpty()) View.VISIBLE else View.GONE
                binding.rvGoals.visibility = if (goals.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class GoalsAdapter(
        private val repo: com.jnetai.volunteerlog.data.repository.VolunteerRepository,
        private val scope: androidx.lifecycle.LifecycleCoroutineScope
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<GoalsAdapter.VH>() {

        private var items: List<Goal> = emptyList()

        fun submitList(list: List<Goal>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val binding = ItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(binding)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val goal = items[position]
            holder.binding.tvGoalOrg.text = if (goal.organisationName.isEmpty()) "Global Goal" else goal.organisationName

            // Get hours for this goal
            val hoursFlow: Flow<Double?> = if (goal.organisationName.isEmpty()) {
                repo.getTotalHours()
            } else {
                repo.getHoursForOrganisation(goal.organisationName)
            }

            scope.launch {
                hoursFlow.collect { total ->
                    val hours = total ?: 0.0
                    val progress = if (goal.targetHours > 0) ((hours / goal.targetHours) * 100).toInt().coerceAtMost(100) else 0
                    holder.binding.progressBar.progress = progress
                    holder.binding.tvGoalProgress.text = String.format("%d / %d hrs (%.0f%%)", hours.toInt(), goal.targetHours, progress.toFloat())
                    holder.binding.tvGoalAchieved.visibility = if (hours >= goal.targetHours) View.VISIBLE else View.GONE
                }
            }

            holder.binding.btnDeleteGoal.setOnClickListener {
                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle(R.string.delete)
                    .setMessage("Delete this goal?")
                    .setPositiveButton(R.string.delete) { _, _ ->
                        scope.launch {
                            withContext(Dispatchers.IO) { repo.deleteGoalById(goal.id) }
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }

        override fun getItemCount() = items.size

        inner class VH(val binding: ItemGoalBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)
    }
}
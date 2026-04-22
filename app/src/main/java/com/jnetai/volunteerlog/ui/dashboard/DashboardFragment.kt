package com.jnetai.volunteerlog.ui.dashboard

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jnetai.volunteerlog.R
import com.jnetai.volunteerlog.data.dao.OrgHours
import com.jnetai.volunteerlog.data.entity.VolunteerEntry
import com.jnetai.volunteerlog.databinding.FragmentDashboardBinding
import com.jnetai.volunteerlog.ui.MainActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var orgHoursAdapter: OrgHoursAdapter
    private lateinit var recentEntriesAdapter: RecentEntriesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repo = (requireActivity() as MainActivity).repository

        orgHoursAdapter = OrgHoursAdapter()
        binding.rvOrgHours.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOrgHours.adapter = orgHoursAdapter

        recentEntriesAdapter = RecentEntriesAdapter()
        binding.rvRecentEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentEntries.adapter = recentEntriesAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            repo.getTotalHours().collectLatest { hours ->
                binding.tvTotalHours.text = String.format("%.1f", hours ?: 0.0)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repo.getHoursByOrganisation().collectLatest { list ->
                orgHoursAdapter.submitList(list)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repo.getAllEntries().collectLatest { entries ->
                if (entries.isEmpty()) {
                    binding.tvNoEntries.visibility = View.VISIBLE
                    binding.rvRecentEntries.visibility = View.GONE
                } else {
                    binding.tvNoEntries.visibility = View.GONE
                    binding.rvRecentEntries.visibility = View.VISIBLE
                    recentEntriesAdapter.submitList(entries.take(5))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class OrgHoursAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<OrgHoursAdapter.VH>() {
        private var items: List<OrgHours> = emptyList()

        fun submitList(list: List<OrgHours>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            LayoutInflater.from(parent.context).inflate(R.layout.item_org_hours, parent, false)
        )

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.orgName.text = item.organisationName
            holder.orgHours.text = String.format("%.1f hrs", item.hours)
        }

        override fun getItemCount() = items.size

        inner class VH(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val orgName: TextView = view.findViewById(R.id.tvOrgName)
            val orgHours: TextView = view.findViewById(R.id.tvOrgHours)
        }
    }

    inner class RecentEntriesAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<RecentEntriesAdapter.VH>() {
        private var items: List<VolunteerEntry> = emptyList()
        private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.UK)

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
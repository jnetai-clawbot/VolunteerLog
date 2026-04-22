package com.jnetai.volunteerlog.ui.hours

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jnetai.volunteerlog.databinding.FragmentHoursBinding

class HoursFragment : Fragment() {

    private var _binding: FragmentHoursBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagerAdapter = HoursPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        com.google.android.material.tabs.TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Log Hours"
                1 -> "Reminders"
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class HoursPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount() = 2
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> AddEntryFragment()
                1 -> com.jnetai.volunteerlog.ui.reminders.RemindersFragment()
                else -> AddEntryFragment()
            }
        }
    }
}